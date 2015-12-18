package Reasoner;
import Communication.Communicator;
import Database.DB;
import Sampler.Action;
import Sampler.Sample;
import Sampler.Sampler;
import Sampler.SampleList;
import com.digi.xbee.api.exceptions.XBeeException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * created by kafuch
 */
public class Reasoner {
  private static Reasoner reasoner;
  private static Logger logger;
  private IModel currentModel = null;
  private Communicator com = null;
  private SampleList sampleList = SampleList.getInstance();
  //husk actions vi har sendt, indenfor 5 sekunder, så vi kan tjekke om de actions vi får er bruger eller system
  private Cache<Action, Reasoning> sentActions = CacheBuilder
          .newBuilder()
          .concurrencyLevel(1)
          .expireAfterWrite(5, TimeUnit.SECONDS)
          .build();

  private Reasoner(Logger reasonLogger) {
    logger = reasonLogger;
  }

  public static Reasoner getInstance(Logger reasonLogger){
    if(reasoner == null){
      reasoner = new Reasoner(reasonLogger);
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
    if( actions == null ) { return; }
    boolean skip = false;
    for (Action action :
        actions)
    {
      for(Sample sampleListSample: sampleList.getSamples()){
        for (Action sampleAction: sampleListSample.getActions()){
          if(action == Sampler.inverseAction(sampleAction)){
            skip = true;
            break;
          }
        }
        if(skip){
          break;
        }
      }
      if(skip){
        skip = false;
        continue;
      }
      try{
        logger.log(Level.INFO, "Sending data: " + action.toString());
        com.SendData(action.getVal1().getDeviceAddress(), action.serialize());
      } catch (XBeeException e){
        //Would probably be a good idea to handle the exception instead of ignoring it...
      }
    }

  }

  /**
   * Given a sample, calculate an action
   * @param sample the sample to reason about
   * @return an action which is probable according to the model
     */
  public List<Action> reason(Sample sample) {
    if(currentModel == null){
      return null;
    }
    Reasoning reasoning = currentModel.CalculateReasoning(sample);
    if (reasoning == null) {
      return null;
    }
    if(reasoning.getActions().isEmpty()){
      return null;
    }
    List<Action> actions = reasoning.getActions()
        .stream()
        .filter(action -> action != null)
        .filter(action -> !sentActions.asMap().keySet().contains(Sampler.inverseAction(action))).collect(Collectors.toList());

    actions
        .forEach(action -> sentActions.put(action, reasoning));//foreach action, store in cache

    return actions;
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
    if(currentModel != null){
      currentModel.TakeFeedback(reasoning);
    }
  }

  public void setCurrentModel(IModel model){
      try{
        currentModel = model;
      } catch (ConcurrentModificationException e){
        setCurrentModel(model);
      }
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