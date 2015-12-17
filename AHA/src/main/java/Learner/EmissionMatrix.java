package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.javatuples.Pair;

import java.util.*;

// rows are hidden states and cols are emission states
public class EmissionMatrix extends CommonMatrix
{

  private GammaMatrix gammaMatrix;
  protected MapWarden mapWarden;

  /**
   * Generates an observation matrix N x M (hiddenstates x emissionStates) with uniform distribution
   */
  public EmissionMatrix(MapWarden mapWarden)
  {
    this.mapWarden = mapWarden;
    int numEmissionStates = mapWarden.getNumEmissionStates();
    int numHiddenStates = mapWarden.getNumHiddenStates();
    double[][] values = new double[numHiddenStates][numEmissionStates];
    double preset = 1d / numEmissionStates;
    // initialize the matrix to a uniform distribution
    double[] innerValueArray = new double[numEmissionStates];
    Arrays.fill(innerValueArray, preset);
    Arrays.fill(values, innerValueArray);
    matrix = new BlockRealMatrix(values);
  }

  public EmissionMatrix(GammaMatrix gammaMatrix, MapWarden mapWarden) {
    this.gammaMatrix = gammaMatrix;
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    int numEmissionStates = mapWarden.getNumEmissionStates();
    matrix = new BlockRealMatrix(numHiddenStates, numEmissionStates);

/*    for (HiddenState i :
        mapWarden.iterateHiddenStates())
    {
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      for (Observation j : mapWarden.iterateObservations())
      {
        double probability = calcNewEmissionProbability(i, j);
        int jIndex = mapWarden.observationToObservationIndex(j);

        matrix.setEntry(iIndex, jIndex, probability);
      }
    }*/

    for (HiddenState i : mapWarden.iterateHiddenStates()){
      for (EmissionState j : mapWarden.iterateEmissionStates()){
        double numer = 0;
        double denom = 0;
        for (Observation t : mapWarden.iterateObservations()){
          EmissionState tEmission = mapWarden.observationToEmission(t);
          if (tEmission.equals(j)) {
            numer += gammaMatrix.getGammaEntry(t, i);
          }
          denom += gammaMatrix.getGammaEntry(t, i);
        }

        double value = numer / denom;
        setEntry(i, j, value);
      }
    }
  }

  public EmissionMatrix(MapWarden mapWarden, double[][] values){
    this.mapWarden = mapWarden;
    matrix = new BlockRealMatrix(values);
  }

/*  private double calcNewEmissionProbability(HiddenState i, Observation observation)
  {
    double numerator = 0;
    double denominator = 0;
    for (Observation t: mapWarden.iterateObservations())
    {
      double gammaProbability = gammaMatrix.getGammaEntry(i,t);
      if(observation.equals(t)) {
        numerator += gammaProbability;
      }
      denominator += gammaProbability;
    }

    return  numerator / denominator;
  }*/

  protected EmissionMatrix()
  {
  }

  public double getNorm()
  {
    return matrix.getNorm();
  }

  public NormalisedEmissionMatrix normalise()
  {
    double[][] normalisedMatrix = super.normalise(matrix);
    return new NormalisedEmissionMatrix(mapWarden, normalisedMatrix);
  }

  public double getEntry(HiddenState fromHiddenState, EmissionState emissionState)
  {
    int emissionStateIndex = mapWarden.emissionStateToEmissionStateIndex(emissionState);
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    return matrix.getEntry(hiddenStateIndex, emissionStateIndex);
  }

  public void setEntry(HiddenState fromHiddenState, EmissionState emissionState, double probability)
  {
    int emissionStateIndex = mapWarden.emissionStateToEmissionStateIndex(emissionState);
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    matrix.setEntry(hiddenStateIndex, emissionStateIndex, probability);
  }

  // returns the hidden state index that hiddenStateIndex is most likely to transition from, along with the probability to do so
  public Pair<EmissionState, Double> mostProbableTransitionFrom(HiddenState hiddenState)
  {
    double maxProbability = 0;
    int mostProbableIndex = 0;
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(hiddenState);
    double[] row = matrix.getRow(hiddenStateIndex);
    for (int i = 0; i < row.length; i++)
    {
      if (row[i] > maxProbability) {
        maxProbability = row[i];
        mostProbableIndex = i;
      }
    }

    EmissionState mostProbableEmissionState = mapWarden.emissionStateIndexToEmissionState(mostProbableIndex);

    return Pair.with(mostProbableEmissionState, maxProbability);
  }

  public void setProbabilityAndNormalise(double newProbability, HiddenState hiddenState, EmissionState emissionState){
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(hiddenState);
    int emissionStateIndex = mapWarden.emissionStateToEmissionStateIndex(emissionState);

    // calculate the difference between the current value and the value to set the probability to
    double currentProbability = getEntry(hiddenState, emissionState);
    double diff = newProbability - currentProbability;

    // set the probability to the newProbability
    setEntry(hiddenState, emissionState, newProbability);

    // normalise the rest of the values
    int valuesToNormalise = matrix.getColumnDimension()- 1;
    double valueToOffsetRest = diff / (double) valuesToNormalise;
      for (int col = 0; col < matrix.getColumnDimension(); col++){
        if (col != emissionStateIndex) {
          double curProb = matrix.getEntry(hiddenStateIndex, col);
          double valueToSet = curProb - valueToOffsetRest;
          matrix.setEntry(hiddenStateIndex, col, valueToSet);
      }
    }
  }
}
