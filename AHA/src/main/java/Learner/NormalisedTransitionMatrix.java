package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

/**
 * Created by simon on 04/12/2015.
 */
public class NormalisedTransitionMatrix extends TransitionMatrix
{
  public NormalisedTransitionMatrix(MapWarden mapWarden, double[][] values)
  {
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    matrix = new BlockRealMatrix(numHiddenStates, numHiddenStates, values, true);
  }
}
