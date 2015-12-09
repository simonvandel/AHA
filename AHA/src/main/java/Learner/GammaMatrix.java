package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.List;

/**
 * Created by simon on 04/12/2015.
 */
public class GammaMatrix
{
  private final ForwardsMatrix forwards;
  private final BackwardsMatrix backwards;

  // rows are hidden states, cols are observations
  private final BlockRealMatrix matrix;
  private MapWarden mapWarden;

  public GammaMatrix(ForwardsMatrix forwards, BackwardsMatrix backwards, MapWarden mapWarden) {
    this.forwards = forwards;
    this.backwards = backwards;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numObservations = mapWarden.getNumObservations();
    matrix = new BlockRealMatrix(numHiddenStates, numObservations);

    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        int tIndex = mapWarden.observationToObservationIndex(t);
        double probability = calcGammaProbability(i, t);
        matrix.setEntry(iIndex, tIndex, probability);
      }
    }
  }

  private double calcGammaProbability(HiddenState i, Observation t)
  {
    double numerator = forwards.getEntry(i,t) * backwards.getEntry(i,t);
    double denominator = 0;
    Observation lastObservation = mapWarden.lastObservation();

    for (HiddenState j: mapWarden.iterateHiddenStates())
    {
      denominator += forwards.getEntry(j, lastObservation);
    }

    if (denominator == 0) return 0;
    else {
      return numerator / denominator;
    }
  }

  public double getEntry(HiddenState hiddenState, Observation observation)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(hiddenState);
    int observationIndex = mapWarden.observationToObservationIndex(observation);
    return matrix.getEntry(hiddenStateIndex, observationIndex);
  }
}
