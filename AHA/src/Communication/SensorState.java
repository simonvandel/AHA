package Communication;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A SensorState is a representation of sensor values in a given time snapshot.
 * The ordering of the sensor values can be assumed to not change. That is,
 * SensorState from time 1 and SensorState from time 2 will have the same ordering of sensor values,
 * given no sensors have been added or removed.
 */
public class SensorState {
    private List<SensorValue> values = new ArrayList<>();
    private Instant time;

    // constructor with default visibility, so only this package can construct it
    SensorState(List<SensorValue> values, Instant time) {
        this.values = values;
        this.time = time;
    }

    /**
     * @return The time the last sensor value was recorded
     */
    public Instant getTime() {
        return time;
    }

    /**
     * @return The sensor values in a given time
     */
    public List<SensorValue> getValues() {
        return values;
    }
}
