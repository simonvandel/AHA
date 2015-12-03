package Learner;

import Sampler.Sample;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by simon on 11/30/15.
 */
public class Learner
{
  private HiddenMarkovModel currentModel;

  private final double convergenceConstant = 0.001; // TODO: change to suitable number

  public HiddenMarkovModel learn(List<Sample> sampleObservations){

    List<Integer> observations = sampleObservations
        .stream()
        .map(sample -> sample.getHash().get(sample.getHash().size() - 1))
        .collect(Collectors.toList());

    // an emission state is a snapshot. We can only emit all distinct snapshots we have seen.
    List<Integer> emissionStates = observations
                                      .stream()
                                      .distinct()
                                      .collect(Collectors.toList());

    // There are as many hidden states as there are unique patterns in our observations.
    // We only support patterns we have already seen, so we base this number on the number of different samples seen
    List<HiddenState> hiddenStates = sampleObservations
        .stream()
        .distinct()
        .map(sample -> new HiddenState(sample.getHash()))
        .collect(Collectors.toList());

    // This is the N on the wikipedia article on Baum-Welch
    int numHiddenStates = hiddenStates.size();

    // This is the K on the wikipedia article on Baum-Welch
    //noinspection UnnecessaryLocalVariable
    int numEmissionStates = emissionStates.size();

    int numObservations = sampleObservations.size();

    // we need to check if a model has ever been generated.
    // If not, we generate one with uniform distribution of
    // initial probability, transition matrix and observation matrix

    // this null check is needed when we support reusing an already learnt model. Currently, we discard the old learnt model
    // if(currentModel == null){
      EmissionMatrix initEmissionMatrix = new EmissionMatrix(numEmissionStates, numHiddenStates, observations);
      TransitionMatrix initTransitionMatrix = new TransitionMatrix(numHiddenStates);
      InitialProbability initProbability = new InitialProbability(numHiddenStates);
      currentModel = new HiddenMarkovModel(initProbability, initTransitionMatrix, initEmissionMatrix, hiddenStates);
    //}

    // we are now sure that we have a model.
    // We how want to iteratively apply the Baum-Welch algorithm to the model until we converge on some model.
    HiddenMarkovModel oldModel = currentModel;
    HiddenMarkovModel newModel;
    boolean shouldContinue;
    do{
      newModel = baumWelch(oldModel, observations, emissionStates, numHiddenStates, numEmissionStates, numObservations, hiddenStates);
      shouldContinue = diffModel(oldModel, newModel) > convergenceConstant;
      oldModel = newModel;
    }while (shouldContinue);

    return newModel;
  }

  private HiddenMarkovModel baumWelch(HiddenMarkovModel oldModel, List<Integer> observations, List<Integer> emissionStates,
                                              int numHiddenStates, int numEmissionStates, int numObservations, List<HiddenState> hiddenStates)
  {
    BlockRealMatrix forwards = calcForwardsMatrix(oldModel,observations, numHiddenStates, numObservations);
    BlockRealMatrix backwards = calcBackwardsMatrix(oldModel, observations, numHiddenStates, numObservations);

    BlockRealMatrix gammaMatrix = calcGammaMatrix(forwards, backwards, numHiddenStates, numObservations);
    double[][][] xiMatrix = calcXiMatrix(forwards,backwards,oldModel, observations, numHiddenStates, numObservations);

    InitialProbability newInitProbability = calcNewInitialProbability(gammaMatrix);
    TransitionMatrix newTransitionMatrix = calcNewTransitionMatrix(gammaMatrix,xiMatrix, numHiddenStates, numObservations);
    EmissionMatrix newEmissionMatrix = calcNewEmissionMatrix(gammaMatrix, observations, emissionStates, numHiddenStates, numEmissionStates);

    return new HiddenMarkovModel(newInitProbability, newTransitionMatrix, newEmissionMatrix, hiddenStates);
  }

  private BlockRealMatrix calcForwardsMatrix(HiddenMarkovModel oldModel, List<Integer> observations, int numHiddenStates, int numObservations) {
    BlockRealMatrix forwards = new BlockRealMatrix(numHiddenStates, numObservations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < numObservations; t++)
      {
        double probability = calcForwardsProbability(i,t, oldModel, observations, numHiddenStates);
        forwards.setEntry(i,t,probability);
      }
    }

