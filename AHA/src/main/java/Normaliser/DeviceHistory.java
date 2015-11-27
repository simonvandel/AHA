package Normaliser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by brugeren on 26-11-2015.
 */
public class DeviceHistory
{
    private HashMap<Integer, ArrayList<Integer>> sensors = new HashMap<Integer, ArrayList<Integer>>(1500);



    //returns false if the sensor index wasn't created
    public boolean AddSensor(Integer sensorIndex) {
        if(!sensors.containsKey(sensorIndex)) {
            sensors.put(sensorIndex, new ArrayList<Integer>());
            return true;
        }
        return false;
    }

    //adds value, assumes ArrayList.put updates exsisting entries (found no definitive documentation on it)
    public void AddValue(Integer value, Integer sensorIndex) {
        ArrayList<Integer> values;
        if(sensors.containsKey(sensorIndex))
            values = sensors.get(sensorIndex);
        else
            values = new ArrayList<Integer>();
        values.add(value);
        sensors.put(sensorIndex, values);
    }

    public ArrayList<Integer> GetValues(Integer sensorIndex) {
        return sensors.get(sensorIndex);
    }


}
