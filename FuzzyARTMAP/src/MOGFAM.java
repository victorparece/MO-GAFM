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

    private static String DATA = "data/g4c_25";
    //private static String DATA = "data/g6c_15";
    //private static String DATA = "data/Iris5000";

    public static void main(String[ ] args)
    {
        ArrayList<Chromosome> P = new ArrayList<Chromosome>();
        ArrayList<Chromosome> A = new ArrayList<Chromosome>();

        Random random = new Random(1);

        //Training data
        DataReader trainingDataReader = new DataReader(DATA, DataReader.Type.TRAINING);
        Map<Double[], Integer> trainingSet = trainingDataReader.GetInputMap();

        //Validation data
        DataReader validationDataReader = new DataReader(DATA, DataReader.Type.VALIDATION);
        Map<Double[], Integer> validationSet = validationDataReader.GetInputMap();

//        Map<Double[], Integer> trainingSet = new LinkedHashMap<Double[], Integer>();
//        trainingSet.put(new Double[]{0.3, 0.2, 0.7, 0.8}, new Integer(1));
//        trainingSet.put(new Double[]{0.6, 0.7, 0.4, 0.3}, new Integer(1));
//        trainingSet.put(new Double[]{0.2, 0.1, 0.8, 0.9}, new Integer(1));
//        trainingSet.put(new Double[]{0.25, 0.25, 0.75, 0.75}, new Integer(2));
//        trainingSet.put(new Double[]{0.8, 0.85, 0.2, 0.15}, new Integer(1));
//        trainingSet.put(new Double[]{0.28, 0.5, 0.72, 0.5}, new Integer(2));

//        Map<Double[], Integer> validationSet = new LinkedHashMap<Double[], Integer>();
//        validationSet.put(new Double[]{0.2, 0.2, 0.7, 0.5}, new Integer(1));
//        validationSet.put(new Double[]{0.4, 0.8, 0.4, 0.8}, new Integer(1));
//        validationSet.put(new Double[]{0.8, 0.1, 0.8, 0.9}, new Integer(1));
//        validationSet.put(new Double[]{0.3, 0.3, 0.75, 0.75}, new Integer(2));
//        validationSet.put(new Double[]{0.9, 0.85, 0.5, 0.15}, new Integer(2));
//        validationSet.put(new Double[]{0.3, 0.5, 0.72, 0.3}, new Integer(2));

        //Generate initial population
        for (int i = 0; i < POPULATION_SIZE; i++)
            P.add(new Chromosome(trainingSet, random));

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
                }

                //P is nondominated by solutions in A
                if (isChromoPNondominated)
                {
                    //Remove dominated solutions from A
                    A.removeAll(dominated);
                    //Add chromo P to A
                    if (!A.contains(chromoP))
                        A.add(chromoP);
                    //Remove chromo P from P
                    P.remove(chromoP);

                    lastUpdateGeneration = generation;
                }
            }


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
            for (int parent = 0; parent < POPULATION_SIZE; parent++)
            {
                Chromosome parentTest = GetChromosome(PA, random);

                Chromosome parentA = new Chromosome(parentTest);
                Chromosome parentB = new Chromosome(GetChromosome(PA, random));

                //Prune categories
                parentA.DoPrune(random);
                parentB.DoPrune(random);

                //Mutation
                parentA.DoMutation(random);
                parentB.DoMutation(random);

                //Crossover
                newP.add(Chromosome.DoCrossover(random, parentA, parentB));
            }

            //Copy new population
            P = newP;

            //Stop if A is not updated for 10 consecutive generations
            if (generation - lastUpdateGeneration > 10)
                break;
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
