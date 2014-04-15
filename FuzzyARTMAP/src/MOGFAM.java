import javax.xml.crypto.Data;
import java.awt.*;
import java.util.*;

/**
 * Created by vic on 3/26/14.
 */
public class MOGFAM
{
    private static int POPULATION_SIZE = 20;
    private static int TOTAL_GENERATIONS = 500;
    private static double MUTATION_PARAM = 0.00005;
    private static double CHOICE_PARAM = 0.01;
    private static Chromosome.CrossoverType CROSSOVER_TYPE = Chromosome.CrossoverType.CrossoverLabels;

    private static int TOTAL_RUNS = 5;

    private static String DATA = "data/g4c_25";
    //private static String DATA = "data/g6c_15";
    //private static String DATA = "data/Iris5000";

    public static void main(String[ ] args)
    {
        //Training data
        DataReader trainingDataReader = new DataReader(DATA, DataReader.Type.TRAINING);
        Map<Double[], Integer> trainingSet = trainingDataReader.GetInputMap();

        //Validation data
        DataReader validationDataReader = new DataReader(DATA, DataReader.Type.VALIDATION);
        Map<Double[], Integer> validationSet = validationDataReader.GetInputMap();

        Map<Integer, ArrayList<Chromosome>> resultsMap = new LinkedHashMap<Integer, ArrayList<Chromosome>>();

        for (int run = 0; run < TOTAL_RUNS; run++)
        {
            Random random = new Random(); //new Random(SEED);

            ArrayList<Chromosome> P = new ArrayList<Chromosome>();
            ArrayList<Chromosome> A = new ArrayList<Chromosome>();

            //Generate initial population
            for (int i = 0; i < POPULATION_SIZE; i++)
                P.add(new Chromosome(trainingSet, random, CHOICE_PARAM));

            int lastUpdateGeneration = 0; //Last generation A was updated

            //Loop for generations
            for (int generation = 0; generation < TOTAL_GENERATIONS; generation++)
            {
                //Compute objective values for each chromosome
                for (Chromosome individual : P)
                    individual.ComputeObjectiveValues(validationSet);

                //Update A with solutions in P that are nondominated by solutions in A
                for (int i = P.size()-1; i >= 0; i--)
                {
                    Chromosome chromoP = P.get(i);
                    ArrayList<Chromosome> dominated = new ArrayList<Chromosome>();

                    boolean isChromoPNondominated = true;
                    boolean hasSimilarChromoInA = false;
                    for (Chromosome chromoA : A)
                    {
                        //Determine if P is nondominated by solutions in A
                        if (chromoA.Dominates(chromoP))
                        {
                            isChromoPNondominated = false;
                            break;
                        }
                        //Find solutions in A dominated by P
                        if (chromoP.Dominates(chromoA))
                            dominated.add(chromoA);
                        //Determine if similar chromosome is already in A
                        if (chromoA.GetErrorRate() == chromoP.GetErrorRate()
                                && chromoA.GetComplexity() == chromoP.GetComplexity())
                            hasSimilarChromoInA = true;
                    }

                    //P is nondominated by solutions in A
                    if (isChromoPNondominated && !hasSimilarChromoInA)
                    {
                        //Remove dominated solutions from A
                        A.removeAll(dominated);
                        //Add chromo P to A
                        if (!A.contains(chromoP))
                            A.add(chromoP);

                        lastUpdateGeneration = generation;
                    }
                }
                //Remove duplicates from P (already in A)
                P.removeAll(A);

                ArrayList<Chromosome> newP = new ArrayList<Chromosome>();
                ArrayList<Chromosome> PA = new ArrayList<Chromosome>();
                PA.addAll(P);
                PA.addAll(A);

                //Compute the strength value for each chromosome
                for (Chromosome chromo : PA)
                    chromo.ComputeStrengthValue(PA);

                //Compute the fitness for each chromosome
                for (Chromosome chromo : PA)
                    chromo.ComputeFitness(PA);

                //Select parents and apply genetic operators
                for (int individual = 0; individual < POPULATION_SIZE; individual++)
                {
                    Chromosome parentA = new Chromosome(GetChromosome(PA, random));
                    Chromosome parentB = new Chromosome(GetChromosome(PA, random));

                    //Prune categories
                    parentA.DoPrune(random);
                    parentB.DoPrune(random);

                    //Mutation
                    parentA.DoMutation(random, MUTATION_PARAM);
                    parentB.DoMutation(random, MUTATION_PARAM);

                    //Crossover
                    newP.add(Chromosome.DoCrossover(random, CROSSOVER_TYPE, parentA, parentB, CHOICE_PARAM));
                }

                //Copy best chromosomes in terms of error and complexity to new population
                Chromosome bestErrorRateChromo = PA.get(1);
                Chromosome bestComplexityChromo = PA.get(1);
                for (Chromosome chromo : PA)
                {
                    if (chromo.GetErrorRate() < bestErrorRateChromo.GetErrorRate())
                        bestErrorRateChromo = chromo;
                    if (chromo.GetComplexity() < bestComplexityChromo.GetComplexity())
                        bestComplexityChromo = chromo;
                }
                newP.add(bestErrorRateChromo);
                newP.add(bestComplexityChromo);

                //Copy temporary population
                P = newP;

                //Break if A is not updated for 10 consecutive generations
                if (generation - lastUpdateGeneration > 10)
                    break;
            }

            System.out.println("---- Run: " + run + " - Results for Validation Set ----");
            PrintResults(A);
            System.out.println();

            //Testing data
            DataReader testingDataReader = new DataReader(DATA, DataReader.Type.TESTING);
            Map<Double[], Integer> testingSet = testingDataReader.GetInputMap();

            //Compute objective values for each chromosome
            for (Chromosome chromo : A)
                chromo.ComputeObjectiveValues(testingSet);

            System.out.println("---- Run: " + run + " - Results for Testing Set ----");
            PrintResults(A);
            System.out.println();

            //Save archive
            resultsMap.put(run, A);
        }
        //Results for all runs
        System.out.println("---- Results for " + TOTAL_RUNS + " Runs ----");
        System.out.println(String.format(" %5s%6s%6s%6s%6s", "size", "max", "min", "avg", "count"));
        for (int complexity = 1; complexity < 10; complexity++)
        {
            double min = Double.MAX_VALUE, max = 0, avg = 0;
            int count = 0;
            for (Map.Entry<Integer, ArrayList<Chromosome>> entry : resultsMap.entrySet())
            {
                Chromosome chromo = null;
                //Find chromosome with corresponding complexity
                for (Chromosome c : entry.getValue())
                    if (c.GetComplexity() == complexity)
                        chromo = c;
                if (chromo != null)
                {
                    max = Math.max(max, chromo.GetErrorRate());
                    min = Math.min(min, chromo.GetErrorRate());
                    avg += chromo.GetErrorRate();
                    count++;
                }
            }
            if (count > 0)
                System.out.println(String.format(" %5d %1.3f %1.3f %1.3f%6d", complexity, max, min, avg/(double)count, count));
        }
    }

    private static void PrintResults(ArrayList<Chromosome> chromos)
    {
        //Print chromosomes in A in increasing order of complexity
        for (int complexity = 0; complexity < 100; complexity++)
        {
            for (Chromosome chromo : chromos)
            {
                if (chromo.GetComplexity() == complexity)
                    System.out.println(" Complexity=" + chromo.GetComplexity() + " ErrorRate=" + chromo.GetErrorRate());
            }
        }
    }

    private static Chromosome GetChromosome(ArrayList<Chromosome> chromosomes, Random random)
    {
        //Randomly select two chromosomes
        Chromosome randomChromoA = chromosomes.get(random.nextInt(chromosomes.size()));
        Chromosome randomChromoB = chromosomes.get(random.nextInt(chromosomes.size()));

        //Select chromosome with the smallest fitness
        if (randomChromoA.GetFitness() < randomChromoB.GetFitness())
            return randomChromoA;

        return randomChromoB;
    }
}
