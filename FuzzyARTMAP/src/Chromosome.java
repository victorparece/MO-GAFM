import java.util.*;

/**
 * Created by vic on 3/26/14.
 */
public class Chromosome
{
    private FuzzyARTMAP fartmap;

    private double errorRate;
    private int complexity;

    /**
     * Constructor
     */
    public Chromosome(Map<Double[], Integer> input, Random random)
    {
        //Random baseline vigilance between 0.1 and 0.95
        double baselineVigilance = 0.1 + (0.95 - 0.1) * random.nextDouble();

        //Randomly ordered input
        Map<Double[], Integer> shuffledMap = new LinkedHashMap<Double[], Integer>();
        ArrayList<Double[]> shuffledList = new ArrayList<Double[]>(input.keySet());
        Collections.shuffle(shuffledList);
        for (Double[] key : shuffledList)
            shuffledMap.put(key, input.get(key));

        fartmap = new FuzzyARTMAP(input, 0.01, baselineVigilance);
    }

    /**
     * Compute fitness for individual (error rate and complexity)
     * @param validationSet
     */
    public void ComputeFitness(Map<Double[], Integer> validationSet)
    {
        errorRate = fartmap.Validate(validationSet);
        complexity = fartmap.Size();
    }

}
