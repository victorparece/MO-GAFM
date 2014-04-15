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

    public enum CrossoverType
    {
        Simple, CrossoverLabels
    }

    /**
     * Constructor
     */
    public Chromosome(Map<Double[], Integer> input, Random random, double choiceParam)
    {
        //Random baseline vigilance between 0.1 and 0.95
        double baselineVigilance = GetRandomBaselineVigilanceParameter(random);

        //Randomly ordered input
        Map<Double[], Integer> shuffledMap = new LinkedHashMap<Double[], Integer>();
        ArrayList<Double[]> shuffledList = new ArrayList<Double[]>(input.keySet());
        Collections.shuffle(shuffledList);
        for (Double[] key : shuffledList)
            shuffledMap.put(key, input.get(key));

        fartmap = new FuzzyARTMAP(input, choiceParam, baselineVigilance);
    }

    public Chromosome(Chromosome chromo)
    {
        fartmap = new FuzzyARTMAP(chromo.GetFuzzyARTMAP());
        errorRate = chromo.GetErrorRate();
        complexity = chromo.GetComplexity();
        strengthValue = chromo.GetStrengthValue();
        fitness = chromo.GetFitness();
    }

    public Chromosome(ArrayList<Node> nodes, Random random, double choiceParam)
    {
        //Random baseline vigilance between 0.1 and 0.95
        double baselineVigilance = GetRandomBaselineVigilanceParameter(random);

        fartmap = new FuzzyARTMAP(nodes, choiceParam, baselineVigilance);
    }

    public Chromosome(ArrayList<Node> nodes, double baselineVigilance, double choiceParam)
    {
        fartmap = new FuzzyARTMAP(nodes, choiceParam, baselineVigilance);
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
            return true;
        else if (GetComplexity() < b.GetComplexity() && GetErrorRate() == b.GetErrorRate())
            return true;
        else if ((GetComplexity() < b.GetComplexity() && GetErrorRate() < b.GetErrorRate()))
            return true;

        return false;
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
        ArrayList<Node> deleteList = new ArrayList<Node>();

        //Mark nodes for deletion
        for (Node node : nodes)
            if (random.nextDouble() < 1.0 - node.GetConfidenceFactor())
                deleteList.add(node);

        //Get all nodes that represent each label
        Map<Integer, ArrayList<Node>> nodeLabelMap = GetNodesForLabel(nodes);

        Collections.sort(deleteList, new Comparator<Node>() {
            @Override
            public int compare(Node a, Node b) {
                return Double.compare(a.GetConfidenceFactor(), b.GetConfidenceFactor());
            }
        } );

        //Delete nodes marked for deletion
        for (Node markedNode : deleteList)
        {
            for (int i = nodes.size()-1; i >= 0; i--)
            {
                //Delete marked node if there exists a different node with the same label
                if (markedNode != nodes.get(i) && markedNode.GetLabel() == nodes.get(i).GetLabel())
                {
                    nodes.remove(markedNode);
                    break;
                }
            }
        }
    }

    public void DoMutation(Random random, double mutationParam)
    {
        for (Node node : fartmap.GetNodes())
        {
            Double[] d = node.GetPattern();
            int start, end;

            if (random.nextDouble() < 0.5)
            {
                start = 0;
                end = node.GetPattern().length/2;
            }
            else
            {
                start = node.GetPattern().length/2;
                end = node.GetPattern().length;
            }

            double z = random.nextGaussian() * mutationParam *(1-node.GetConfidenceFactor());
            for (int i = start; i < end; i++)
            {
                d[i] += z;
                if (d[i] < 0.0)
                    d[i] = 0.0;
                else if (d[i] > 1.0)
                    d[i] = 1.0;
            }
        }
    }

    public static Chromosome DoCrossover(Random random, CrossoverType type, Chromosome chromoA, Chromosome chromoB, double choiceParam)
    {
        ArrayList<Node> newChromoNodes = new ArrayList<Node>();

        switch (type)
        {
            case CrossoverLabels:
                //Copy nodes into map based on label, crossover is done separately for each label
                Map<Integer, ArrayList<Node>> chromoALabelMap = GetNodesForLabel(chromoA.GetFuzzyARTMAP().GetNodes());
                Map<Integer, ArrayList<Node>> chromoBLabelMap = GetNodesForLabel(chromoB.GetFuzzyARTMAP().GetNodes());

                //Correctness check
                assert chromoALabelMap.size() == chromoBLabelMap.size();

                //Loop for labels
                for (Map.Entry<Integer, ArrayList<Node>> entry : chromoALabelMap.entrySet())
                {
                    ArrayList<Node> chromoANodesForLabel = entry.getValue();
                    ArrayList<Node> chromoBNodesForLabel = chromoBLabelMap.get(entry.getKey());

                    newChromoNodes.addAll(CombineNodes(random, chromoANodesForLabel, chromoBNodesForLabel));

                    assert newChromoNodes.size() > 0;
                }
                break;
            case Simple:
                newChromoNodes.addAll(CombineNodes(random, chromoA.GetFuzzyARTMAP().GetNodes(), chromoB.GetFuzzyARTMAP().GetNodes()));
                break;
        }

        //Use baseline vigilance from most fit parent
        double baselineVigilance = GetRandomBaselineVigilanceParameter(random);
//        if (chromoA.GetFitness() > chromoB.GetFitness())
//            baselineVigilance = chromoA.GetFuzzyARTMAP().GetBaselineVigilanceParam();
//        else
//            baselineVigilance = chromoB.GetFuzzyARTMAP().GetBaselineVigilanceParam();

        //Create new chromosome from selected nodes and reset nodes
        Chromosome newChromo = new Chromosome(newChromoNodes, baselineVigilance, choiceParam);
        for (Node node : newChromo.GetFuzzyARTMAP().GetNodes())
            node.Reset();

        return newChromo;
    }

    private static ArrayList<Node> CombineNodes(Random random, ArrayList<Node> nodesA, ArrayList<Node> nodesB)
    {
        ArrayList<Node> newNodes = new ArrayList<Node>();

        int crossoverPointA = random.nextInt(nodesA.size());
        int crossoverPointB = random.nextInt(nodesB.size());

        //Copy from start of nodes for A to the crossover point
        for (int i = 0; i < crossoverPointA; i++)
            newNodes.add(nodesA.get(i));

        //Copy from crossover point to end of nodes for B
        for (int i = crossoverPointB; i < nodesB.size(); i++)
            newNodes.add(nodesB.get(i));

        return newNodes;
    }

    private static Map<Integer, ArrayList<Node>> GetNodesForLabel(ArrayList<Node> nodes)
    {
        Map<Integer, ArrayList<Node>> labelMap = new LinkedHashMap<Integer, ArrayList<Node>>();

        for (Node node : nodes)
        {
            ArrayList<Node> currentNodes = new ArrayList<Node>();
            if (labelMap.containsKey(node.GetLabel()))
                currentNodes = labelMap.get(node.GetLabel());
            currentNodes.add(node);
            labelMap.put(node.GetLabel(), currentNodes);
        }
        return labelMap;
    }

    public static double GetRandomBaselineVigilanceParameter(Random random)
    {
        return 0.1 + (0.95 - 0.1) * random.nextDouble();
    }
}
