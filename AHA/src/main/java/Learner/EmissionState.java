package Learner;

import Sampler.Action;
import Sampler.Sample;

import java.util.List;

/**
 * Created by simon on 04/12/2015.
 */
public class EmissionState
{
  private Integer hashcode;
  private List<Action> actions;
  public EmissionState(Sample distinctSample)
  {
    this.actions = distinctSample.getActions();
    this.hashcode = distinctSample.getHash().get(distinctSample.getHash().size() - 1);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EmissionState that = (EmissionState) o;

    return hashcode.equals(that.hashcode);

  }

  @Override
  public int hashCode()
  {
    return hashcode.hashCode();
  }

  public List<Action> getActions()
  {
    return actions;
  }

  public boolean equalsObservation(Observation t)
  {
    return hashcode == t.getHashCode();
  }
}
