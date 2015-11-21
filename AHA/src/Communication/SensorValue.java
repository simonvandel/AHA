package Communication;

public class SensorValue {

    private final int value;
    private final boolean isEmulatable;

    public SensorValue(int value, boolean isEmulatable) {
        this.value = value;
        this.isEmulatable = isEmulatable;
    }

    public int getValue()
    {
        return this.value;
    }

    public boolean isEmulatable() {
        return isEmulatable;
    }
}
