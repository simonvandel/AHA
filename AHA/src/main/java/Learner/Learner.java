package Learner;

import Sampler.Sample;
import org.apache.commons.math3.linear.BlockRealMatrix;

import java.util.ArrayList;
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

  private BlockRealMatrix calcForwards(HiddenMarkovModel oldModel, List<Sample> samples) {
    int numHiddenStates = oldModel.getNumHiddenStates();
    int observations = samples.size();
    BlockRealMatrix forwards = new BlockRealMatrix(numHiddenStates, observations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < observations; t++)
      {
        double probability = calcForwardsRec(i,t, oldModel, samples);
        forwards.setEntry(i,t,probability);
      }
    }
    
    return forwards;
  }

  private double calcForwardsRec(int hiddenStateIndex, int observationIndex, HiddenMarkovModel oldModel, List<Sample> samples) {
    if(observationIndex == 0) {
      return oldModel.getInitialProbability().getProbability(hiddenStateIndex)
          * oldModel.getEmissionMatrix().getEntry(hiddenStateIndex,samples.get(observationIndex));
    }
    else {
      double sum = 0;
      int numHiddenStates = oldModel.getNumHiddenStates();
      for (int i = 0; i < numHiddenStates; i++)
      {
        sum += calcForwardsRec(i, observationIndex - 1, oldModel, samples) * oldModel.getTransitionMatrix().getEntry(i, observationIndex);
      }
      return oldModel.getEmissionMatrix().getEntry(hiddenStateIndex, samples.get(observationIndex)) * sum;
    }
  }

  private BlockRealMatrix calcBackwards(HiddenMarkovModel oldModel, List<Sample> samples) {

    int numHiddenStates = oldModel.getNumHiddenStates();
    int observations = samples.size();
    BlockRealMatrix backwards = new BlockRealMatrix(numHiddenStates, observations);

    for (int i = 0; i < numHiddenStates; i++)
    {
      for (int t = 0; t < observations; t++)
      {
        double probability = calcBackwardsRec(i,t, oldModel, samples);
        backwards.setEntry(i,t,probability);
      }
    }

    return backwards;
  }

  private double calcBackwardsRec(int hiddenStateIndex, int observationIndex, HiddenMarkovModel oldModel, List<Sample> samples) {
    // base case is that are at the last observation
    if(observationIndex == samples.size() - 1) {
      return 1;
    }
    else {
      double sum = 0;
      int numHiddenStates = oldModel.getNumHiddenStates();
      for (int j = 0; j < numHiddenStates; j++)
      {
        sum += calcBackwardsRec(j, observationIndex + 1, oldModel, samples)
               * oldModel.getTransitionMatrix().getEntry(j, observationIndex)
               * oldModel.getEmissionMatrix().getEntry(j, samples.get(observationIndex + 1));
      }
      return sum;
    }
  }

  private BlockRealMatrix calcXi(BlockRealMatrix forwards, BlockRealMatrix backwards, HiddenMarkovModel oldModel, Collection<Sample> samples) {
    return null;
  }

  private BlockRealMatrix calcGamma(BlockRealMatrix forwards, BlockRealMatrix backwards) {
    /*// calculate gamma values. gammaValues.get(i).get(t) == gamma_i(t)
    List<List<Double>> gammaValues = new ArrayList<>();
    for (int i = 0; i < hiddenStates.size(); i++)
    {
      List<Double> innerList = new ArrayList<>();
      for (int t = 0; t < observations.size(); t++)
      {
        double res = (forwards.get(i).get(t) * backwards.get(i).get(t)) / ();
        innerList.add(res);
      }
      gammaValues.add(i,innerList);
    }*/
    return null;
  }

  private InitialProbability calcNewInitialProbability(){
    return  null;
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
