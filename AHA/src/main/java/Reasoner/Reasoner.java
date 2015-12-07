package Reasoner;
import Communication.Communicator;
import Database.DB;
import Sampler.Action;
import Sampler.Sample;
import com.digi.xbee.api.exceptions.XBeeException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
 * Action CalculateReasoning(Sample s); //should calculate the most likely action to occur (which is above a certain threshold)
 * void TakeFeedback(Action a1, Action a2); //used to update the reasoners model based on two wrong actions
 */
public class Reasoner {
  private static Reasoner reasoner;
  private DB db = DB.getInstance();
  private IModel currentModel = null; //TODO db.getModel();
  private Communicator com = null;
  //husk actions vi har sendt, indenfor 5 sekunder, så vi kan tjekke om de actions vi får er bruger eller system
  private Cache<Action, Reasoning> sentActions = CacheBuilder
          .newBuilder()
          .concurrencyLevel(1)
          .expireAfterWrite(5, TimeUnit.SECONDS)
          .build();

  private Reasoner() {
  }
  public static Reasoner getInstance(){
    if(reasoner == null){
      reasoner = new Reasoner();
    }
    return reasoner;
  }

  public void setCommunicator(Communicator com) {
    this.com = com;

  }

  /**
   * Given a sample, calculate an action and send this to communicator
   * @param sample the sample to reason about
     */
  public void reasonAndSend(Sample sample){
    List<Action> actions = reason(sample);
    if(actions != null){
      for (Action action :
          actions)
      {
        try{
          com.SendData(action.getVal1().getDeviceAddress(), action.serialize());
        } catch (XBeeException e){
          //Would probably be a good idea to handle the exception instead of ignoring it...
        }
      }

    }
  }

  /**
   * Given a sample, calculate an action
   * @param sample the sample to reason about
   * @return an action which is probable according to the model
     */
  public List<Action> reason(Sample sample) {
    Reasoning reasoning = currentModel.CalculateReasoning(sample);
    sentActions.cleanUp();
    if (reasoning == null) {
      return null;
    }
    if(reasoning.getActions().isEmpty()){
      return null;
    }
    reasoning.getActions() //foreach action store in cache
        .stream()
        .filter(action -> action != null)
        .forEach(action -> sentActions.put(action, reasoning));

    return reasoning.getActions();
  }

  /**
   * Gives the reasoning behind the given system action
   * @param action the action to get the reasoning behind
   * @return a reasoning or null, if action was not system action (or time limit has expired)
   */
  public Reasoning getReasoningBehindAction(Action action){
    if(sentActions.asMap().containsKey(action)){
      return sentActions.asMap().get(action);
    }
    return null;
  }

  /**
   * Updates the reasoners instance of the model s.t. the same mistake will not be made several times in a row
   * @param reasoning the reasoning behind the action, which was a mistake
   */
  public void updateModel(Reasoning reasoning){

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