package Normaliser;

/**
 * Created by Zobair on 19-11-2015.
 */
public class NormalizedValue {
    private int value;
    private final boolean isEmulatable;

    public NormalizedValue(int value, boolean isEmulatable) {
        this.value = value;
        this.isEmulatable = isEmulatable;
    }
}
