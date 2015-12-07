package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

/**
 * Created by simon on 04/12/2015.
 */
public class NormalisedEmissionMatrix extends EmissionMatrix
{
  public NormalisedEmissionMatrix(MapWarden mapWarden, double[][] values)
  {
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numEmissionStates = mapWarden.getNumEmissionStates();
    matrix = new BlockRealMatrix(numEmissionStates, numHiddenStates, values, true);
  }
}
