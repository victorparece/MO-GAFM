import java.util.*;

/**
 * Created by vic on 3/16/14.
 */
public class FuzzyARTMAP
{
    private double baselineVigilenceParam;
    private double choiceParam;

    private ArrayList<Node> nodes = new ArrayList<Node>();

    public FuzzyARTMAP(Map<Double[], Integer> inputPatterns, double choiceParam, double baselineVigilenceParam)
    {
        this.choiceParam = choiceParam;
        this.baselineVigilenceParam = baselineVigilenceParam;

        boolean weightsUpdated = true;

        //Loop until the weights are not updated
        while (weightsUpdated)
        {
            weightsUpdated = false;

            //Loop for input/output pairs
            for (Map.Entry<Double[], Integer> input : inputPatterns.entrySet())
            {
                double vigilanceParam = baselineVigilenceParam;

                //Compute activation for committed nodes
                ArrayList<Node> sortedActivationList = GetActivation(input);

                //Select node with highest activation and do vigilance/label tests
                Double[] selectedNode = null;
                for (Node node : sortedActivationList)
                {
                    //Compute vigilance
                    double vigilance = FuzzyMath.sum(FuzzyMath.fuzzyMin(input.getKey(), node.GetPattern()))/FuzzyMath.sum(input.getKey());

                    //Committed Node
                    if (nodes.contains(node))
                    {
                        //Vigilance and Label tests
                        if (vigilance >= vigilanceParam)
                        {
                            if (input.getValue().equals(node.GetLabel()))
                            {
                                Node newNode = new Node(FuzzyMath.fuzzyMin(input.getKey(), node.GetPattern()), node.GetLabel());

                                //New node is not already in nodes
                                if (!nodes.contains(newNode))//!ContainsKey(newNode))
                                {
                                    nodes.remove(node);
                                    nodes.add(newNode);

                                    weightsUpdated = true;
                                }
                                //New node is already in nodes, do nothing

                                break;
                            }
                            //Label test failed, increase vigilance
                            else
                            {
                                vigilanceParam = FuzzyMath.sum(FuzzyMath.fuzzyMin(input.getKey(), node.GetPattern()))/FuzzyMath.sum(input.getKey());
                            }
                        }
                    }
                    //Uncommitted Node
                    else
                    {
                        nodes.add(new Node(input.getKey(), input.getValue()));

                        weightsUpdated = true;
                        break;
                    }

                    //Disqualify node
                    node.SetActivation(-1.0);
                }
            }
        }
    }

    public FuzzyARTMAP(FuzzyARTMAP fartmap)
    {
        baselineVigilenceParam = fartmap.GetBaselineVigilanceParam();
        choiceParam = fartmap.GetChoiceParam();

        for (Node node : fartmap.GetNodes())
            nodes.add(new Node(node));
    }

    public FuzzyARTMAP(ArrayList<Node> nodes, double choiceParam, double baselineVigilenceParam)
    {
        this.choiceParam = choiceParam;
        this.baselineVigilenceParam = baselineVigilenceParam;
        this.nodes = nodes;
    }

    /**
     * Calculates the bottom-up inputs for all of the nodes in the Fuzzy ARTMAP
     * @param input
     * @return
     */
    public ArrayList<Node> GetActivation(Map.Entry<Double[], Integer> input)
    {
        ArrayList<Node> activationList = new ArrayList<Node>();

        //Compute activation for uncommitted nodes
        for (Node node : nodes)
        {
            node.ComputeActivation(input, choiceParam);
            activationList.add(node);
        }

        //Add uncommitted node
        Node uncommittedNode = new Node(input.getKey(), input.getValue());
        uncommittedNode.SetActivation(FuzzyMath.sum(input.getKey())/(choiceParam + FuzzyMath.sum(GetAllOnes(input.getKey().length))));
        //uncommittedNode.ComputeActivation(input, choiceParam);
        activationList.add(uncommittedNode);

        Collections.sort(activationList);

        return activationList;
    }

    /**
     * Returns the all ones vector
     * @param length
     * @return
     */
    private Double[] GetAllOnes(int length)
    {
        Double[] result = new Double[length];
        for (int i = 0; i < length; i++)
            result[i] = 1.0;
        return result;
    }

