package Sampler;

import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Sampler {
  private static Sampler mSampler;
  private final static int SCOPE_SIZE = 2;
  private DB db = DB.getInstance();
  private List<NormalizedSensorState> mHistory;
  private NormalizedSensorState mPrevious;
  private int mScopeSize = 0;  //TODO Scopesize needs to have a proper value. Why 6? Well, this exclamation has, unexpectedly, six 's', six 'i' and six 'x'!
  private RemovalListener<String, Sample> sanitizerListener = removalNotification -> {
    Sample sample = removalNotification.getValue();
    if(removalNotification.getCause() != RemovalCause.EXPIRED){
      //we done goofed
      //something was removed from uncleanSamples due to something else than time expiration
      return;
    }
    if(sample == null){
      //we're fucked
      //value was garbage-collected before removalListener got to it. Yay dynamic garbage collection! just ignore? not much else to do..
      return;
    }
    db.putStateScopeIntoDB(sample);
    return;
  };
  //husk actions vi har fundet, indenfor 5 sekunder, så vi kan tjekke fejl der går på tværs af samples
  private Cache<String, Sample> uncleanSamples = CacheBuilder
      .newBuilder()
      .concurrencyLevel(1)
      .expireAfterWrite(5, TimeUnit.SECONDS)
      .removalListener(sanitizerListener)
      .build();
  private Reasoner reasoner = Reasoner.getInstance();

  List<NormalizedValue> zeroVal = new ArrayList<NormalizedValue>();

  /**
   * Initializes an object of Sampler class.
   */
  private Sampler(int SCOPE_SIZE){
    mScopeSize = SCOPE_SIZE;
    zeroVal.add(new NormalizedValue(0,false,"NotADevice",0));
    List<NormalizedSensorState> history = new ArrayList<NormalizedSensorState>(); //Initialize mHistory

    for (int i = 0; i < mScopeSize; i++) {
      history.add(null);
    }
    mHistory = history;
    mPrevious = null;
  }

  /**
   * Get instance method to ensure singleton pattern,
   *
   * @return the one and only object of the Sampler class.
   */
  public static Sampler getInstance() {
    if (mSampler == null) {
      mSampler = new Sampler(SCOPE_SIZE);
    }
    return mSampler;
  }

  /**
   * @return A sensor state
   */
  public Sample getSample(NormalizedSensorState newState) {
    List<Action> acs = findActions(mPrevious,newState);
    findInvertedActionsAndCleanStates(new Sample(mHistory,newState.getTime().getNano(),acs), newState); //important: The sample here is not the same as the one below, as newState is modified in this method
    List<Action> acsCorrected = findActions(mPrevious, newState); //we find actions again, as newState is modified
    moveScope(newState);
    Sample res = new Sample(mHistory, newState.getTime().getNano(), acsCorrected);
    return res;
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
    if (state1 == null || state2 == null) {
      // return empty list, as we cannot not find a action if the previous or current state was null
      return new ArrayList<>();
    }
    List<NormalizedValue> emulatables1 = findEmulatables(state1);
    List<NormalizedValue> emulatables2 = findEmulatables(state2);
    List<Action> actions = new ArrayList<Action>();
    for (int i = 0; i < emulatables1.size() && i < emulatables2.size(); i++) {//CANT ZIP Shitty java
      // add all emulatable sensor values as actions
      actions.add(new Action(emulatables1.get(i),emulatables2.get(i),emulatables2.get(i).getSensorIndexOnDevice()));
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

  /**
   * Cleans states, by finding actions which are inverted, and resetting their values to the correct value
   * @param sample the current sample
   * @param state2 the state which correlates to the action
   */
  private void findInvertedActionsAndCleanStates(Sample sample, NormalizedSensorState state2){
    //region Update found samples cache
    uncleanSamples.put(sample.toString(), sample);
    //endregion

    //region Identify and correct inverted actions
    List<Action> validActions = new ArrayList<Action>();
    for (Sample s: uncleanSamples.asMap().values())
    {
      validActions.addAll(s.getActions());
    }


    for (int i = 0; i < validActions.size() - 1; i++) {
      for (int j = i + 1; j < validActions.size() - 1; j++) { //Get all combinations of actions in sample
        if (validActions.get(i) == inverseAction(validActions.get(j))) { //If two actions are inverse to each other
          state2
              .getNormalizesValues()
              .get(state2
                  .getNormalizesValues()
                  .indexOf(validActions
                      .get(i)
                      .getVal2()))
              .setValue(validActions
                  .get(j)
                  .getVal2()
                  .getValue()); //Correct the state to have the value of before the action happened
          state2
              .getNormalizesValues()
              .get(state2
                  .getNormalizesValues()
                  .indexOf(validActions
                      .get(j)
                      .getVal1()))
              .setValue(validActions
                  .get(i)
                  .getVal1()
                  .getValue()); //Correct the state to have the value of before the action happened
          Reasoning reasoning = reasoner.getReasoningBehindAction(validActions.get(i));
          if(reasoning != null){
            db.flagModel(reasoning);
            reasoner.updateModel(reasoning);
          }
        }
      }
    }
    //endregion
  }
}
