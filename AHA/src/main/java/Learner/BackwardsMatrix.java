package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.HashMap;
import java.util.List;

/**
 * Created by simon on 04/12/2015.
 */
public class BackwardsMatrix
{
  private BlockRealMatrix matrix;
  private HiddenMarkovModel oldModel;
  private MapWarden mapWarden;

  public BackwardsMatrix(HiddenMarkovModel oldModel, MapWarden mapWarden) {
    this.oldModel = oldModel;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numObservations = mapWarden.getNumObservations();
    matrix = new BlockRealMatrix(numHiddenStates, numObservations);

    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        double probability = calcBackwardsProbability(i,t);
        int tIndex = mapWarden.observationToObservationIndex(t);
        matrix.setEntry(iIndex,tIndex,probability);
      }
    }
  }

  private double calcBackwardsProbability(HiddenState hiddenState, Observation observation) {
    // base case is that are at the last observation
    if(observation.equals(mapWarden.lastObservation())) {
      return 1;
    }
    else {
      double sum = 0;
      for (HiddenState j: mapWarden.iterateHiddenStates())
      {
        Observation nextObservation = mapWarden.nextObservation(observation);
        sum += calcBackwardsProbability(j, nextObservation)
            * oldModel.getTransitionMatrix().getEntry(hiddenState, j)
            * oldModel.getEmissionMatrix().getEntry(j, mapWarden.observationToEmission(nextObservation));
      }
      return sum;
    }
  }

  public double getEntry(HiddenState i, Observation t)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int observationIndex = mapWarden.observationToObservationIndex(t);
    return matrix.getEntry(observationIndex, hiddenStateIndex);
  }
}
