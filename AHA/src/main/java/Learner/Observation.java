package Learner;

import Sampler.Sample;

/**
 * Created by simon on 04/12/2015.
 */
public class Observation
{
  private Integer hashCode;
  public Observation(Sample sample)
  {
    hashCode = sample.getHash().get(sample.getHash().size() - 1);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Observation that = (Observation) o;

    return hashCode.equals(that.hashCode);

  }

  @Override
  public int hashCode()
  {
    return hashCode.hashCode();
  }
}
