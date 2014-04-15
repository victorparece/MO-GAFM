import java.util.Map;

/**
 * Created by vic on 4/3/14.
 */
public class Node implements Comparable<Node>
{
    private int label;
    private Double[] pattern;

    private double activation;
    private int accuracyCount;
    private int frequencyCount;
    private double accuracy;
    private double frequency;

    public Node(Double[] pattern, int label)
    {
        this.pattern = pattern;
        this.label = label;
    }

    public Node(Node node)
    {
        label = node.GetLabel();
        pattern = node.GetPattern();
        activation = node.GetActivation();
        accuracyCount = node.GetAccuracyCount();
        frequencyCount = node.GetFrequencyCount();
        accuracy = node.GetAccuracy();
        frequency = node.GetFrequency();
    }

    public int compareTo(Node b)
    {
        return Double.compare(b.GetActivation(), this.activation);
    }

    @Override
    public boolean equals(Object b)
    {
        if (b == null) return false;
        if (!(b instanceof Node)) return false;

        for (int i = 0; i < ((Node)b).GetPattern().length; i++)
            if (((Node)b).GetPattern()[i].compareTo(pattern[i]) != 0)
                return false;

        return ((Node)b).GetLabel() == label;
    }

    @Override
    public int hashCode()
    {
        return pattern.hashCode() + label;
    }

    public Double[] GetPattern()
    {
        return pattern;
    }

    public int GetLabel()
    {
        return label;
    }

    public double GetActivation()
    {
        return activation;
    }

    public void SetAccuracy(double accuracy)
    {
        this.accuracy = accuracy;
    }

    public double GetAccuracy()
    {
        return accuracy;
    }

    public int GetAccuracyCount()
    {
        return accuracyCount;
    }

    public void IncrementAccuracyCount()
    {
        accuracyCount++;
    }

    public void SetFrequency(double frequency)
    {
        this.frequency = frequency;
    }

    public double GetFrequency()
    {
        return frequency;
    }

    public int GetFrequencyCount()
    {
        return frequencyCount;
    }

    public void IncrementFrequencyCount()
    {
        frequencyCount++;
    }

    public double GetConfidenceFactor()
    {
        return 0.5*accuracy + 0.5*frequency;
    }

    public void ComputeActivation(Map.Entry<Double[], Integer> input, double choiceParam)
    {
        activation = FuzzyMath.sum(FuzzyMath.fuzzyMin(input.getKey(), pattern))/(choiceParam + FuzzyMath.sum(pattern));
    }

    public void SetActivation(double newActivation)
    {
        this.activation = newActivation;
    }

    public void Reset()
    {
        activation = 0;
        accuracyCount = 0;
        frequencyCount = 0;
        accuracy = 0;
        frequency = 0;
    }
}
