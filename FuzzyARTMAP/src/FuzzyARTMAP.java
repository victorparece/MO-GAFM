import java.util.*;

/**
 * Created by vic on 3/16/14.
 */
public class FuzzyARTMAP
{
    private double baselineVigilenceParam;
    private double choiceParam;

    private Map<Double[], Integer> nodes = new LinkedHashMap<Double[], Integer>();

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
                Map<Double[], Double> sortedActivationMap = GetActivation(input);

                //Select node with highest activation and do vigilance/label tests
                Double[] selectedNode = null;
                for (Map.Entry<Double[], Double> node : sortedActivationMap.entrySet())
                {
                    //Compute vigilance
                    double vigilance = sum(fuzzyMin(input.getKey(), node.getKey()))/sum(input.getKey());

                    //Committed Node
                    if (nodes.containsKey(node.getKey()))
                    {
                        //Vigilance and Label tests
                        if (vigilance >= vigilanceParam)
                        {
                            if (input.getValue().equals(nodes.get(node.getKey())))
                            {
                                Double[] newNode = fuzzyMin(input.getKey(), node.getKey());

                                //New node is not already in nodes
                                if (!ContainsKey(newNode))
                                {
                                    nodes.remove(node.getKey());
                                    nodes.put(newNode, input.getValue());

                                    weightsUpdated = true;
                                }
                                //New node is already in nodes, do nothing

                                break;
                            }
                            //Label test failed, increase vigilance
                            else
                            {
                                vigilanceParam = sum(fuzzyMin(input.getKey(), node.getKey()))/sum(input.getKey());
                            }
                        }
                    }
                    //Uncommitted Node
                    else
                    {
                        nodes.put(input.getKey(), input.getValue());

                        weightsUpdated = true;
                        break;
                    }

                    //Disqualify node
                    node.setValue(-1.0);
                }
            }
        }
    }

    /**
     * Calculates the bottom-up inputs for all of the nodes in the Fuzzy ARTMAP
     * @param input
     * @return
     */
    public Map<Double[], Double> GetActivation(Map.Entry<Double[], Integer> input)
    {
        Map<Double[], Double> activationMap = new LinkedHashMap<Double[], Double>();

        //Compute activation for uncommitted nodes
        for (Double[] node : nodes.keySet())
        {
            double activation = sum(fuzzyMin(input.getKey(), node))/(choiceParam + sum(node));
            activationMap.put(node, activation);
        }

        //Add uncommitted node
        activationMap.put(input.getKey(), sum(input.getKey())/(choiceParam + sum(GetAllOnes(input.getKey().length))));

        List<Map.Entry<Double[], Double>> entries = new ArrayList<Map.Entry<Double[], Double>>(activationMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Double[], Double>>() {
            public int compare(Map.Entry<Double[], Double> a, Map.Entry<Double[], Double> b){
                return b.getValue().compareTo(a.getValue());
            }
        });
        Map<Double[], Double> sortedActivationMap = new LinkedHashMap<Double[], Double>();
        for (Map.Entry<Double[], Double> entry : entries)
            sortedActivationMap.put(entry.getKey(), entry.getValue());

        return sortedActivationMap;
    }

    /**
     * Returns true if the map contains the key (this method uses equals for comparison)
     */
    public boolean ContainsKey(Double[] comparisonKey)
    {
        for (Double[] key : nodes.keySet())
        {
            boolean match = true;
            for (int i = 0; i < key.length; i++)
                if (comparisonKey[i].compareTo(key[i]) != 0)
                    match = false;

            if (match) return true;
        }
        return false;
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
     * Compute the fuzzy mean of two arrays
     * @param a
     * @param b
     * @return
     */
    private Double[] fuzzyMin(Double[] a, Double[] b)
    {
        int maxLength = Math.max(a.length, b.length);
        Double[] result = new Double[maxLength];

        for (int i = 0; i < maxLength; i++)
            if (i <= a.length && i <= b.length)
                result[i] = Math.min(a[i], b[i]);
            else if (i > a.length)
                result[i] = b[i];
            else if (i > b.length)
                result[i] = a[i];

        return result;
    }

    /**
     * Sums all of the elements of an array
     * @param a
     * @return
     */
    private double sum(Double[] a)
    {
        double result = 0;

        for (int i = 0; i < a.length; i++)
            result += a[i];

        return result;
    }

    /**
     * Gets the complement of each element in the array
     * @param a
     * @return
     */
    private Double[] complement(Double[] a)
    {
        Double[] result = new Double[a.length];
        for (int i = 0; i < a.length; i++)
            a[i] = 1 - a[i];

        return result;
    }

    /**
     * Compute the error rate of the Fuzzy ARTMAP using the validation set
     * @param validationSet
     * @return
     */
    public double Validate(Map<Double[], Integer> validationSet)
    {
        double correctCount = 0;
        Map<Double[], Integer> result = new LinkedHashMap<Double[], Integer>();

        //Loop for input/output pairs
        for (Map.Entry<Double[], Integer> input : validationSet.entrySet())
        {
            double vigilanceParam = baselineVigilenceParam;

            //Compute activation for committed nodes
            Map<Double[], Double> sortedActivationMap = GetActivation(input);

            //Select node with highest activation and do vigilance tests
            for (Map.Entry<Double[], Double> node : sortedActivationMap.entrySet())
            {
                //Compute vigilance
                double vigilance = sum(fuzzyMin(input.getKey(), node.getKey()))/sum(input.getKey());

                //Committed Node
                if (nodes.containsKey(node.getKey()))
                {
                    //Vigilance test
                    if (vigilance >= vigilanceParam)
                        result.put(input.getKey(), nodes.get(node.getKey()));
                }
                //Uncommitted Node
                else
                {
                    //Unknown
                    result.put(input.getKey(), -1);
                    break;
                }

                //Disqualify node
                node.setValue(-1.0);
            }

            //Increment number of correct response counter if validation matches computed
            if (result.get(input).equals(validationSet.get(input)))
                correctCount += 1;
        }

        return correctCount/validationSet.size();
    }

    /**
     * Returns the number of nodes in the category representation layer of the Fuzzy ARTMAP
     * @return
     */
    public int Size()
    {
        return nodes.size();
    }
}
