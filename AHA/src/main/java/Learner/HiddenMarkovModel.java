package Learner;

import Reasoner.IModel;
import Sampler.Action;
import Sampler.Sample;

/**
 * Created by simon on 11/30/15.
 */
public class HiddenMarkovModel implements IModel
{

  private InitialProbability initialProbability;
  private TransitionMatrix transitionMatrix;
  private EmissionMatrix emissionMatrix;

  public HiddenMarkovModel(InitialProbability initialProbability, TransitionMatrix transitionMatrix, EmissionMatrix emissionMatrix)
  {
    this.initialProbability = initialProbability;
    this.transitionMatrix = transitionMatrix;
    this.emissionMatrix = emissionMatrix;
  }

  public InitialProbability getInitialProbability()
  {
    return initialProbability;
  }

  public TransitionMatrix getTransitionMatrix()
  {
    return transitionMatrix;
  }

  public EmissionMatrix getEmissionMatrix()
  {
    return emissionMatrix;
  }

  public int getNumHiddenStates()
  {
    return transitionMatrix.getNumberOfHiddenStates();
  }
  public int getNumEmissionVariables()
  {
    return emissionMatrix.getNumEmissionVariables();
  }

  @Override
  public Action CalculateAction(Sample s)
  {
    // 1. use decoding problem solution to find the most likely hidden state h that emitted the sample s
    HiddenState ht;
    // 2. find the most likely transition h will make. call This state h2
    //transitionMatrix.getEntry()

    // 3. find the emission state e that h2 will most likely emit
    // 4. Compare s and e, and make an action based of changes of the two

    // 1. kig i emission matrix efter rækken der indeholder s. Find højest probability og vælg det som dit h
    // 2. kig i transition matrix rækk
    return null;
  }

  @Override
  public void TakeFeedback(Action a1, Action a2)
  {

  }
}
