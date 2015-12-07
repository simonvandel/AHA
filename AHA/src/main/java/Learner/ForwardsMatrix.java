package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by simon on 04/12/2015.
 */
public class ForwardsMatrix
{
  private BlockRealMatrix matrix;
  HashMap<HiddenState, Integer> hiddenStatesMap = new HashMap<>();
  HashMap<Integer, Integer> observationsMap = new HashMap<>();
  MapWarden mapWarden;
  private HiddenMarkovModel oldModel;

  public ForwardsMatrix(HiddenMarkovModel oldModel, MapWarden mapWarden) {
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
        double probability = calcForwardsProbability(i,t);
        int tIndex = mapWarden.observationToObservationIndex(t);
        matrix.setEntry(iIndex, tIndex, probability);
      }
    }
  }

  private double calcForwardsProbability(HiddenState hiddenState, Observation observation) {
    if(Objects.equals(observation, mapWarden.observationIndexToObservation(0))) {
      return oldModel.getInitialProbability().getProbability(hiddenState)
          * oldModel.getEmissionMatrix().getEntry(hiddenState, mapWarden.observationToEmission(observation) );
    }
    else {
      double sum = 0;
      for (HiddenState i: mapWarden.iterateHiddenStates())
      {
        Observation previousObservation = mapWarden.previousObservation(observation);
        sum += calcForwardsProbability(i, previousObservation)
            * oldModel.getTransitionMatrix().getEntry(i, hiddenState);
      }
      return oldModel.getEmissionMatrix().getEntry(hiddenState, mapWarden.observationToEmission(observation))
          * sum;
    }
  }

  public double getEntry(HiddenState i, Observation t)
  {
    int hiddenStateIndex = hiddenStatesMap.get(i);
    int observationIndex = observationsMap.get(t);
    return matrix.getEntry(observationIndex, hiddenStateIndex);
  }
}
