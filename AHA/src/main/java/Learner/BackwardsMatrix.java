package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.HashMap;

/**
 * Created by simon on 04/12/2015.
 */
public class BackwardsMatrix
{
  // rows are hidden states, cols are observations
  private BlockRealMatrix matrix;
  private HiddenMarkovModel oldModel;
  private MapWarden mapWarden;

  public BackwardsMatrix(HiddenMarkovModel oldModel, MapWarden mapWarden) {
    this.oldModel = oldModel;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numObservations = mapWarden.getNumObservations();
    HashMap<HiddenStateObservationPair, Double> calculatedValues = new HashMap<>(numHiddenStates * numObservations);
    matrix = new BlockRealMatrix(numHiddenStates, numObservations);

    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        double probability = calcBackwardsProbability(i,t, calculatedValues);
        int tIndex = mapWarden.observationToObservationIndex(t);
        matrix.setEntry(iIndex,tIndex,probability);
      }
    }
  }

  private double calcBackwardsProbability(HiddenState hiddenState, Observation observation, HashMap<HiddenStateObservationPair, Double> calculatedValues) {
    // base case is that we are at the last observation
    if(observation.equals(mapWarden.lastObservation())) {
      double result = 1;
      calculatedValues.put(new HiddenStateObservationPair(hiddenState, observation), result);
      return result;
    }
    else {
      double sum = 0;
      for (HiddenState j: mapWarden.iterateHiddenStates())
      {
        Observation nextObservation = mapWarden.nextObservation(observation);
        Double recResult = calculatedValues.get(new HiddenStateObservationPair(hiddenState, nextObservation));
        if (recResult == null) {
          recResult = calcBackwardsProbability(j, nextObservation, calculatedValues);
        }
        sum += recResult
            * oldModel.getTransitionMatrix().getEntry(hiddenState, j)
            * oldModel.getEmissionMatrix().getEntry(j, nextObservation);
      }
      calculatedValues.put(new HiddenStateObservationPair(hiddenState, observation), sum);
      return sum;
    }
  }

  public double getEntry(HiddenState i, Observation t)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int observationIndex = mapWarden.observationToObservationIndex(t);
    return matrix.getEntry(hiddenStateIndex, observationIndex);
  }
}
