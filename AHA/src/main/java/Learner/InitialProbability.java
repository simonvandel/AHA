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
    vector = new ArrayRealVector(numHiddenStates, 1/numHiddenStates);
  }

  public InitialProbability(MapWarden mapWardens, GammaMatrix gammaMatrix)
  {
    vector = new ArrayRealVector(mapWardens.getNumHiddenStates());
    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      Observation firstObservation = mapWardens.firstObservation();

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
