package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.javatuples.Pair;

import java.util.Arrays;

/**
 * Created by simon on 11/30/15.
 */
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
    double preset = 1/numHiddenStates;
    // initialize the matrix to a uniform distribution
    Arrays.fill(values, preset);
    matrix = new BlockRealMatrix(numHiddenStates, numHiddenStates, values, true);
  }

  public TransitionMatrix(MapWarden mapWarden, GammaMatrix gammaMatrix, XiMatrix xiMatrix) {
    this.mapWarden = mapWarden;
    this.gammaMatrix = gammaMatrix;
    this.xiMatrix = xiMatrix;
    int numHiddenStates = mapWarden.getNumHiddenStates();
    matrix = new BlockRealMatrix(numHiddenStates, numHiddenStates);
    for (HiddenState i: mapWarden.iterateHiddenStates())
    {
      for (HiddenState j: mapWarden.iterateHiddenStates())
      {
        double probability = calcNewTransitionProbability(i, j);
        setEntry(i, j, probability);
      }
    }
  }

  private double calcNewTransitionProbability(HiddenState i, HiddenState j)
  {
    double numerator = 0;
    double denominator = 0;

    for (Observation t: mapWarden.iterateObservations())
    {
      numerator += xiMatrix.getEntry(i, j, t);
      denominator += gammaMatrix.getEntry(i,t);
    }

    return  numerator / denominator;
  }

  public NormalisedTransitionMatrix normalise()
  {
    double[][] normalisedMatrix = super.normalise(matrix);
    return new NormalisedTransitionMatrix(mapWarden, normalisedMatrix);
  }

  public double getNorm()
  {
    return matrix.getNorm();
  }

  public int getNumberOfHiddenStates() {
    return matrix.getColumnDimension();
  }

  public double getEntry(HiddenState fromHiddenState, HiddenState toHiddenState)
  {
    int fromHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    int toHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(toHiddenState);
    return matrix.getEntry(toHiddenStateIndex, fromHiddenStateIndex);
  }

  public void setEntry(HiddenState fromHiddenState, HiddenState toHiddenState, double probability)
  {
    int fromHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    int toHiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(toHiddenState);
    matrix.setEntry(toHiddenStateIndex, fromHiddenStateIndex, probability);
  }

  // returns the hidden state index that hiddenStateIndex is most likely to transition from, along with the probability to do so
  public Pair<HiddenState, Double> mostProbableTransitionFrom(HiddenState hiddenState)
  {
    double maxProbability = 0;
    int mostProbableIndex = 0;
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(hiddenState);
    double[] column = matrix.getColumn(hiddenStateIndex);
    for (int i = 0; i < column.length; i++)
    {
      if (column[i] > maxProbability) {
        maxProbability = column[i];
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
    int valuesToNormalise = (matrix.getColumnDimension() * matrix.getRowDimension() - 1);
    double valueToOffsetRest = diff / valuesToNormalise;
    for (int row = 0; row < matrix.getRowDimension(); row++){
      for (int col = 0; col < matrix.getColumnDimension(); col++){
        if (row != toHiddenStateIndex && col != fromHiddenStateIndex) {
          double curProb = matrix.getEntry(row, col);
          double valueToSet = curProb + valueToOffsetRest;
          matrix.setEntry(row, col, valueToSet);
        }
      }
    }
  }
}
