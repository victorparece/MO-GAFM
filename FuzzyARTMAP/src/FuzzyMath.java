/**
 * Created by vic on 4/3/14.
 */
public class FuzzyMath
{
    /**
     * Compute the fuzzy mean of two arrays
     * @param a
     * @param b
     * @return
     */
    public static Double[] fuzzyMin(Double[] a, Double[] b)
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
    public static double sum(Double[] a)
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
    public static Double[] complement(Double[] a)
    {
        Double[] result = new Double[a.length];
        for (int i = 0; i < a.length; i++)
            a[i] = 1 - a[i];

        return result;
    }
}
