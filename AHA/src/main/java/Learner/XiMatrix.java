package Learner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.List;
import java.util.Map;

/**
 * Created by simon on 04/12/2015.
 */
public class XiMatrix
{
  // matrix[row][col][depth] where rows are hidden states, cols are hidden states and depths are observations
  private double[][][] matrix;
  private final ForwardsMatrix forwards;
  private final BackwardsMatrix backwards;
  private final HiddenMarkovModel oldModel;
  private final MapWarden mapWarden;

  public XiMatrix(ForwardsMatrix forwards, BackwardsMatrix backwards, HiddenMarkovModel oldModel, MapWarden mapWarden) {
    this.forwards = forwards;
    this.backwards = backwards;
    this.oldModel = oldModel;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numObservations = mapWarden.getNumObservations();
    matrix = new double[numHiddenStates][numHiddenStates][numObservations];

    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (HiddenState j: mapWarden.iterateHiddenStates())
      {
        int jIndex = mapWarden.hiddenStateToHiddenStateIndex(j);
        for (Observation t: mapWarden.iterateObservations())
        {
          int tIndex = mapWarden.observationToObservationIndex(t);
          double probability = calcXiProbability(i, j, t);
          matrix[iIndex][jIndex][tIndex] = probability;
        }
      }
    }
  }

  private double calcXiProbability(HiddenState i, HiddenState j, Observation t)
  {
    double denominator = 0;
    for (HiddenState k: mapWarden.iterateHiddenStates())
    {
      Observation lastObservation = mapWarden.lastObservation();
      denominator += forwards.getEntry(k, lastObservation);
    }

    Observation nextObservation = mapWarden.nextObservation(t);
    if (nextObservation == null) {
      // there is no next observation, so we can not calculate this xi probability
      return 0;
    }
    double numerator = forwards.getEntry(i, t)
        * oldModel.getTransitionMatrix().getEntry(i, j)
        * backwards.getEntry(j, nextObservation)
        * oldModel.getEmissionMatrix().getEntry(j, nextObservation);

    if (denominator == 0) return 0;
    else {
      return numerator / denominator;
    }
  }

  public double getEntry(HiddenState i, HiddenState j, Observation t)
  {
    int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int jIndex = mapWarden.hiddenStateToHiddenStateIndex(j);
    int tIndex = mapWarden.observationToObservationIndex(t);
    return matrix[iIndex][jIndex][tIndex];
  }
}
