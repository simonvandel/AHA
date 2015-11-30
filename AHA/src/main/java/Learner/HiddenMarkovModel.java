package Learner;

/**
 * Created by simon on 11/30/15.
 */
public class HiddenMarkovModel
{

  private InitialProbability initialProbability;
  private TransitionMatrix transitionMatrix;
  private ObservationMatrix observationMatrix;

  public HiddenMarkovModel(InitialProbability initialProbability, TransitionMatrix transitionMatrix, ObservationMatrix observationMatrix)
  {
    this.initialProbability = initialProbability;
    this.transitionMatrix = transitionMatrix;
    this.observationMatrix = observationMatrix;
  }

  public InitialProbability getInitialProbability()
  {
    return initialProbability;
  }

  public TransitionMatrix getTransitionMatrix()
  {
    return transitionMatrix;
  }

  public ObservationMatrix getObservationMatrix()
  {
    return observationMatrix;
  }
}
