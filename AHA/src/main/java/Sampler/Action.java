package Sampler;

import Normaliser.NormalizedValue;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.nio.ByteBuffer;
@DatabaseTable(tableName = "Actions")
public class Action {
  @DatabaseField(foreign = true, foreignAutoCreate=true)
  private Sample dbs;
  @DatabaseField(foreign = true)
  private NormalizedValue mValPrevious;

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Action action = (Action) o;

    if (mSensorId != action.mSensorId) return false;
    if (dbs != null ? !dbs.equals(action.dbs) : action.dbs != null) return false;
    if (mValPrevious != null ? !mValPrevious.equals(action.mValPrevious) : action.mValPrevious != null) return false;
    return !(mValCurrent != null ? !mValCurrent.equals(action.mValCurrent) : action.mValCurrent != null);

  }

  @Override
  public int hashCode(){
    int result = dbs != null ? dbs.hashCode() : 0;
    result = 31 * result + (mValPrevious != null ? mValPrevious.hashCode() : 0);
    result = 31 * result + (mValCurrent != null ? mValCurrent.hashCode() : 0);
    result = 31 * result + mSensorId;
    return result;
  }

  @DatabaseField(foreign = true)
  private NormalizedValue mValCurrent;
  @DatabaseField
  private int mSensorId;

  public Action(NormalizedValue val1, NormalizedValue val2, int sensorId) {
    this.mValPrevious = val1;
    this.mValCurrent = val2;
    this.mSensorId = sensorId;
  }

  Action(){}

  public int getDevice() {
    return mSensorId;
  }

  public int getDiff() {
    return mValCurrent.getValue() - mValPrevious.getValue();
  }

  public int getChangeToValue() {
    return mValCurrent.getValue();
  }

  public NormalizedValue getVal1(){ return mValPrevious; }
  public void setVal1(NormalizedValue value){mValPrevious = value;}

  public NormalizedValue getVal2(){ return mValCurrent; }
  public void setVal2(NormalizedValue value){mValCurrent = value;}

  @Override
  public String toString() {
    return String.format("Set sensor id %d to value %d", mSensorId, getChangeToValue());
  }

  /** A method to serialize the Action as a byte[]
   * We only include the SensorIndex and the 2nd value (the value to change to), as these are the values relevant for the arduino
   * @return a byte array consisting of the sensor index and the value
   */
  public byte[] serialize(){
    return ByteBuffer.allocate(4)
            .putShort((short) mValCurrent.getSensorIndexOnDevice())
            .putShort((short) mValCurrent.getValue()).array();
  }
}