    public static void main(String[ ] args)
    {
        Map<Double[], Integer> input = new LinkedHashMap<Double[], Integer>();

        //TODO: Automatically compute complement

        input.put(new Double[] {0.3, 0.2, 0.7, 0.8}, new Integer(1));
        input.put(new Double[] {0.6, 0.7, 0.4, 0.3}, new Integer(1));
        input.put(new Double[] {0.2, 0.1, 0.8, 0.9}, new Integer(1));
        input.put(new Double[] {0.25, 0.25, 0.75, 0.75}, new Integer(2));
        input.put(new Double[] {0.8, 0.85, 0.2, 0.15}, new Integer(1));
        input.put(new Double[] {0.28, 0.5, 0.72, 0.5}, new Integer(2));

        FuzzyARTMAP fartmap = new FuzzyARTMAP(input, 0.01, 0.7);
    }



    /**
     * Compute the error rate of the Fuzzy ARTMAP using the validation set
     * @param validationSet
     * @return
     */
    public double Validate(Map<Double[], Integer> validationSet)
    {
        int correctCount = 0;

        for (Node node : nodes)
            node.Reset();

        //Loop for input/output pairs
        for (Map.Entry<Double[], Integer> input : validationSet.entrySet())
        {
            double vigilanceParam = baselineVigilenceParam;

            //Compute activation for committed nodes
            ArrayList<Node> sortedActivationList = GetActivation(input);

            //Select node with highest activation and do vigilance tests
            for (Node node : sortedActivationList)
            {
                //Compute vigilance
                double vigilance = FuzzyMath.sum(FuzzyMath.fuzzyMin(input.getKey(), node.GetPattern()))/FuzzyMath.sum(input.getKey());

                //Committed Node
                if (nodes.contains(node))
                {
                    //Vigilance test
                    if (vigilance >= vigilanceParam)
                    {
                        //Node selected, increment frequency
                        node.IncrementFrequencyCount();

                        //Selected node label matches input, increment accuracy
                        if (input.getValue() == node.GetLabel())
                            node.IncrementAccuracyCount();

                        break;
                    }
                }
                //Uncommitted Node, do nothing

                //Disqualify node
                node.SetActivation(-1.0);
            }
        }

        //Find max accuracy and frequency for each label
        Map<Integer, Integer> maxAccuracyMap = new LinkedHashMap<Integer, Integer>();
        Map<Integer, Integer> maxFrequencyMap = new LinkedHashMap<Integer, Integer>();
        for (Node node : nodes)
        {
            Integer maxAccuracy = 0;
            if (maxAccuracyMap.containsKey(node.GetLabel()))
                maxAccuracy = maxAccuracyMap.get(node.GetLabel());
            maxAccuracyMap.put(node.GetLabel(), Math.max(maxAccuracy, node.GetAccuracyCount()));

            Integer maxFrequency = 0;
            if (maxFrequencyMap.containsKey(node.GetLabel()))
                maxFrequency = maxFrequencyMap.get(node.GetLabel());
            maxFrequencyMap.put(node.GetLabel(), Math.max(maxFrequency, node.GetFrequencyCount()));
        }

        //Compute metrics
        for (Node node : nodes)
        {
            correctCount += node.GetAccuracyCount();

            int maxAccuracyCount = maxAccuracyMap.get(node.GetLabel());
            int maxFrequencyCount = maxFrequencyMap.get(node.GetLabel());

            if (maxAccuracyCount > 0)
                node.SetAccuracy((double)node.GetAccuracyCount() / (double)maxAccuracyCount);
            if (maxFrequencyCount > 0)
                node.SetFrequency((double)node.GetFrequencyCount() / (double)maxFrequencyCount);
        }

        if ((double)correctCount > (double)validationSet.size())
            correctCount = 0;

        return 1 - (double)correctCount/(double)validationSet.size();
    }

    /**
     * Returns the number of nodes in the category representation layer of the Fuzzy ARTMAP
     * @return
     */
    public int Size()
    {
        return nodes.size();
    }

    public ArrayList<Node> GetNodes()
    {
        return nodes;
    }

    public double GetBaselineVigilanceParam()
    {
        return baselineVigilenceParam;
    }

    public double GetChoiceParam()
    {
        return choiceParam;
    }
}
