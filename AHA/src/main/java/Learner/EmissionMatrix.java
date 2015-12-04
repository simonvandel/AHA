package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by simon on 11/30/15.
 */
public class EmissionMatrix extends CommonMatrix
{

  /**
   * Generates an observation matrix K x N (numEmissionStates x hiddenstates) with uniform distribution
   */
  public EmissionMatrix(MapWarden mapWarden)
  {
    this.mapWarden = mapWarden;
    int numEmissionStates = mapWarden.getNumEmissionStates();
    int numHiddenStates = mapWarden.getNumHiddenStates();
    double[][] values = new double[numEmissionStates][numHiddenStates];
    double preset = 1/numEmissionStates;
    // initialize the matrix to a uniform distribution
    Arrays.fill(values, preset);
    matrix = new BlockRealMatrix(numEmissionStates, numHiddenStates, values, true);
  }

  public EmissionMatrix(MapWarden mapWarden, double[][] values) {
    this.mapWarden = mapWarden;
    int numEmissionStates = mapWarden.getNumEmissionStates();
    int numHiddenStates = mapWarden.getNumHiddenStates();
    matrix = new BlockRealMatrix(numEmissionStates, numHiddenStates, values, true);
  }

  protected EmissionMatrix()
  {
  }

  public double getNorm()
  {
    return matrix.getNorm();
  }

  public int getNumEmissionVariables()
  {
    return matrix.getRowDimension();
  }

  public NormalisedEmissionMatrix normalise()
  {
    double[][] normalisedMatrix = super.normalise(matrix);
    return new NormalisedEmissionMatrix(mapWarden, normalisedMatrix);
  }

  public double getEntry(HiddenState fromHiddenState, EmissionState toEmissionState)
  {
    int emissionIndex = mapWarden.emissionStateToEmissionStateIndex(toEmissionState);
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    return matrix.getEntry(emissionIndex, hiddenStateIndex);
  }

  public void setEntry(HiddenState fromHiddenState, EmissionState toEmissionState, double probability)
  {
    int emissionIndex = mapWarden.emissionStateToEmissionStateIndex(toEmissionState);
    int hiddenStateIndex = mapWarden.hiddenStateToHiddenStateIndex(fromHiddenState);
    matrix.setEntry(emissionIndex, hiddenStateIndex, probability);
  }

  // returns the hidden state index that hiddenStateIndex is most likely to transition from, along with the probability to do so
  public Pair<EmissionState, Double> mostProbableTransitionFrom(HiddenState hiddenState)
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

    EmissionState mostProbableEmissionState = mapWarden.emissionStateIndexToEmissionState(mostProbableIndex);

    return Pair.with(mostProbableEmissionState, maxProbability);
  }

}
