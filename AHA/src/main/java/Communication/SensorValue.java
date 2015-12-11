package Communication;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SensorValues")
public class SensorValue {
    @DatabaseField(foreign = true)
    private SensorState dbss;
    @DatabaseField(generatedId = true, unique = true)
    private int id;
    @DatabaseField
    private int value;
    @DatabaseField
    private boolean isEmulatable;
    @DatabaseField(canBeNull = false)
    private String deviceAddress;
    @DatabaseField
    private int sensorIndexOnDevice;

    public SensorValue(int value, boolean isEmulatable, String deviceAddress, int sensorIndexOnDevice) {
        this.value = value;
        this.isEmulatable = isEmulatable;
        this.deviceAddress = deviceAddress;
        this.sensorIndexOnDevice = sensorIndexOnDevice;
    }

    private SensorValue(){}

    public void addDBSS(SensorState that)
    {
        dbss = that;
    }

    public int getValue()
    {
        return this.value;
    }

    public boolean isEmulatable() {
        return isEmulatable;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public int getSensorIndexOnDevice() {
        return sensorIndexOnDevice;
    }

    @Override
    public String toString(){
        return ""+value;
    }
}
