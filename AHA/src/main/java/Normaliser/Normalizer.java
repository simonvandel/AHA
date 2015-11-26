package Normaliser;

import Communication.SensorState;
import Communication.SensorValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Created by Zobair on 20-11-2015.
 */
public class Normalizer
{
    private static Normalizer normalizer;

    /**
     * Initializes an object of normalizer class.
     */
    private Normalizer()
    {

    }

    /**
     * Get instance method to ensure singleton pattern,
     *
     * @return the one and only object of the Normalizer class.
     */
    public static Normalizer getInstance()
    {
        if (normalizer == null)
        {
            normalizer = new Normalizer();
        }

        return normalizer;
    }

    /**
     * The method normalizes the input data and returns an instance of NormalizedsensorState object.
     *
     * @param sensorState
     * @return a normalized sensorstate object.
     */
    public NormalizedSensorState Normalize(SensorState sensorState)
    {
        Instant sTime = sensorState.getTime();
        List<SensorValue> values = sensorState.getValues();
        int nValue = 0;
        int temp;
        int oValue = 0;
        boolean isEmulatable = true;
        NormalizedSensorState normalizedSensorState = new NormalizedSensorState(sTime);
        NormalizedValue normalizedValue;
        int max;
        int min;

        //Find the MAX and MIN values
        min = FindMin(values);
        max = FindMax(values);

        //Normalize the data
        // Retrieve data from the list and use the local variables isEmulatable and nvalue
        // nvalue = (oldvalue - max)/(max - min)
        for (int i = 0; i < values.size(); i++)
        {
            oValue = values.get(i).getValue();
            isEmulatable = values.get(i).isEmulatable();
            device =
            temp = (oValue - max) / (max - min);
            nValue = DetermineRange(temp);
            normalizedValue = new NormalizedValue(nValue, isEmulatable);
            normalizedSensorState.AddNormalizedValue(normalizedValue);
        }

        return normalizedSensorState;
    }

    /**
     * Determines what range the temproray normalized value should in.
     *  Do note that this might misbehave if the datatype is corrupt. Solution might be to truncate variable i.(SOLVED)
     *  Another problem may occur, if input "temp" is something like 0.2135234523, it might be evaluated range 3.(SOLVED)
     * @return normalized range.
     */
    private int DetermineRange(double temp)
    {
        int range = -1;
        for (double i = 0; i < 1; i = i+0.1)
        {
            i = HalfRound(i);
            if(temp < i);
            {
                range = (int)((i - 0.1)*10);
                break;
            }
        }
        return range;
    }

    /**
     * Formats the decimal number and truncates with a half round up.
     * @param i
     * @return Rounded decimal
     */
    private double HalfRound(double i)
    {
        int decimalPlaces = 2;
        BigDecimal bd = new BigDecimal(i);
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * The method finds the maximum value in the list.
     *
     * @param values list of sensorvalues
     * @return the maximum value of the list.
     */
    private int FindMax(List<SensorValue> values)
    {
        SensorValue currentSensorValue = values.get(0);
        int max = currentSensorValue.getValue();

        for (int i = 0; i < values.size(); i++)
        {
            currentSensorValue = values.get(i);
            if (currentSensorValue.getValue() > max)
                max = currentSensorValue.getValue();
        }

        return max;
    }

    /**
     * The method finds the minimum value in the list.
     *
     * @param values list of sensorvalues
     * @return the minimum value of the list.
     */
    private int FindMin(List<SensorValue> values)
    {
        SensorValue currentSensorValue = values.get(0);
        int min = currentSensorValue.getValue();
        for (int i = 0; i < values.size(); i++)
        {
            currentSensorValue = values.get(i);
            if (min > currentSensorValue.getValue())
                min = currentSensorValue.getValue();
        }

        return min;
    }
}
