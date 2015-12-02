package Learner;

import Sampler.Sample;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by simon on 11/30/15.
 */
public class EmissionMatrix
{
  private BlockRealMatrix matrix;
  private HashMap<Sample, Integer> observationMapping;
  /**
   * Generates an observation matrix K x N (numEmissionStates x hiddenstates) with uniform distribution
   */
  public EmissionMatrix(int numEmissionStates, int numHiddenStates, List<Sample> samples)
  {
    observationMapping = new HashMap<>(numEmissionStates);
    // We want to map samples (that can contain duplicated samples) to an index of unique samples
    int indexCount = 0;
    for (Sample sample: samples)
    {
      observationMapping.put(sample, indexCount);
      indexCount++;
    }

    double[][] data = new double[numEmissionStates][numHiddenStates];
    double preset = 1/numEmissionStates;
    // initialize the matrix to a uniform distribution
    Arrays.fill(data, preset);
    matrix = new BlockRealMatrix(numEmissionStates, numHiddenStates, data, true);
  }

  public EmissionMatrix(int numEmissionStates, int numHiddenStates)
  {
    matrix = new BlockRealMatrix(numEmissionStates, numHiddenStates);
  }

  public double getNorm()
  {
    return matrix.getNorm();
  }

  public int getNumEmissionVariables()
  {
    return matrix.getRowDimension();
  }

  public double getEntry(int hiddenStateIndex, Sample emissionState)
  {
    int emissionIndex = observationMapping.get(emissionState);
    return matrix.getEntry(emissionIndex, hiddenStateIndex);
  }

  public void setEntry(int i, int j, double probability)
  {
    matrix.setEntry(i,j,probability);
  }
}
