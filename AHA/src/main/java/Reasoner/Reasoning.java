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

  public Reasoning(List<Action> actions, List<HiddenState> hiddenStates, List<Observation> observations)
  {
    this.actions = actions;
    this.hiddenStates = hiddenStates;
    this.observations = observations;
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
