package Learner;

import Sampler.Sample;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by simon on 11/30/15.
 */
public class Learner
{
  private HiddenMarkovModel currentModel;

  private final double convergenceConstant = 0.001; // TODO: change to suitable number

  public HiddenMarkovModel learn(List<Sample> samples){
    List<HiddenState> hiddenStates = samples
                                      .stream()
                                      .map(sample -> new HiddenState(sample.getHash()))
                                      .distinct()
                                      .collect(Collectors.toList());
    // we need to check if a model has ever been generated.
    // If not, we generate one with uniform distribution of
    // initial probability, transition matrix and observation matrix
    if(currentModel == null){

      // the number of hidden states are the unique number of patterns.
      // We only support patterns we have already seen, so we base this number on the number of different samples seen
      // This is the N on the wikipedia article on Baum-Welch
      int numHiddenStates = hiddenStates.size();

      // the number of observation variables is the same as the number of hidden states,
      // since each hidden state must correspond to an observed variable
      // This is the K on the wikipedia article on Baum-Welch
      int numObservationVariables = numHiddenStates;

      EmissionMatrix initEmissionMatrix = new EmissionMatrix(numObservationVariables, numHiddenStates, samples);
      TransitionMatrix initTransitionMatrix = new TransitionMatrix(numHiddenStates);
      InitialProbability initProbability = new InitialProbability(numHiddenStates);
      currentModel = new HiddenMarkovModel(initProbability, initTransitionMatrix, initEmissionMatrix);
    }

    // we are now sure that we have a model.
    // We how want to iteratively apply the Baum-Welch algorithm to the model until we converge on some model.
    HiddenMarkovModel oldModel = currentModel;
    HiddenMarkovModel newModel = null;
    boolean shouldContinue = true;
    do{
      newModel = baumWelch(oldModel, samples, hiddenStates);
      shouldContinue = diffModel(oldModel, newModel) > convergenceConstant;
      oldModel = newModel;
    }while (shouldContinue);

    return newModel;
  }

  private HiddenMarkovModel baumWelch(HiddenMarkovModel oldModel, List<Sample> observations, List<HiddenState> hiddenStates)
  {


    InitialProbability newInitProbability = null; // TODO
    TransitionMatrix newTransitionMatrix = null; // TODO
    EmissionMatrix newObervationMatrix = null; // TODO
    return new HiddenMarkovModel(newInitProbability, newTransitionMatrix, newObervationMatrix);
  }

  private BlockRealMatrix calcForwardsMatrix(HiddenMarkovModel oldModel, List<Sample> samples) {
    int numHiddenStates = oldModel.getNumHiddenStates();
    int observations = samples.size();
    BlockRealMatrix forwards = new BlockRealMatrix(numHiddenStates, observations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < observations; t++)
      {
        double probability = calcForwardsProbability(i,t, oldModel, samples);
        forwards.setEntry(i,t,probability);
      }
    }

