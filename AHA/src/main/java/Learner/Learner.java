package Learner;

import Sampler.Sample;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.List;
import java.util.Map;
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

    MapWarden mapWarden = new MapWarden(sampleObservations);

    // This is the N on the wikipedia article on Baum-Welch
    int numHiddenStates = mapWarden.getNumHiddenStates();

    // This is the K on the wikipedia article on Baum-Welch
    //noinspection UnnecessaryLocalVariable
    int numEmissionStates = mapWarden.getNumEmissionStates();

    int numObservations = mapWarden.getNumObservations();

    // we need to check if a model has ever been generated.
    // If not, we generate one with uniform distribution of
    // initial probability, transition matrix and observation matrix

    // this null check is needed when we support reusing an already learnt model. Currently, we discard the old learnt model
    // if(currentModel == null){
      EmissionMatrix initEmissionMatrix = new EmissionMatrix(mapWarden);
      TransitionMatrix initTransitionMatrix = new TransitionMatrix(mapWarden);
      InitialProbability initProbability = new InitialProbability(mapWarden);
      currentModel = new HiddenMarkovModel(initProbability, initTransitionMatrix, initEmissionMatrix, mapWarden);
    //}

    // we are now sure that we have a model.
    // We how want to iteratively apply the Baum-Welch algorithm to the model until we converge on some model.
    HiddenMarkovModel oldModel = currentModel;
    HiddenMarkovModel newModel;
    boolean shouldContinue;
    do{
      newModel = baumWelch(oldModel, mapWarden);
      shouldContinue = diffModel(oldModel, newModel) > convergenceConstant;
      oldModel = newModel;
    }while (shouldContinue);

    return newModel;
  }

  private HiddenMarkovModel baumWelch(HiddenMarkovModel oldModel, MapWarden mapWarden)
  {
    ForwardsMatrix forwards = new ForwardsMatrix(oldModel, mapWarden); //calcForwardsMatrix(oldModel,observations, numHiddenStates, numObservations);
    BackwardsMatrix backwards = new BackwardsMatrix(oldModel, mapWarden); //calcBackwardsMatrix(oldModel, observations, numHiddenStates, numObservations);

    GammaMatrix gammaMatrix = new GammaMatrix(forwards, backwards, mapWarden); //calcGammaMatrix(forwards, backwards, numHiddenStates, numObservations);
    XiMatrix xiMatrix = new XiMatrix(forwards, backwards, oldModel, mapWarden); // calcXiMatrix(forwards,backwards,oldModel, observations, numHiddenStates, numObservations);

    InitialProbability newInitProbability = new InitialProbability(mapWarden, gammaMatrix);
    TransitionMatrix newTransitionMatrix = new TransitionMatrix(mapWarden, gammaMatrix, xiMatrix) // calcNewTransitionMatrix(gammaMatrix,xiMatrix, numHiddenStates, numObservations);
    EmissionMatrix newEmissionMatrix = calcNewEmissionMatrix(gammaMatrix, observations, emissionStates, numHiddenStates, numEmissionStates);

    return new HiddenMarkovModel(newInitProbability, newTransitionMatrix, newEmissionMatrix, hiddenStates);
  }




  private EmissionMatrix calcNewEmissionMatrix(BlockRealMatrix gammaMatrix, List<Integer> observations, List<EmissionState> emissionStates, List<HiddenState> hiddenStates) {
    EmissionMatrix emissionMatrix = new EmissionMatrix(emissionStates,hiddenStates);

    for (HiddenState i :
        hiddenStates)
    {
      for (EmissionState j :
          emissionStates)
      {
        double probability = calcNewEmissionProbability(i, j, gammaMatrix, observations);
        emissionMatrix.setEntry(i,j,probability);
      }
    }

    return emissionMatrix;
  }

  private double calcNewEmissionProbability(HiddenState i, Integer emissionSample, BlockRealMatrix gammaMatrix, List<Integer> observations)
  {
    double numerator = 0;
    double denominator = 0;
    for (Integer t: observations)
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
