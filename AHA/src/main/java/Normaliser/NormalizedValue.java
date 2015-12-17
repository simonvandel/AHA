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

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NormalizedValue that = (NormalizedValue) o;

    if (value != that.value) return false;
    if (isEmulatable != that.isEmulatable) return false;
    if (sensorIndexOnDevice != that.sensorIndexOnDevice) return false;
    return !(deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null);

  }

  @Override
  public int hashCode(){
    int result = value;
    result = 31 * result + (isEmulatable ? 1 : 0);
    result = 31 * result + (deviceAddress != null ? deviceAddress.hashCode() : 0);
    result = 31 * result + sensorIndexOnDevice;
    return result;
  }

  public NormalizedValue(int value, boolean isEmulatable, String deviceAddress, int sensorIndexOnDevice, int valueRange)
  {
    this.value = value;
    this.isEmulatable = isEmulatable;
    this.deviceAddress = deviceAddress;
    this.sensorIndexOnDevice = sensorIndexOnDevice;
    this.range = valueRange;
  }
  private NormalizedValue(){}

  private int range;

  public int getRange(){return range;}

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
