package Learner;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Created by simon on 11/30/15.
 */
public class InitialProbability
{
  private ArrayRealVector vector;
  public InitialProbability(long numHiddenStates)
  {
    // initialize the vector to a uniform distribution
    vector = new ArrayRealVector((int) numHiddenStates, 1/numHiddenStates);
  }

  public double getProbability(int hiddenStateIndex){

    return vector.getEntry(hiddenStateIndex);
  }

  public double getNorm()
  {
    return vector.getNorm();
  }
}
