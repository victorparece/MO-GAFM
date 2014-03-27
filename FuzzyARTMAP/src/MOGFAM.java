import java.util.*;

/**
 * Created by vic on 3/26/14.
 */
public class MOGFAM
{
    private static int POPULATION_SIZE = 20;
    private static int TOTAL_GENERATIONS = 500;

    public static void main(String[ ] args)
    {
        ArrayList<Chromosome> P = new ArrayList<Chromosome>();
        ArrayList<Chromosome> A = new ArrayList<Chromosome>();

        Random random = new Random(1);

        //Training data
        Map<Double[], Integer> trainingSet = new LinkedHashMap<Double[], Integer>();
        trainingSet.put(new Double[]{0.3, 0.2, 0.7, 0.8}, new Integer(1));
        trainingSet.put(new Double[]{0.6, 0.7, 0.4, 0.3}, new Integer(1));
        trainingSet.put(new Double[]{0.2, 0.1, 0.8, 0.9}, new Integer(1));
        trainingSet.put(new Double[]{0.25, 0.25, 0.75, 0.75}, new Integer(2));
        trainingSet.put(new Double[]{0.8, 0.85, 0.2, 0.15}, new Integer(1));
        trainingSet.put(new Double[]{0.28, 0.5, 0.72, 0.5}, new Integer(2));

        //Validation data
        Map<Double[], Integer> validationSet = new LinkedHashMap<Double[], Integer>();
        validationSet.put(new Double[]{0.2, 0.2, 0.7, 0.5}, new Integer(1));
        validationSet.put(new Double[]{0.4, 0.8, 0.4, 0.8}, new Integer(1));
        validationSet.put(new Double[]{0.8, 0.1, 0.8, 0.9}, new Integer(1));
        validationSet.put(new Double[]{0.3, 0.3, 0.75, 0.75}, new Integer(2));
        validationSet.put(new Double[]{0.9, 0.85, 0.5, 0.15}, new Integer(2));
        validationSet.put(new Double[]{0.3, 0.5, 0.72, 0.3}, new Integer(2));

        //Generate initial population
        for (int i = 0; i < POPULATION_SIZE; i++)
            P.add(new Chromosome(trainingSet, random));

        int lastUpdateGeneration; //Last generation A was updated

        //Loop for generations
        for (int generation = 0; generation < TOTAL_GENERATIONS; generation++)
        {
            //Evaluate fitness according to objective function
            for (Chromosome individual : P)
                individual.ComputeFitness(validationSet);

            //Update A with solutions in P that are nondominated by solutions in A
            //TODO
            lastUpdateGeneration = generation;

            //Stop if A is not updated for 10 consecutive generations
            if (generation - lastUpdateGeneration > 10)
                break;
        }
    }
}
