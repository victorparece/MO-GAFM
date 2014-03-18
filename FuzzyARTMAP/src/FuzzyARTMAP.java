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
        double vigilanceParam = baselineVigilenceParam;

        //Loop until the weights are not updated
        while (weightsUpdated)
        {
            weightsUpdated = false;

            //Loop for input/output pairs
            for (Map.Entry<Double[], Integer> input : inputPatterns.entrySet())
            {
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

                                nodes.remove(node);
                                nodes.put(newNode, input.getValue());

                                weightsUpdated = true;
                                break;
                            }
                            //Label test failed, increase vigilance
                            else
                            {
                                vigilance = sum(fuzzyMin(input.getKey(), node.getKey()))/sum(input.getKey());
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
}
