package Learner;

/**
 * Created by simon on 11/30/15.
 */
public class HiddenMarkovModel
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
}
