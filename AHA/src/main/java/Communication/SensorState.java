package Communication;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A SensorState is a representation of sensor values in a given time snapshot.
 * The ordering of the sensor values can be assumed to not change. That is,
 * SensorState from time 1 and SensorState from time 2 will have the same ordering of sensor values.
 */
public class SensorState {
    private List<SensorValue> mValues = new ArrayList<SensorValue>();
    private Instant mTime;

    public SensorState(List<SensorValue> values, Instant time) {
        mValues = values;
        mTime = time;
    }

    /**
     * @return The time the last sensor value was recorded
     */
    public Instant getTime() {
        return mTime;
    }

    /**
     * @return The sensor values in a given time
     */
    public List<SensorValue> getValues() {
        return mValues;
    }

    @Override
    public String toString() {
        String string = "";
        for (int i = 0; i <= mValues.size(); i++) {
            string = string + "," + mValues.toString();
        }
        return string + mTime.toString();
    }

    @Override
    public int hashCode() {
        String hash = "";
        for(int i=0;i<mValues.size()-1;i++)
            hash = hash + mValues.get(i).hashCode();
        return hash.hashCode();
    }
}