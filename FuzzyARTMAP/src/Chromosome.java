import java.awt.*;
import java.util.*;

/**
 * Created by vic on 3/26/14.
 */
public class Chromosome implements Comparable<Chromosome>
{
    private FuzzyARTMAP fartmap;

    private double errorRate;
    private int complexity;
    private int strengthValue;
    private double fitness;

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
     * Compute objective values for individual (error rate and complexity)
     * @param validationSet
     */
    public void ComputeObjectiveValues(Map<Double[], Integer> validationSet)
    {
        errorRate = fartmap.Validate(validationSet);
        complexity = fartmap.Size();
    }

    public double GetErrorRate()
    {
        return errorRate;
    }

    public int GetComplexity()
    {
        return complexity;
    }

    public int GetStrengthValue()
    {
        return strengthValue;
    }

    public double GetFitness()
    {
        return fitness;
    }

    public boolean Dominates(Chromosome b)
    {
        return GetComplexity() < b.GetComplexity() && GetErrorRate() < b.GetErrorRate();
    }

    /**
     * Compute the strength value for this chromosome (Number of solutions this chromosome dominates)
     * @param chromos
     * @return
     */
    public void ComputeStrengthValue(ArrayList<Chromosome> chromos)
    {
        strengthValue = 0;
        for (Chromosome chromo : chromos)
            if (this.Dominates(chromo))
                strengthValue++;
    }

    public void ComputeFitness(ArrayList<Chromosome> chromos)
    {
        double rawFitness = 0;
        int k = (int)Math.round(Math.sqrt(chromos.size()));
        ArrayList<Double> chromoDistances = new ArrayList<Double>();

        for (Chromosome chromo : chromos)
        {
            //Update raw fitness
            if (chromo.Dominates(this))
                rawFitness += chromo.GetStrengthValue();

            //Compute distance in objective space
            chromoDistances.add(Point.distance(errorRate, complexity, chromo.GetErrorRate(), chromo.GetComplexity()));
        }

        //Sort //TODO: Make sure this is ascending order
        Collections.sort(chromoDistances);

        fitness = rawFitness + 1/(chromoDistances.get(k) + 2);
    }

    @Override
    public int compareTo(Chromosome b)
    {
        return new Double(fitness).compareTo(b.GetFitness());
    }
}
