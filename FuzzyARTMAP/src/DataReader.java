import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by vic on 4/4/14.
 */
public class DataReader
{
    private static String FILE_EXTENSION = ".csv";
    private static String DELIM = "-";

    Map<Double[], Integer> inputMap = new LinkedHashMap<Double[], Integer>();

    public enum Type
    {
        TRAINING, VALIDATION, TESTING
    }

    public DataReader(String prefix, Type suffix)
    {
        String suffixString = "";
        switch (suffix)
        {
            case TRAINING:
                suffixString  = "training" + FILE_EXTENSION;
                break;
            case VALIDATION:
                suffixString = "validation" + FILE_EXTENSION;
                break;
            case TESTING:
                suffixString = "testing" + FILE_EXTENSION;
                break;
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(prefix + DELIM + suffixString));
            StringBuilder sb = new StringBuilder();

            //Read file into arraylist
            String line = br.readLine();
            while (line != null)
            {
                String[] parts = line.split("\\s*,\\s*");

                Double[] pattern = new Double[2];
                pattern[0] = new Double(parts[0]);
                pattern[1] = new Double(parts[1]);

                Integer label = new Integer(parts[2]);

                inputMap.put(pattern, label);

                line = br.readLine();
            }
        } catch (IOException e)
        {
            System.out.println("IOException occurred while reading " + prefix + DELIM + suffixString + ".");
            System.exit(1);
        }
    }

    public Map<Double[], Integer> GetInputMap()
    {
        return inputMap;
    }
}
