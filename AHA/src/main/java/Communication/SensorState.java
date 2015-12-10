package Communication;

import Database.InstantPersister;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A SensorState is a representation of sensor values in a given time snapshot.
 * The ordering of the sensor values can be assumed to not change. That is,
 * SensorState from time 1 and SensorState from time 2 will have the same ordering of sensor values.
 */
@DatabaseTable(tableName = "SensorStates")
public class SensorState {
    @DatabaseField(generatedId = true, unique = true)
    private int id;
    @ForeignCollectionField(eager = true)
    private Collection<SensorValue> mpValues = new ArrayList<SensorValue>(); //persistable values
    private List<SensorValue> mValues = new ArrayList<SensorValue>();
    @DatabaseField(persisterClass = InstantPersister.class)
    private Instant mTime;

    SensorState(){ mValues = mpValues.stream().collect(Collectors.toList());}

    public SensorState(List<SensorValue> values, Instant time) {
        values.stream().forEach(val -> val.addDBSS(this));
        mValues = values;
        mpValues = values;
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
        for (SensorValue val : mpValues) {
            string = string + "," + val.toString();
        }
        return string;
    }

    @Override
    public int hashCode() {
        String hash = "";
        for(int i=0;i<mValues.size()-1;i++)
            hash = hash + mValues.get(i).hashCode() * 17;
        return hash.hashCode();
    }
}
