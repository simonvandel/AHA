package Learner;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.javatuples.Pair;

import java.util.Arrays;

/**
 * Created by simon on 04/12/2015.
 */
public class CommonMatrix
{
  // we expect this matrix to become large,
  // so we use a BlockRealMatrix since the documentation suggests it is cache friendly.
  // The matrix is dense, as we need to store all transitions.
  protected BlockRealMatrix matrix;
  protected MapWarden mapWarden;

  protected CommonMatrix() {
  }

  protected double[][] normalise(BlockRealMatrix inputMatrix)
  {
    BlockRealMatrix normalisedMatrix = new BlockRealMatrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
    for (int coloumnIndex = 0; coloumnIndex < inputMatrix.getColumnDimension(); coloumnIndex++)
    {
      double[] coloumn = inputMatrix.getColumn(coloumnIndex);
      double max = Arrays.stream(coloumn)
          .max().getAsDouble();
      double min = Arrays.stream(coloumn)
          .min().getAsDouble();

      double diffMaxMin = max - min;
      // normalisedValue = (value - min) / diff
      double[] newColoumn = Arrays.stream(coloumn)
          .map(value -> (value - min) / diffMaxMin)
          .toArray();
      normalisedMatrix.setColumn(coloumnIndex, newColoumn);
    }

    return normalisedMatrix.getData();
  }


}