    return forwards;
  }

  private double calcForwardsProbability(int hiddenStateIndex, int observationIndex,
                                         HiddenMarkovModel oldModel, List<Integer> observations, int numHiddenStates) {
    if(observationIndex == 0) {
      return oldModel.getInitialProbability().getProbability(hiddenStateIndex)
          * oldModel.getEmissionMatrix().getEntry(hiddenStateIndex, observations.get(observationIndex));
    }
    else {
      double sum = 0;
      for (int i = 0; i < numHiddenStates; i++)
      {
        sum += calcForwardsProbability(i, observationIndex - 1, oldModel, observations, numHiddenStates)
            * oldModel.getTransitionMatrix().getEntry(i, hiddenStateIndex);
      }
      return oldModel.getEmissionMatrix().getEntry(hiddenStateIndex, observations.get(observationIndex))
          * sum;
    }
  }

  private BlockRealMatrix calcBackwardsMatrix(HiddenMarkovModel oldModel, List<Integer> observations, int numHiddenStates, int numObservations) {
    BlockRealMatrix backwards = new BlockRealMatrix(numHiddenStates, numObservations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < numObservations; t++)
      {
        double probability = calcBackwardsProbability(i,t, oldModel, observations, numHiddenStates, numObservations);
        backwards.setEntry(i,t,probability);
      }
    }

    return backwards;
  }

  private double calcBackwardsProbability(int hiddenStateIndex, int observationIndex, HiddenMarkovModel oldModel,
                                          List<Integer> observations, int numHiddenStates, int numObservations) {
    // base case is that are at the last observation
    if(observationIndex == numObservations - 1) {
      return 1;
    }
    else {
      double sum = 0;
      for (int j = 0; j < numHiddenStates; j++)
      {
        sum += calcBackwardsProbability(j, observationIndex + 1, oldModel, observations, numHiddenStates, numObservations)
               * oldModel.getTransitionMatrix().getEntry(j, hiddenStateIndex)
               * oldModel.getEmissionMatrix().getEntry(j, observations.get(observationIndex + 1));
      }
      return sum;
    }
  }

  private double[][][] calcXiMatrix(BlockRealMatrix forwards, BlockRealMatrix backwards, HiddenMarkovModel oldModel,
                                    List<Integer> observations, int numHiddenStates, int numObservations) {
    double[][][] xi = new double[numHiddenStates][numHiddenStates][numObservations];

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int j = 0; j < numHiddenStates; j++)
      {
        for (int t = 0; t < numObservations; t++)
        {
          double probability = calcXiProbability(i, j, t, oldModel, observations, forwards, backwards, numHiddenStates, numObservations);
          xi[i][j][t] = probability;
        }
      }
    }

    return xi;
  }

  private double calcXiProbability(int i, int j, int t, HiddenMarkovModel oldModel, List<Integer> observations, BlockRealMatrix forwards,
                                   BlockRealMatrix backwards, int numHiddenStates, int numObservations)
  {
    double denominator = 0;
    for (int k = 0; k < numHiddenStates; k++)
    {
      denominator += forwards.getEntry(k, numObservations - 1);
    }

    double numerator = forwards.getEntry(i, t)
        * oldModel.getTransitionMatrix().getEntry(i, j)
        * backwards.getEntry(j, t+1)
        * oldModel.getEmissionMatrix().getEntry(j, observations.get(t+1));

    return numerator / denominator;
  }

  private BlockRealMatrix calcGammaMatrix(BlockRealMatrix forwards, BlockRealMatrix backwards, int numHiddenStates, int numObservations) {
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

  private TransitionMatrix calcNewTransitionMatrix(BlockRealMatrix gammaMatrix, double[][][] xiMatrix, int numHiddenStates, int numObservations) {
    TransitionMatrix transitionMatrix = new TransitionMatrix(numHiddenStates);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int j = 0; j < numObservations; j++)
      {
        double probability = calcNewTransitionProbability(i, j, xiMatrix, gammaMatrix, numObservations);
        transitionMatrix.setEntry(i,j,probability);
      }
    }

    return transitionMatrix;
  }

  private double calcNewTransitionProbability(int i, int j, double[][][] xiMatrix, BlockRealMatrix gammaMatrix, int numObservations)
  {
    double numerator = 0;
    for (int t = 0; t < numObservations - 1; t++)
    {
      numerator += xiMatrix[i][j][t];
    }
    double denominator = 0;
    for (int t = 0; t < numObservations - 1; t++)
    {
      denominator += gammaMatrix.getEntry(i,t);
    }

    return  numerator / denominator;
  }

  private EmissionMatrix calcNewEmissionMatrix(BlockRealMatrix gammaMatrix, List<Integer> observations, List<Integer> emissionSamples,
                                               int numHiddenStates, int numEmissionStates) {
    EmissionMatrix emissionMatrix = new EmissionMatrix(numEmissionStates,numHiddenStates);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int j = 0; j < numEmissionStates; j++)
      {
        double probability = calcNewEmissionProbability(i, emissionSamples.get(j), gammaMatrix, numEmissionStates, observations);
        emissionMatrix.setEntry(i,j,probability);
      }
    }

    return emissionMatrix;
  }

  private double calcNewEmissionProbability(int i, Integer emissionSample, BlockRealMatrix gammaMatrix, int numObservations, List<Integer> observations)
  {
    double numerator = 0;
    double denominator = 0;
    for (int t = 0; t < numObservations; t++)
    {
      double gammaProbability =  gammaMatrix.getEntry(i,t);
      if(Objects.equals(emissionSample, observations.get(t))) {
        numerator += gammaProbability;
      }
      denominator += gammaProbability;
    }

    return  numerator / denominator;
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
