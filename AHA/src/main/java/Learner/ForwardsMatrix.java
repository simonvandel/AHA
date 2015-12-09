package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by simon on 04/12/2015.
 */
public class ForwardsMatrix
{
  // rows are hidden states, cols are observations
  private BlockRealMatrix matrix;
  MapWarden mapWarden;
  private HiddenMarkovModel oldModel;
  private HashMap<HiddenStateObservationPair, Double> calculatedValues;

  public ForwardsMatrix(HiddenMarkovModel oldModel, MapWarden mapWarden) {
    this.oldModel = oldModel;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numObservations = mapWarden.getNumObservations();
    calculatedValues = new HashMap<>(numHiddenStates * numObservations);
    matrix = new BlockRealMatrix(numHiddenStates, numObservations);

    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        int tIndex = mapWarden.observationToObservationIndex(t);
        double probability = calcForwardsProbability(i,t, calculatedValues);
        matrix.setEntry(iIndex, tIndex, probability);
      }
    }
  }

  private double calcForwardsProbability(HiddenState hiddenState, Observation observation, HashMap<HiddenStateObservationPair, Double> calculatedValues) {
    if(Objects.equals(observation, mapWarden.observationIndexToObservation(0))) {
      double result = oldModel.getInitialProbability().getProbability(hiddenState)
          * oldModel.getEmissionMatrix().getEntry(hiddenState, observation );

      calculatedValues.put(new HiddenStateObservationPair(hiddenState, observation), result);
      return result;
    }
    else {
      double sum = 0;
      for (HiddenState i: mapWarden.iterateHiddenStates())
      {
        Observation previousObservation = mapWarden.previousObservation(observation);
        Double recResult = calculatedValues.get(new HiddenStateObservationPair(hiddenState, previousObservation));
        if (recResult == null) {
          recResult = calcForwardsProbability(i, previousObservation, calculatedValues);
        }
        sum += recResult
            * oldModel.getTransitionMatrix().getEntry(i, hiddenState);
      }
      double result = oldModel.getEmissionMatrix().getEntry(hiddenState, observation)
          * sum;

      calculatedValues.put(new HiddenStateObservationPair(hiddenState, observation), result);
      return result;
    }
  }

  public double getEntry(HiddenState i, Observation t)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int observationIndex = mapWarden.observationToObservationIndex(t);
    return matrix.getEntry(hiddenStateIndex, observationIndex);
  }

}
