package Reasoner;

import Learner.EmissionState;
import Learner.HiddenState;
import Sampler.Action;

import java.util.List;

/**
 * Created by simon on 07/12/2015.
 */
public class Reasoning
{
  private List<Action> actions;
  private List<HiddenState> hiddenStates;
  private List<EmissionState> emissionStates;

  public List<Action> getActions()
  {
    return actions;
  }

  public Reasoning(List<Action> actions, List<HiddenState> hiddenStates, List<EmissionState> emissionStates)
  {
    this.actions = actions;
    this.hiddenStates = hiddenStates;
    this.emissionStates = emissionStates;
  }

  public List<HiddenState> getHiddenStates()
  {
    return hiddenStates;

  }

  @Override
  public String toString(){
    String s = "";
    for (Action a : actions){
      s += a.toString() + "; ";
    }
    return s;
  }

  public List<EmissionState> getEmissions()
  {
    return emissionStates;
  }
}
