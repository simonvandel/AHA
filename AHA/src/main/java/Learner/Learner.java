package Learner;

import Sampler.Sample;

import java.util.Collection;

/**
 * Created by simon on 11/30/15.
 */
public class Learner
{
  private HiddenMarkovModel currentModel;

  private final double convergenceConstant = 0.001;

  public HiddenMarkovModel learn(Collection<Sample> samples){
    // we need to check if a model has ever been generated.
    // If not, we generate one with uniform distribution of
    // initial probability, transition matrix and observation matrix
    if(currentModel == null){
      // the number of hidden states are the unique number of patterns.
      // We only support patterns we have already seen, so we base this number on the number of different samples seen
      long numHiddenStates = samples.stream().distinct().count();

      // the number of observation variables is how many different snapshots can occur.
      // Fx. if a snapshot contained 2 sensors and every sensor could be 1024 values,
      // there could be 1024^2 different snapshots
      long numObservationVariables = 0; // TODO: right now, there is no way to know from the learner how many sensors we have and the values each sensor can be
      ObservationMatrix initObservationMatrix = new ObservationMatrix(numHiddenStates, numObservationVariables);
      TransitionMatrix initTransitionMatrix = new TransitionMatrix();
      InitialProbability initProbability = new InitialProbability();
      currentModel = new HiddenMarkovModel(initProbability, initTransitionMatrix, initObservationMatrix);
    }

    // we are now sure that we have a model.
    // We how want to iteratively apply the Baum-Welch algorithm to the model until we converge on some model.
    HiddenMarkovModel oldModel = currentModel;
    HiddenMarkovModel newModel = null;
    boolean shouldContinue = true;
    do{
      newModel = baumWelch(oldModel, samples);
      shouldContinue = diffModel(oldModel, newModel) > convergenceConstant;
      oldModel = newModel;
    }while (shouldContinue);

    return newModel;
  }

  private HiddenMarkovModel baumWelch(HiddenMarkovModel oldModel, Collection<Sample> samples)
  {
    InitialProbability newInitProbability = null; // TODO
    TransitionMatrix newTransitionMatrix = null; // TODO
    ObservationMatrix newObervationMatrix = null; // TODO
    return new HiddenMarkovModel(newInitProbability, newTransitionMatrix, newObervationMatrix);
  }

  /**
   * @param model1 Model 1
   * @param model2 Model 2
   * @return Calculates the similarity between two models. Can be used to tell how similar two models are.
   */
  private double diffModel(HiddenMarkovModel model1, HiddenMarkovModel model2)
  {
    return model1.getInitialProbability().getNorm() - model2.getInitialProbability().getNorm()
             - model1.getObservationMatrix().getNorm() - model2.getObservationMatrix().getNorm()
             - model1.getTransitionMatrix().getNorm() - model2.getTransitionMatrix().getNorm();
  }
}
