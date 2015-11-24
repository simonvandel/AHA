package Communication;

public class SensorValue {

    private final int value;
    private final boolean isEmulatable;
    private final String deviceAddress;
    private final int sensorIndexOnDevice;

    public SensorValue(int value, boolean isEmulatable, String deviceAddress, int sensorIndexOnDevice) {
        this.value = value;
        this.isEmulatable = isEmulatable;
        this.deviceAddress = deviceAddress;
        this.sensorIndexOnDevice = sensorIndexOnDevice;
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
}
