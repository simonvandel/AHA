package Sampler;

import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Sampler {
  private static Sampler mSampler;
  private List<NormalizedSensorState> mHistory;
  private NormalizedSensorState mPrevious;
  private int mScopeSize = 0;
  private int mEmulatableNum = 0;

  List<NormalizedValue> zeroVal = new ArrayList<NormalizedValue>();

  /**
   * Initializes an object of Sampler class.
   */
  private Sampler(int scopeSize, int emulatableNum){
    mScopeSize = scopeSize;
    zeroVal.add(new NormalizedValue(0,false,"NotADevice",0));
    List<NormalizedSensorState> history = new ArrayList<NormalizedSensorState>(); //Initialize mHistory
    for (int i = 0; i < mScopeSize; i++) {
      history.add(new NormalizedSensorState(Instant.now()));
    }
    mHistory = history;
    mPrevious = mHistory.get(mScopeSize-1);
    mEmulatableNum = emulatableNum;
  }

  /**
   * Get instance method to ensure singleton pattern,
   *
   * @return the one and only object of the Sampler class.
   */
  public static Sampler getInstance(int scopeSize, int emulatableNum) {
    if (mSampler == null) {
      mSampler = new Sampler(scopeSize, emulatableNum);
    }
    return mSampler;
  }

  /**
   * @return A sensor state
   */
  public Sample getSample(NormalizedSensorState newState) {
    List<Action> acs = findActions(mPrevious,newState);
    moveScope(newState);
    return new Sample(mHistory,newState.getTime(),acs);
  }

  private void moveScope(NormalizedSensorState newState){
    mHistory.remove(0); // Remove eldest entry
    mHistory.add(newState);
    mPrevious = newState;
  }

  /**
   * @return A sensor state
   */
  public List<Action> findActions(NormalizedSensorState state1, NormalizedSensorState state2) {
    List<NormalizedValue> emulatables1 = findEmulatables(state1);
    List<NormalizedValue> emulatables2 = findEmulatables(state2);
    List<Action> actions = new ArrayList<Action>();
    for (int i = 0; i < emulatables1.size() && i < emulatables2.size(); i++) {//CANT ZIP Shitty java
      if (emulatables1.get(i).getValue() != emulatables2.get(i).getValue()) {
        actions.add(new Action(emulatables1.get(i),emulatables2.get(i),i));
      }
    }
    return actions;
  }


  public List<NormalizedValue> findEmulatables(NormalizedSensorState state) {
    return state.getNormalizesValues()
                .stream()
                .filter(sensorValue -> sensorValue.isEmulatable())
                .collect(Collectors.toList());
  }
}
