package Normaliser;

/**
 * Created by Zobair on 19-11-2015.
 */
public class NormalizedValue{
  private int value;
  private final boolean isEmulatable;
  private final String deviceAddress;
  private final int sensorIndexOnDevice;

  public NormalizedValue(int value, boolean isEmulatable, String deviceAddress, int sensorIndexOnDevice)
  {
    this.value = value;
    this.isEmulatable = isEmulatable;
    this.deviceAddress = deviceAddress;
    this.sensorIndexOnDevice = sensorIndexOnDevice;
  }

  public int getValue()
  {
    return this.value;
  }
  public void setValue(int newValue){ value = newValue; }

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
