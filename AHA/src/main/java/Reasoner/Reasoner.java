package Reasoner;
import Database.DB;
import Sampler.Action;
import Sampler.Sample;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * created by kafuch
 *
 * THESE NEED TO BE IMPLEMENTED FOR THIS TO WORK:
 * Model //class
 * Model DB.getModel() //gets the most recent model from the DB
 * void DB.flagModel(Action a1, Action a2) //flags two actions as wrong in the databases model
 * void DB.flagEntries(List<Action> actions) //flags the actions in the databases history as valid
 * Action CalculateAction(Sample s); //should calculate the most likely action to occur (which is above a certain threshold)
 * void TakeFeedback(Action a1, Action a2); //used to update the reasoners model based on two wrong actions
 */
public class Reasoner {
  private DB db = null;
  private IModel currentModel = null;
  private ICom com = null;
  //husk actions vi har sendt, indenfor 5 sekunder, så vi kan tjekke om de actions vi får er bruger eller system
  private Cache<String, Action> sentActions = CacheBuilder
          .newBuilder()
          .expireAfterWrite(5, TimeUnit.SECONDS)
          .build();
  //husk actions vi har modtaget, indenfor x sekunder, så vi kan tjekke fejl der går på tværs af samples
  private Cache<String, Action> receivedActions = CacheBuilder
          .newBuilder()
          .expireAfterWrite(5, TimeUnit.SECONDS)
          .build();

  public Reasoner() {
    //db = DB.getInstance();
    //currentModel = db.getModel();
  }

  /**
   * Given a sample, calculate an action and send this to communicator
   * @param sample the sample to reason about
     */
  public void reasonAndSend(Sample sample){
    List<Action> actions = reason(sample);
    if(actions != null){
      for (Action action : actions) {
        com.sendAction(action);
      }
    }
  }

  /**
   * Given a sample, calculate an action
   * @param sample the sample to reason about
   * @return an action which is probable according to the model
     */
  public List<Action> reason(Sample sample) {
    //region Update received actions cache
    for (Action action: sample.getActions()){
      receivedActions.put(action.toString(), action);
    }
    //endregion

    //region Feedback
    receivedActions.cleanUp();
    sentActions.cleanUp();
    List<Action> validActions = new ArrayList<Action>(receivedActions.asMap().values());
    for (int i = 0; i < sample.getActions().size() - 1; i++) {
      for (int j = i + 1; j < sample.getActions().size() - 1; j++) { //Get all combinations of actions in sample
        if (sample.getActions().get(i) == inverseAction(sample.getActions().get(j))) { //If two actions are inverse to each other
          validActions.remove(sample.getActions().get(i)); //remove invalid actions
          validActions.remove(sample.getActions().get(j));
          if(sentActions.getIfPresent(sample.getActions().get(i).toString()) != null){ //if first action was system action
            //db.flagModel(sample.getActions().get(i), sample.getActions().get(i)); //flag the model in DB, so learner knows a mistake was made
            currentModel.TakeFeedback(sample.getActions().get(i), sample.getActions().get(j)); //Update our current model, to not make the same mistake twice
          }
        }
      }
    }

    //db.flagEntries(validActions);
    //endregion

    List<Action> actions = currentModel.CalculateAction(sample);
    if(actions != null){
      for (Action action: actions) {
        sentActions.put(actions.toString(), action);
      }

    }
    return actions;
  }

  /**
   * Gives the inverse action to the one given
   * @param action the action from which the inverse is wanted
     * @return the inverse action to the one given as input
     */
  private Action inverseAction(Action action) {
    return new Action(action.getVal2(), action.getVal1(), action.getDevice());
  }
}

//This is just placeholders for now
//public class DataBase {
//  IModel GetModel(){}
//
//  void FlagEntries(List<Action> actions){} //should flag every action in the list as valid in the DB's history
//
//  void FlagModel(Action a1, Action a2){}
//}
//
interface ICom {
  public void sendAction(Action act);
}

//interface Action {
//}
//
//class Sample {
//  List<Action> Actions = null;
//}
