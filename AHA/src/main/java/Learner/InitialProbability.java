package Learner;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.List;

/**
 * Created by simon on 11/30/15.
 */
public class InitialProbability
{
  private ArrayRealVector vector;
  private MapWarden mapWarden;

  public InitialProbability(MapWarden mapWarden)
  {
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    // initialize the vector to a uniform distribution
    vector = new ArrayRealVector(numHiddenStates, 1d / numHiddenStates);
  }

  public InitialProbability(MapWarden mapWarden, GammaMatrix gammaMatrix)
  {
    this.mapWarden = mapWarden;
    vector = new ArrayRealVector(mapWarden.getNumHiddenStates());
    for (HiddenState i: this.mapWarden.iterateHiddenStates())
    {
      int iIndex = this.mapWarden.hiddenStateToHiddenStateIndex(i);
      Observation firstObservation = mapWarden.firstObservation();

      double probability = gammaMatrix.getEntry(i, firstObservation);
      vector.setEntry(iIndex, probability);
    }
  }

  public double getProbability(HiddenState hiddenState){

    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(hiddenState);
    return vector.getEntry(hiddenStateIndex);
  }

  public double getNorm()
  {
    return vector.getNorm();
  }
}
