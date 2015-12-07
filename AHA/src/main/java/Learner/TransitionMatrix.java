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
        int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
        int jIndex = mapWarden.hiddenStateToHiddenStateIndex(j);
        matrix.setEntry(iIndex, jIndex, probability);
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

  public void setEntry(int i, int j, double probability)
  {
    matrix.setEntry(i,j, probability);
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
}
