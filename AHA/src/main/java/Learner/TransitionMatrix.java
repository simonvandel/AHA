package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.javatuples.Pair;

import java.util.Arrays;

/**
 * Created by simon on 11/30/15.
 */
public class TransitionMatrix
{
  // we expect this matrix to become large,
  // so we use a BlockRealMatrix since the documentation suggests it is cache friendly.
  // The matrix is dense, as we need to store all transitions.
  private BlockRealMatrix matrix;
  public TransitionMatrix(int numHiddenStates)
  {
    double[][] data = new double[numHiddenStates][numHiddenStates];
    double preset = 1/numHiddenStates;
    // initialize the matrix to a uniform distribution
    Arrays.fill(data, preset);
    matrix = new BlockRealMatrix(numHiddenStates, numHiddenStates, data, true);
  }

  public double getNorm()
  {
    return matrix.getNorm();
  }

  public int getNumberOfHiddenStates() {
    return matrix.getColumnDimension();
  }

  public double getEntry(int hiddenStateRowIndex, int hiddenStateColoumnIndex)
  {
    return matrix.getEntry(hiddenStateRowIndex, hiddenStateColoumnIndex);
  }

  public void setEntry(int i, int j, double probability)
  {
    matrix.setEntry(i,j, probability);
  }

  public Pair<Integer, Double> mostProbableTransitionFrom(int hiddenStateIndex)
  {
    double maxProbability = 0;
    int mostProbableIndex = 0;
    double[] row = matrix.getRow(hiddenStateIndex);
    for (int i = 0; i < row.length; i++)
    {
      if (row[i] > maxProbability) {
        maxProbability = row[i];
        mostProbableIndex = i;
      }
    }

    return Pair.with(mostProbableIndex, maxProbability);
  }
}
