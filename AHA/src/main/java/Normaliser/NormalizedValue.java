package Normaliser;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Zobair on 19-11-2015.
 */
@DatabaseTable(tableName = "NormalizedValues")
public class NormalizedValue{
  @DatabaseField(generatedId = true, unique = true)
  private int id;
  @DatabaseField
  private int value;
  @DatabaseField
  private boolean isEmulatable;
  @DatabaseField
  private String deviceAddress;
  @DatabaseField
  private int sensorIndexOnDevice;

  public NormalizedValue(int value, boolean isEmulatable, String deviceAddress, int sensorIndexOnDevice)
  {
    this.value = value;
    this.isEmulatable = isEmulatable;
    this.deviceAddress = deviceAddress;
    this.sensorIndexOnDevice = sensorIndexOnDevice;
  }
  private NormalizedValue(){}

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
