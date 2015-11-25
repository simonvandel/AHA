package Sampler;

import Communication.SensorState;
import Communication.SensorValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by heider on 19/11/15.
 */
public class Sampler {
  private static Sampler mSampler;
  private List<SensorState> mHistory;
  private SensorState mPrevious;
  private int mScopeSize = 0;
  private int mEmulatableNum = 0;

  List<SensorValue> zeroVal = new ArrayList<>();

  /**
   * Initializes an object of Sampler class.
   */
  private Sampler(int scopeSize, int emulatableNum){
    mScopeSize = scopeSize;
    zeroVal.add(new SensorValue(0,false,"NAD",0));
        List<SensorState> history = new ArrayList<>(); //Initialize mHistory
    for (int i = 0; i < mScopeSize; i++) {
      history.add(new SensorState(zeroVal,Instant.now()));
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
  public Sample getSample(SensorState newState) {
    List<Action> acs = findActions(mPrevious,newState);
    moveScope(newState);
    return new Sample(mHistory,newState.getTime().getNano(),acs);
  }

  private void moveScope(SensorState newState){
    mHistory.remove(0); // Remove eldest entry
    mHistory.add(newState);
    mPrevious = newState;
  }

  /**
   * @return A sensor state
   */
  public List<Action> findActions(SensorState state1, SensorState state2) {
    List<SensorValue> emulatables1 = findEmulatables(state1);
    List<SensorValue> emulatables2 = findEmulatables(state2);
    List<Action> actions = new ArrayList<>();
    for (int i = 0; i < emulatables1.size() && i < emulatables2.size(); i++) {//CANT ZIP Shitty java
      if (emulatables1.get(i).getValue() != emulatables2.get(i).getValue()) {
        actions.add(new Action(emulatables1.get(i),emulatables2.get(i),i));
      }
    }
    return actions;
  }


  public List<SensorValue> findEmulatables(SensorState state) {
    return state.getValues()
                .stream()
                .filter(sensorValue -> sensorValue.isEmulatable())
                .collect(Collectors.toList());
  }
}
