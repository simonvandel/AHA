package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;

/**
 * Created by simon on 04/12/2015.
 */
public class GammaMatrix
{
  // yt(i,j)
  private final double[][][] xiMatrix;
  private HiddenMarkovModel oldModel;
  private final ForwardsMatrix forwards;
  private final BackwardsMatrix backwards;


  // yt(i): rows are hidden states, cols are observations
  private final BlockRealMatrix gammaMatrix;
  private MapWarden mapWarden;

  public GammaMatrix(HiddenMarkovModel oldModel, ForwardsMatrix forwards, BackwardsMatrix backwards, MapWarden mapWarden) {
    this.oldModel = oldModel;
    this.forwards = forwards;
    this.backwards = backwards;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numObservations = mapWarden.getNumObservations();
    gammaMatrix = new BlockRealMatrix(numHiddenStates, numObservations);
    xiMatrix = new double[numObservations][numHiddenStates][numHiddenStates];

    /*for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation t: mapWarden.iterateObservations())
      {
        int tIndex = mapWarden.observationToObservationIndex(t);
        double probability = calcGammaProbability(i, t);
        gammaMatrix.setEntry(iIndex, tIndex, probability);
      }
    }*/

    for (int t = 0; t <= mapWarden.getNumObservations() - 2; t++){
      double denom = 0;
      Observation tObservation = mapWarden.observationIndexToObservation(t);
      Observation nextObservation = mapWarden.observationIndexToObservation(t + 1);
      EmissionState nextEmissionState = mapWarden.observationToEmission(nextObservation);
      for (HiddenState i : mapWarden.iterateHiddenStates()){
        for (HiddenState j : mapWarden.iterateHiddenStates()){
          denom += forwards.getEntry(tObservation, i)
              * oldModel.getTransitionMatrix().getEntry(i, j)
              * oldModel.getEmissionMatrix().getEntry(j, nextEmissionState)
              * backwards.getEntry(nextObservation, j);
        }
      }

      for (HiddenState i : mapWarden.iterateHiddenStates()){
        setGammaEntry(tObservation, i, 0);
        for (HiddenState j : mapWarden.iterateHiddenStates()){
          double xiValue =
              (forwards.getEntry(tObservation, i)
                * oldModel.getTransitionMatrix().getEntry(i, j)
                * oldModel.getEmissionMatrix().getEntry(j, nextEmissionState)
                * backwards.getEntry(nextObservation, j)) / denom;
          setXiEntry(tObservation, i, j, xiValue);

          double gammaValue = getGammaEntry(tObservation, i) + getXiEntry(tObservation, i, j);
          setGammaEntry(tObservation, i, gammaValue);
        }
      }
    }

    // Special case for gamma T-1 (i)
    double denom = 0;
    Observation lastObservation = mapWarden.lastObservation();
    for (HiddenState i : mapWarden.iterateHiddenStates()){
      denom += forwards.getEntry(lastObservation, i);
    }

    for (HiddenState i : mapWarden.iterateHiddenStates()){
      double value = forwards.getEntry(lastObservation, i) / denom;
      setGammaEntry(lastObservation, i, value);
    }
  }

  public double getXiEntry(Observation observation, HiddenState i, HiddenState j){
    int observationIndex = mapWarden.observationToObservationIndex(observation);
    int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int jIndex = mapWarden.hiddenStateToHiddenStateIndex(j);
    return xiMatrix[observationIndex][iIndex][jIndex];
  }

  private void setXiEntry(Observation observation, HiddenState i, HiddenState j, double value){
    int observationIndex = mapWarden.observationToObservationIndex(observation);
    int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int jIndex = mapWarden.hiddenStateToHiddenStateIndex(j);
    xiMatrix[observationIndex][iIndex][jIndex] = value;
  }

  private void setGammaEntry(Observation observation, HiddenState i, double value){
    int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
    int observationIndex = mapWarden.observationToObservationIndex(observation);
    gammaMatrix.setEntry(iIndex, observationIndex, value);
  }

/*  private double calcGammaProbability(HiddenState i, Observation t)
  {
    double numerator = forwards.getGammaEntry(i,t) * backwards.getGammaEntry(i,t);
    double denominator = 0;
    Observation lastObservation = mapWarden.lastObservation();

    for (HiddenState j: mapWarden.iterateHiddenStates())
    {
      denominator += forwards.getGammaEntry(j, lastObservation);
    }

    if (denominator == 0) return 0;
    else {
      return numerator / denominator;
    }
  }*/

  public double getGammaEntry(Observation observation, HiddenState hiddenState)
  {
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(hiddenState);
    int observationIndex = mapWarden.observationToObservationIndex(observation);
    return gammaMatrix.getEntry(hiddenStateIndex, observationIndex);
  }
}
