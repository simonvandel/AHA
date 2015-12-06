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
    ForwardsMatrix forwards = new ForwardsMatrix(oldModel, mapWarden);
    BackwardsMatrix backwards = new BackwardsMatrix(oldModel, mapWarden);

    GammaMatrix gammaMatrix = new GammaMatrix(forwards, backwards, mapWarden);
    XiMatrix xiMatrix = new XiMatrix(forwards, backwards, oldModel, mapWarden);

    InitialProbability newInitProbability = new InitialProbability(mapWarden, gammaMatrix);
    TransitionMatrix newTransitionMatrix = new TransitionMatrix(mapWarden, gammaMatrix, xiMatrix);
    EmissionMatrix newEmissionMatrix = new EmissionMatrix(gammaMatrix, mapWarden);

    return new HiddenMarkovModel(newInitProbability, newTransitionMatrix, newEmissionMatrix, mapWarden);
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
