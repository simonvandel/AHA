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
  private NormalizedValue mValFrom;

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Action action = (Action) o;

    if (mSensorId != action.mSensorId) return false;
    if (mValFrom != null ? !mValFrom.equals(action.mValFrom) : action.mValFrom != null) return false;
    return !(mValTo != null ? !mValTo.equals(action.mValTo) : action.mValTo != null);

  }

  @Override
  public int hashCode(){
    int result = dbs != null ? dbs.hashCode() : 0;
    result = 31 * result + (mValFrom != null ? mValFrom.hashCode() : 0);
    result = 31 * result + (mValTo != null ? mValTo.hashCode() : 0);
    result = 31 * result + mSensorId;
    return result;
  }

  @DatabaseField(foreign = true)
  private NormalizedValue mValTo;
  @DatabaseField
  private int mSensorId;

  public Action(NormalizedValue valFrom, NormalizedValue valTo, int sensorId) {
    this.mValFrom = valFrom;
    this.mValTo = valTo;
    this.mSensorId = sensorId;
  }

  Action(){}

  public int getDevice() {
    return mSensorId;
  }

  public int getDiff() {
    return mValTo.getValue() - mValFrom.getValue();
  }

  public int getChangeToValue() {
    return mValTo.getValue();
  }

  public NormalizedValue getValFrom(){ return mValFrom; }
  public void setValFrom(NormalizedValue value){
    mValFrom = value;}

  public NormalizedValue getValTo(){ return mValTo; }
  public void setValTo(NormalizedValue value){
    mValTo = value;}

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
            .putShort((short) mValTo.getSensorIndexOnDevice())
            .putShort((short) mValTo.getValue()).array();
  }
}
