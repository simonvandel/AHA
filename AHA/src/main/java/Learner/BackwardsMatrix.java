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

/*    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        double probability = calcBackwardsProbability(i,t, calculatedValues);
        int tIndex = mapWarden.observationToObservationIndex(t);
        matrix.setEntry(iIndex,tIndex,probability);
      }
    }*/

    // beta T-1 (i) = 1, scaled by c T-1
    for (HiddenState i : mapWarden.iterateHiddenStates()){
      Observation lastObservation = mapWarden.lastObservation();
      double value = mapWarden.getScalingFactor(mapWarden.getNumObservations() - 1); // T - 1
      setEntry(lastObservation, i, value);
    }

    // beta pass
    for (int t = mapWarden.getNumObservations() - 2; t >= 0; t--){
      Observation tObservation = mapWarden.observationIndexToObservation(t);
      for (HiddenState i : mapWarden.iterateHiddenStates()){
        setEntry(tObservation, i, 0);
        for (HiddenState j : mapWarden.iterateHiddenStates()){
          Observation nextObservation = mapWarden.observationIndexToObservation(t + 1);
          EmissionState nextEmission = mapWarden.observationToEmission(nextObservation);
          // TODO: emission matrix must be N x M
          double value = getEntry(tObservation, i) +
              oldModel.getTransitionMatrix().getEntry(i, j) * oldModel.getEmissionMatrix().getEntry(j, nextEmission) * getEntry(nextObservation, j);
          setEntry(tObservation, i, value);
        }

        // scale beta t (i) with the same scale factor as alpha t (i)
        double value = mapWarden.getScalingFactor(t) * getEntry(tObservation, i);
        setEntry(tObservation, i, value);
      }
      int i = 0;
    }
    int i = 0;
  }

/*  private double calcBackwardsProbability(HiddenState hiddenState, Observation observation, HashMap<HiddenStateObservationPair, Double> calculatedValues) {
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
            * oldModel.getTransitionMatrix().getGammaEntry(hiddenState, j)
            * oldModel.getEmissionMatrix().getGammaEntry(j, nextObservation);
      }
      calculatedValues.put(new HiddenStateObservationPair(hiddenState, observation), sum);
      return sum;
    }
  }*/

  public double getEntry(Observation t, HiddenState i)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int observationIndex = mapWarden.observationToObservationIndex(t);
    return matrix.getEntry(hiddenStateIndex, observationIndex);
  }

  private void setEntry(Observation t, HiddenState i, double value) {
    int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int tIndex = mapWarden.observationToObservationIndex(t);
    matrix.setEntry(iIndex, tIndex, value);
  }
}
