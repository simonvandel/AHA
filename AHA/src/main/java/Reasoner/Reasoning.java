package Reasoner;

import Learner.HiddenState;
import Learner.Observation;
import Sampler.Action;

import java.util.List;

/**
 * Created by simon on 07/12/2015.
 */
public class Reasoning
{
  private List<Action> actions;
  private List<HiddenState> hiddenStates;
  private List<Observation> observations;

  public List<Action> getActions()
  {
    return actions;
  }

  public List<HiddenState> getHiddenStates()
  {
    return hiddenStates;
  }

  public List<Observation> getObservations()
  {
    return observations;
  }
}
