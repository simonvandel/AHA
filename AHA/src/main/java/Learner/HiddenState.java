package Learner;

import java.util.List;

/**
 * Created by simon on 11/30/15.
 */
public class HiddenState
{
  private int cachedHashCode;
  private List<Integer> sampleHashes;
  public HiddenState(List<Integer> sampleHashes)
  {
    this.sampleHashes = sampleHashes;
    this.cachedHashCode = sampleHashes.hashCode();
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HiddenState that = (HiddenState) o;

    return sampleHashes.equals(that.sampleHashes);
  }

  @Override
  public int hashCode()
  {
    return cachedHashCode;
  }
}
