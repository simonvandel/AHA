package Sampler;

import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Reasoner.Reasoner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.ClassPath;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Sampler {
  private static Sampler mSampler;
  private List<NormalizedSensorState> mHistory;
  private NormalizedSensorState mPrevious;
  private int mScopeSize = 0;
  private int mEmulatableNum = 0;
  //husk actions vi har fundet, indenfor 5 sekunder, så vi kan tjekke fejl der går på tværs af samples
  private Cache<String, Action> receivedActions = CacheBuilder
      .newBuilder()
      .expireAfterWrite(5, TimeUnit.SECONDS)
      .build();
  private Reasoner reasoner = Reasoner.getInstance();


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
    findInvertedActionsAndCleanStates(acs, mPrevious, newState);
    moveScope(newState);
    return new Sample(mHistory,newState.getTime().getNano(),acs);
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

  /**
   * Gives the inverse action to the one given
   * @param action the action from which the inverse is wanted
   * @return the inverse action to the one given as input
   */
  private Action inverseAction(Action action) {
    return new Action(action.getVal2(), action.getVal1(), action.getDevice());
  }

  private void findInvertedActionsAndCleanStates(List<Action> actions, NormalizedSensorState state1, NormalizedSensorState state2){
    //region Update found actions cache
    for (Action action: actions){
      receivedActions.put(action.toString(), action);
    }
    //endregion

    //region Identify and correct inverted actions
    receivedActions.cleanUp();
    List<Action> validActions = new ArrayList<Action>(receivedActions.asMap().values());
    for (int i = 0; i < validActions.size() - 1; i++) {
      for (int j = i + 1; j < validActions.size() - 1; j++) { //Get all combinations of actions in sample
        if (validActions.get(i) == inverseAction(validActions.get(j))) { //If two actions are inverse to each other
          state2
              .getNormalizesValues()
              .get(state2
                  .getNormalizesValues()
                  .indexOf(validActions
                      .get(j)
                      .getVal2()))
              .setValue(validActions
                  .get(j)
                  .getVal1()
                  .getValue()); //Correct the state to have the value of before the action happened

          if(reasoner.wasSystemAction(validActions.get(i))){
            //Flag the model in the DB
            //Tell Reasoner to update its model??
          }
        }
      }
    }
    //endregion
  }
}
