package Sampler;

import Communication.SensorValue;

/**
 * Created by deter on 23-Nov-15.
 */
public class Action {
  private SensorValue mVal1;
  private SensorValue mVal2;
  private int mSensorId;

  public Action(SensorValue val1, SensorValue val2, int sensorId) {
    this.mVal1 = val1;
    this.mVal2 = val2;
    this.mSensorId = sensorId;
  }

  public int getDevice() {
    return mSensorId;
  }

  public int getDiff() {
    return mVal2.getValue() - mVal1.getValue();
  }

  public int getChangeToValue() {
    return mVal2.getValue();
  }

  public SensorValue getVal1(){ return mVal1; }

  public SensorValue getVal2(){ return mVal2; }

  @Override
  public String toString() {
    return mSensorId + "," + getChangeToValue();
  }
}
