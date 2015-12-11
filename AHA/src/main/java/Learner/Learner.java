package Learner;

import Sampler.Sample;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by simon on 11/30/15.
 */
public class Learner
{
  private HiddenMarkovModel currentModel;

  private final double convergenceConstant = 0.001e-15; // TODO: change to suitable number

  public HiddenMarkovModel learn(List<Sample> sampleObservations){

    MapWarden mapWarden = new MapWarden(sampleObservations);

    // we need to check if a model has ever been generated.
    // If not, we generate one with uniform distribution of
    // initial probability, transition matrix and observation matrix


    /*EmissionMatrix initEmissionMatrix = new EmissionMatrix(mapWarden);
    TransitionMatrix initTransitionMatrix = new TransitionMatrix(mapWarden);
    InitialProbability initProbability = new InitialProbability(mapWarden);*/
    double[][] randomEmissionValues = new double[mapWarden.getNumHiddenStates()][mapWarden.getNumObservations()];

    int startRemaining = mapWarden.getNumObservations()*10;
    Random r = new Random(0); // TODO: seed er sat til 0
    for (int row = 0; row < randomEmissionValues.length; row++){
      int remaining = startRemaining;
      for (int col = 0; col < randomEmissionValues[row].length - 1; col++){
        int tempRes = r.nextInt(remaining - (randomEmissionValues[row].length - col) - 1) + 1;
        remaining -= tempRes;
        double res = tempRes / ((double) startRemaining);
        randomEmissionValues[row][col] = res;
      }
      randomEmissionValues[row][randomEmissionValues[row].length - 1] = remaining / ((double) startRemaining);
    }

    EmissionMatrix initEmissionMatrix = new EmissionMatrix(mapWarden, randomEmissionValues);
    //EmissionMatrix initEmissionMatrix = new EmissionMatrix(mapWarden);
    TransitionMatrix initTransitionMatrix = new TransitionMatrix(mapWarden);
    InitialProbability initProbability = new InitialProbability(mapWarden);
    currentModel = new HiddenMarkovModel(initProbability, initTransitionMatrix, initEmissionMatrix, mapWarden);

    // we are now sure that we have a model.
    // We how want to iteratively apply the Baum-Welch algorithm to the model until we converge on some model.
    HiddenMarkovModel oldModel = baumWelch(currentModel, mapWarden);
    HiddenMarkovModel newModel = null;

    int maxIters = 100;
    int iters = 0;
    double oldLogProb = Double.NEGATIVE_INFINITY;

    boolean shouldContinue = true;
    while (shouldContinue) {
      iters++;
      double logProb = computeLog(mapWarden);

      if (iters < maxIters && logProb > oldLogProb) {
        oldLogProb = logProb;
        newModel = baumWelch(oldModel, mapWarden);
        shouldContinue = true;
      } else {
        shouldContinue = false;
      }

      /*double newDiff = diffModel(oldModel, newModel);
      double percentageChange = (newDiff - previousDiff) / newDiff;
      shouldContinue = Math.abs(percentageChange) > convergenceConstant;
      previousDiff = newDiff;
      oldModel = newModel;*/
    }

    return newModel;
  }

  private double computeLog(MapWarden mapWarden){
    double logProb = 0;
    for (int i = 0; i <= mapWarden.getNumObservations() - 1; i++){
      double ci = mapWarden.getScalingFactor(i);
      logProb += Math.log(ci);
    }
    return -logProb;
  }

  private HiddenMarkovModel baumWelch(HiddenMarkovModel oldModel, MapWarden mapWarden)
  {
    ForwardsMatrix forwards = new ForwardsMatrix(oldModel, mapWarden);
    BackwardsMatrix backwards = new BackwardsMatrix(oldModel, mapWarden);

    GammaMatrix gammaMatrix = new GammaMatrix(oldModel, forwards, backwards, mapWarden);
    //XiMatrix xiMatrix = new XiMatrix(forwards, backwards, oldModel, mapWarden);

    InitialProbability newInitProbability = new InitialProbability(mapWarden, gammaMatrix);
    TransitionMatrix newTransitionMatrix = new TransitionMatrix(mapWarden, gammaMatrix);
    EmissionMatrix newEmissionMatrix = new EmissionMatrix(gammaMatrix, mapWarden);

    return new HiddenMarkovModel(newInitProbability, newTransitionMatrix, newEmissionMatrix, mapWarden);
  }

/*  *//**
   * @param model1 Model 1
   * @param model2 Model 2
   * @return Calculates the similarity between two models. Can be used to tell how similar two models are.
   *//*
  private double diffModel(HiddenMarkovModel model1, HiddenMarkovModel model2)
  {
    double value = Math.abs(
        (model1.getInitialProbability().getNorm() - model2.getInitialProbability().getNorm())
             + (model1.getEmissionMatrix().getNorm() - model2.getEmissionMatrix().getNorm())
             + (model1.getTransitionMatrix().getNorm() - model2.getTransitionMatrix().getNorm()));
    return value;
  }*/
}
