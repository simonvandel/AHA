package Learner;

import org.apache.commons.math3.linear.ArrayRealVector;

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
    Observation firstObservation = mapWarden.firstObservation();
    for (HiddenState i: this.mapWarden.iterateHiddenStates())
    {
      int iIndex = this.mapWarden.hiddenStateToHiddenStateIndex(i);
      double probability = gammaMatrix.getGammaEntry(firstObservation, i);
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
