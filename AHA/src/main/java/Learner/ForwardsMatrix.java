package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by simon on 04/12/2015.
 */
public class ForwardsMatrix
{
  // rows are observations, cols are hidden states
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
    matrix = new BlockRealMatrix(numObservations, numHiddenStates);

/*    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        int tIndex = mapWarden.observationToObservationIndex(t);
        double probability = calcForwardsProbability(i,t, calculatedValues);
        matrix.setEntry(iIndex, tIndex, probability);
      }
    }*/

    // compute alpha 0(i)
    double c0 = 0;
    mapWarden.setScalingFactor(0, c0);
    Observation firstObservation = mapWarden.firstObservation();
    for (HiddenState i : mapWarden.iterateHiddenStates()){
      EmissionState firstEmissionState = mapWarden.observationToEmission(firstObservation);
      double value = oldModel.getInitialProbability().getProbability(i)
          * oldModel.getEmissionMatrix().getEntry(i, firstEmissionState);
      setEntry(firstObservation, i, value);

      c0 += value;
      mapWarden.setScalingFactor(0, c0);
    }

    // scale alpha 0(i)
    c0 = 1d / c0;
    mapWarden.setScalingFactor(0, c0);
    for (HiddenState i : mapWarden.iterateHiddenStates()){
      double value = c0 * getEntry(firstObservation, i);
      setEntry(firstObservation, i, value);
    }

    // compute alpha t(i)
    for (int t = 1; t <= mapWarden.getNumObservations() - 1; t++){
      double ct = 0;
      mapWarden.setScalingFactor(t, ct);
      Observation tObservation = mapWarden.observationIndexToObservation(t);
      EmissionState tEmissionState = mapWarden.observationToEmission(tObservation);
      for (HiddenState i : mapWarden.iterateHiddenStates()){
        setEntry(tObservation, i, 0);
        for (HiddenState j : mapWarden.iterateHiddenStates()){
          Observation previousObservation = mapWarden.observationIndexToObservation(t - 1);
          double value = getEntry(tObservation, i) + getEntry(previousObservation, j) * oldModel.getTransitionMatrix().getEntry(j, i);
          setEntry(tObservation, i, value);
        }
        double value = getEntry(tObservation, i) * oldModel.getEmissionMatrix().getEntry(i, tEmissionState);
        setEntry(tObservation, i, value);
        ct += value;
        mapWarden.setScalingFactor(t, ct);
      }

      // scale alpha t(i)
      ct = 1 / ct;
      mapWarden.setScalingFactor(t, ct);
      for (HiddenState i : mapWarden.iterateHiddenStates()){
        double value = ct * getEntry(tObservation, i);
        setEntry(tObservation, i, value);
      }
    }
  }

/*  private double calcForwardsProbability(HiddenState hiddenState, Observation observation, HashMap<HiddenStateObservationPair, Double> calculatedValues) {
    if(Objects.equals(observation, mapWarden.observationIndexToObservation(0))) {
      double result = oldModel.getInitialProbability().getProbability(hiddenState)
          * oldModel.getEmissionMatrix().getGammaEntry(hiddenState, observation );

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
            * oldModel.getTransitionMatrix().getGammaEntry(i, hiddenState);
      }
      double result = oldModel.getEmissionMatrix().getGammaEntry(hiddenState, observation)
          * sum;

      calculatedValues.put(new HiddenStateObservationPair(hiddenState, observation), result);
      return result;
    }
  }*/

  public void setEntry(Observation t, HiddenState i, double value) {
    int tIndex = mapWarden.observationToObservationIndex(t);
    int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    matrix.setEntry(tIndex, iIndex, value);
  }

  public double getEntry(Observation t, HiddenState i)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int observationIndex = mapWarden.observationToObservationIndex(t);
    return matrix.getEntry(observationIndex, hiddenStateIndex);
  }

}
