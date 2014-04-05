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

    private static double CHOICE_PARAM = 0.01;

    /**
     * Constructor
     */
    public Chromosome(Map<Double[], Integer> input, Random random)
    {
        //Random baseline vigilance between 0.1 and 0.95
        double baselineVigilance = GetRandomBaselineVigilanceParameter(random);

        //Randomly ordered input
        Map<Double[], Integer> shuffledMap = new LinkedHashMap<Double[], Integer>();
        ArrayList<Double[]> shuffledList = new ArrayList<Double[]>(input.keySet());
        Collections.shuffle(shuffledList);
        for (Double[] key : shuffledList)
            shuffledMap.put(key, input.get(key));

        fartmap = new FuzzyARTMAP(input, CHOICE_PARAM, baselineVigilance);
    }

    public Chromosome(Chromosome chromo)
    {
        fartmap = new FuzzyARTMAP(chromo.GetFuzzyARTMAP());
        errorRate = chromo.GetErrorRate();
        complexity = chromo.GetComplexity();
        strengthValue = chromo.GetStrengthValue();
        fitness = chromo.GetFitness();
    }

    public Chromosome(ArrayList<Node> nodes, Random random)
    {
        //Random baseline vigilance between 0.1 and 0.95
        double baselineVigilance = GetRandomBaselineVigilanceParameter(random);

        fartmap = new FuzzyARTMAP(nodes, CHOICE_PARAM, baselineVigilance);
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
        if (GetComplexity() == b.GetComplexity() && GetErrorRate() < b.GetErrorRate())
            return GetErrorRate() < b.GetErrorRate();
        else if (GetComplexity() < b.GetComplexity() && GetErrorRate() == b.GetErrorRate())
            return GetComplexity() < b.GetComplexity();

        return (GetComplexity() < b.GetComplexity() && GetErrorRate() < b.GetErrorRate());
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

        //Sort
        Collections.sort(chromoDistances);

        fitness = rawFitness + 1.0/(chromoDistances.get(k) + 2.0);
    }

    @Override
    public int compareTo(Chromosome b)
    {
        return new Double(fitness).compareTo(b.GetFitness());
    }

    public FuzzyARTMAP GetFuzzyARTMAP()
    {
        return fartmap;
    }

    public void DoPrune(Random random)
    {
        ArrayList<Node> nodes = fartmap.GetNodes();
        for (int i = nodes.size()-1; i >= 0; i--)
        {
            Node node = nodes.get(i);
            if (random.nextDouble() < 1.0 - node.GetConfidenceFactor())
                nodes.remove(node);
        }
    }

    public void DoMutation(Random random)
    {
        for (Node node : fartmap.GetNodes())
        {
            double rand = random.nextDouble();
            Double[] d = node.GetPattern();
            int start, end;

            if (rand < 0.5)
            {
                start = 0;
                end = node.GetPattern().length/2;
            }
            else
            {
                start = node.GetPattern().length/2;
                end = node.GetPattern().length;
            }

            for (int i = start; i < end; i++)
            {
                d[i] += random.nextGaussian() * 0.05*(1-node.GetConfidenceFactor());
                if (d[i] < 0.0)
                    d[i] = 0.0;
                else if (d[i] > 1.0)
                    d[i] = 1.0;
            }
        }
    }

    public static Chromosome DoCrossover(Random random, Chromosome chromoA, Chromosome chromoB)
    {
        ArrayList<Node> chromoANodes = chromoA.GetFuzzyARTMAP().GetNodes();
        ArrayList<Node> chromoBNodes = chromoB.GetFuzzyARTMAP().GetNodes();

        int crossoverPointA = 0;
        if (chromoANodes.size() > 0)
            crossoverPointA = random.nextInt(chromoANodes.size());
        int crossoverPointB = 0;
        if (chromoBNodes.size() > 0)
            crossoverPointB = random.nextInt(chromoBNodes.size());

        ArrayList<Node> newChromoNodes = new ArrayList<Node>();

        if (chromoANodes.size() == 0)
            chromoANodes.size();

        //Copy from start of chromo A to the crossover point
        for (int i = 0; i <= crossoverPointA; i++)
            newChromoNodes.add(chromoANodes.get(i));

        //Copy from crossover point to end of chromo B
        for (int i = crossoverPointB; i < chromoBNodes.size(); i++)
            newChromoNodes.add(chromoBNodes.get(i));

        return new Chromosome(newChromoNodes, random);
    }

    public double GetRandomBaselineVigilanceParameter(Random random)
    {
        return 0.1 + (0.95 - 0.1) * random.nextDouble();
    }
}
