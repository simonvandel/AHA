package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.javatuples.Pair;

import java.util.Arrays;

// matrix info: rows are fromHiddenStates, cols are toHiddenStates
public class TransitionMatrix extends CommonMatrix
{
  protected MapWarden mapWarden;
  private GammaMatrix gammaMatrix;
  private XiMatrix xiMatrix;

  protected TransitionMatrix(){

  }

  public TransitionMatrix(MapWarden mapWarden)
  {
    this.mapWarden = mapWarden;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    double[][] values = new double[numHiddenStates][numHiddenStates];
    double preset = 1d / numHiddenStates;
    // initialize the matrix to a uniform distribution

    double[] innerValueArray = new double[numHiddenStates];
    Arrays.fill(innerValueArray, preset);
    Arrays.fill(values, innerValueArray);
    matrix = new BlockRealMatrix(values);
  }

  public TransitionMatrix(MapWarden mapWarden, GammaMatrix gammaMatrix) {
    this.mapWarden = mapWarden;
    this.gammaMatrix = gammaMatrix;

    int numHiddenStates = mapWarden.getNumHiddenStates();
    matrix = new BlockRealMatrix(numHiddenStates, numHiddenStates);

    /*
    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      for (HiddenState j: mapWarden.iterateHiddenStates())
      {
        double probability = calcNewTransitionProbability(i, j);
        setEntry(i, j, probability);
      }
    }*/

    for (HiddenState i : mapWarden.iterateHiddenStates()){
      for (HiddenState j : mapWarden.iterateHiddenStates()){
        double numer = 0;
        double denom = 0;
        for (int t = 0; t <= mapWarden.getNumObservations() - 2; t++){
          Observation tObservation = mapWarden.observationIndexToObservation(t);
          numer += gammaMatrix.getXiEntry(tObservation, i, j);
          denom += gammaMatrix.getGammaEntry(tObservation, i);
        }

        double value = numer / denom;
        setEntry(i, j, value);
      }
    }
  }

  /*private double calcNewTransitionProbability(HiddenState i, HiddenState j)
  {
    double numerator = 0;
    double denominator = 0;

    for (Observation t: mapWarden.iterateObservations())
    {
      // do not compute values for the last observation
      if (t != mapWarden.lastObservation()) {
        numerator += xiMatrix.getEntry(i, j, t);
        denominator += gammaMatrix.getGammaEntry(i,t);
      }
    }
    if (numerator / denominator > 1.0) {
      int k = 0;
    }

    return  numerator / denominator;
  }*/

  public NormalisedTransitionMatrix normalise()
  {
    double[][] normalisedMatrix = super.normalise(matrix);
    return new NormalisedTransitionMatrix(mapWarden, normalisedMatrix);
  }

  public double getNorm()
  {
    return matrix.getNorm();
  }

  public double getEntry(HiddenState fromHiddenState, HiddenState toHiddenState)
  {
    int fromHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    int toHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(toHiddenState);
    return matrix.getEntry(fromHiddenStateIndex, toHiddenStateIndex);
  }

  private void setEntry(HiddenState fromHiddenState, HiddenState toHiddenState, double probability)
  {
    int fromHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    int toHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(toHiddenState);
    matrix.setEntry(fromHiddenStateIndex, toHiddenStateIndex, probability);
  }

  // returns the hidden state index that hiddenStateIndex is most likely to transition from, along with the probability to do so
  public Pair<HiddenState, Double> mostProbableTransitionFrom(HiddenState hiddenState)
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

    HiddenState mostProbableHiddenState = mapWarden.hiddenStateIndexToHiddenState(mostProbableIndex);

    return Pair.with(mostProbableHiddenState, maxProbability);
  }

  public void setProbabilityAndNormalise(double newProbability, HiddenState fromHiddenState, HiddenState toHiddenState){
    int fromHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    int toHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(toHiddenState);

    // calculate the difference between the current value and the value to set the probability to
    double currentProbability = getEntry(fromHiddenState, toHiddenState);
    double diff = newProbability - currentProbability;

    // set the probability to the newProbability
    setEntry(fromHiddenState, toHiddenState, newProbability);

    // normalise the rest of the values
    int valuesToNormalise = matrix.getColumnDimension() - 1;
    double valueToOffsetRest = diff / (double) valuesToNormalise;
      for (int col = 0; col < matrix.getColumnDimension(); col++){
        if (col != toHiddenStateIndex) {
          double curProb = matrix.getEntry(fromHiddenStateIndex, col);
          double valueToSet = curProb - valueToOffsetRest;
          matrix.setEntry(fromHiddenStateIndex, col, valueToSet);
        }
    }
  }
}