    return forwards;
  }

  private double calcForwardsProbability(int hiddenStateIndex, int observationIndex, HiddenMarkovModel oldModel, List<Sample> samples) {
    if(observationIndex == 0) {
      return oldModel.getInitialProbability().getProbability(hiddenStateIndex)
          * oldModel.getEmissionMatrix().getEntry(hiddenStateIndex, samples.get(observationIndex));
    }
    else {
      double sum = 0;
      int numHiddenStates = oldModel.getNumHiddenStates();
      for (int i = 0; i < numHiddenStates; i++)
      {
        sum += calcForwardsProbability(i, observationIndex - 1, oldModel, samples) * oldModel.getTransitionMatrix().getEntry(i, hiddenStateIndex);
      }
      return oldModel.getEmissionMatrix().getEntry(hiddenStateIndex, samples.get(observationIndex)) * sum;
    }
  }

  private BlockRealMatrix calcBackwardsMatrix(HiddenMarkovModel oldModel, List<Sample> samples) {
    int numHiddenStates = oldModel.getNumHiddenStates();
    int observations = samples.size();
    BlockRealMatrix backwards = new BlockRealMatrix(numHiddenStates, observations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < observations; t++)
      {
        double probability = calcBackwardsProbability(i,t, oldModel, samples);
        backwards.setEntry(i,t,probability);
      }
    }

    return backwards;
  }

  private double calcBackwardsProbability(int hiddenStateIndex, int observationIndex, HiddenMarkovModel oldModel, List<Sample> samples) {
    // base case is that are at the last observation
    if(observationIndex == samples.size() - 1) {
      return 1;
    }
    else {
      double sum = 0;
      int numHiddenStates = oldModel.getNumHiddenStates();
      for (int j = 0; j < numHiddenStates; j++)
      {
        sum += calcBackwardsProbability(j, observationIndex + 1, oldModel, samples)
               * oldModel.getTransitionMatrix().getEntry(j, hiddenStateIndex)
               * oldModel.getEmissionMatrix().getEntry(j, samples.get(observationIndex + 1));
      }
      return sum;
    }
  }

  private double[][][] calcXiMatrix(BlockRealMatrix forwards, BlockRealMatrix backwards, HiddenMarkovModel oldModel, List<Sample> samples) {
    int numHiddenStates = oldModel.getNumHiddenStates();
    int observations = samples.size();
    double[][][] xi = new double[numHiddenStates][numHiddenStates][observations];

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int j = 0; j < numHiddenStates; j++)
      {
        for (int t = 0; t < observations; t++)
        {
          double probability = calcXiProbability(i, j, t, oldModel, samples, forwards, backwards);
          xi[i][j][t] = probability;
        }
      }
    }

    return xi;
  }

  private double calcXiProbability(int i, int j, int t, HiddenMarkovModel oldModel, List<Sample> samples, BlockRealMatrix forwards, BlockRealMatrix backwards)
  {
    double denominator = 0;
    int sampleSize = samples.size();
    for (int k = 0; k < oldModel.getNumHiddenStates(); k++)
    {
      denominator += forwards.getEntry(k, sampleSize - 1);
    }

    double numerator = forwards.getEntry(i, t)
        * oldModel.getTransitionMatrix().getEntry(i, j)
        * backwards.getEntry(j, t+1)
        * oldModel.getEmissionMatrix().getEntry(j, samples.get(t+1));

    return numerator / denominator;
  }

  private BlockRealMatrix calcGammaMatrix(BlockRealMatrix forwards, BlockRealMatrix backwards) {

    int numHiddenStates = forwards.getRowDimension();
    int numObservations = forwards.getColumnDimension();
    BlockRealMatrix gamma = new BlockRealMatrix(numHiddenStates, numObservations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < numObservations; t++)
      {
        double probability = calcGammaProbability(i, t, forwards, backwards, numHiddenStates, numObservations);
        gamma.setEntry(i,t,probability);
      }
    }

    return gamma;
  }

  private double calcGammaProbability(int i, int t, BlockRealMatrix forwards, BlockRealMatrix backwards, int numHiddenStates, int numObservations)
  {
    double numerator = forwards.getEntry(i,t) * backwards.getEntry(i,t);
    double denominator = 0;
    for (int j = 0; j < numHiddenStates; j++)
    {
      denominator += forwards.getEntry(j, numObservations);
    }

    return numerator / denominator;
  }

  private InitialProbability calcNewInitialProbability(BlockRealMatrix gammaMatrix){
    return new InitialProbability(gammaMatrix.getColumn(0));
  }

  /**
   * @param model1 Model 1
   * @param model2 Model 2
   * @return Calculates the similarity between two models. Can be used to tell how similar two models are.
   */
  private double diffModel(HiddenMarkovModel model1, HiddenMarkovModel model2)
  {
    return Math.abs( model1.getInitialProbability().getNorm() - model2.getInitialProbability().getNorm()
             - model1.getEmissionMatrix().getNorm() - model2.getEmissionMatrix().getNorm()
             - model1.getTransitionMatrix().getNorm() - model2.getTransitionMatrix().getNorm());
  }
}
