package Sampler;

import Communication.SensorValue;
import Normaliser.NormalizedValue;

public class Action {
  private NormalizedValue mVal1;
  private NormalizedValue mVal2;
  private int mSensorId;

  public Action(NormalizedValue val1, NormalizedValue val2, int sensorId) {
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

  public NormalizedValue getVal1(){ return mVal1; }

  public NormalizedValue getVal2(){ return mVal2; }

  @Override
  public String toString() {
    return mSensorId + "," + getChangeToValue();
  }
}
