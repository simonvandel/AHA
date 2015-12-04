package Learner;

import Reasoner.IModel;
import Sampler.Action;
import Sampler.Sample;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 11/30/15.
 */
public class HiddenMarkovModel implements IModel
{

  private InitialProbability initialProbability;
  private TransitionMatrix transitionMatrix;
  private EmissionMatrix emissionMatrix;
  private MapWarden mapWarden;
  private final double someThreshold = 0.50; // TODO change threshold

  public HiddenMarkovModel(InitialProbability initialProbability, TransitionMatrix transitionMatrix, EmissionMatrix emissionMatrix, MapWarden mapWarden)
  {
    this.initialProbability = initialProbability;
    this.transitionMatrix = transitionMatrix;
    this.emissionMatrix = emissionMatrix;
    this.mapWarden = mapWarden;
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
    List<HiddenState> viterbiPath = viterbi(s.getHash());

    // we find the confidence by summing all probabilities involved in our reasoning, and then dividing by the number of sums.
    // All probabilities involved are:
    // * the viterbi path
    // * the probability to transition from the last state in the viterbi pi to the next state that is most probable.
    // * the the probability to emit the most probable evidence variable
    double confidence = 0;
    int sums = 0;

    NormalisedTransitionMatrix normalisedTransitionMatrix = transitionMatrix.normalise();
    NormalisedEmissionMatrix normalisedEmissionMatrix = emissionMatrix.normalise();

    for (int pathIndex = 0; pathIndex < viterbiPath.size(); pathIndex++)
    {
      HiddenState currentHiddenState = viterbiPath.get(pathIndex);

      HiddenState nextHiddenState = viterbiPath.get(pathIndex + 1);

      confidence += normalisedTransitionMatrix.getEntry(currentHiddenState, nextHiddenState);
      sums += 1;
    }

    HiddenState finalHiddenState = viterbiPath.get(viterbiPath.size() - 1);

    // find the most probable next state index from the final hidden state, and the probability to make the transition
    Pair<HiddenState, Double> transitionPair = normalisedTransitionMatrix.mostProbableTransitionFrom(finalHiddenState);
    confidence += transitionPair.getValue1();
    sums += 1;

    Pair<EmissionState, Double> emissionPair = normalisedEmissionMatrix.mostProbableTransitionFrom(transitionPair.getValue0());
    confidence += emissionPair.getValue1();
    sums += 1;

    // do the actual calculation of confidence.
    confidence = confidence / sums;

    // check whether we are confident enough to perform the action
    if (confidence > someThreshold) {
      EmissionState emissionStatePredicted = emissionPair.getValue0();
      // TODO: calculate an action. Might have a problem because how do we calculate an action if all we have is hash code of sensor value
      // TODO: An action must not only be calculated based on the transition from the previous state. It has to be computed based the values of all emulatable sensor values
    }
    else {
      // we are not confident enough, so return a null action
      return null;
    }
    return null;
  }

  private List<HiddenState> viterbi(List<Observation> observationHashes)
  {
    int numHiddenStates = mapWarden.getNumHiddenStates();
    //List<HiddenState> hiddenStates = mapWarden.gethi
    // we only need to look back the sample size, because the Markov property dictates that older history is not relevant
    List<HiddenState> path = new ArrayList<>(observationHashes.size());

    // the VList stores the probability calculations already calculated, to reduce the amount of work to duplicate.
    // VList.get(t).get(k) returns a tuple (v1,v2)
    // where v1 is the probability that the t th observation was emitted by the k th hidden state
    // and v2 is the k hidden state as Hidden State type
    List<List<Pair<Double, HiddenState>>> VList = new ArrayList<>(observationHashes.size());
    List<Pair<Double, HiddenState>> probabilities = new ArrayList<>(numHiddenStates);


    // Calculate the probabilities that each hidden state k emitted the first observed value.
    for (HiddenState k: mapWarden.iterateHiddenStates())
    {
      EmissionState emissionState = mapWarden.observationToEmission(observationHashes.get(0));
      if (emissionState)
      double value = emissionMatrix.getEntry(k, ) * initialProbability.getProbability(k);
      int kIndex = mapWarden.hiddenStateToHiddenStateIndex(k);
      probabilities.set(kIndex,Pair.with(value, k));
    }

    // copy the probabilities calculated for the first observation value to VList
    VList.set(0, new ArrayList<>(probabilities));

    // t is the t th observation
    for (int t = 1; t < observationHashes.size(); t++)
    {
      // clear previously calculated probabilities, to be ready for the next round
      probabilities.clear();

      // k is the k th hidden state
      for (HiddenState k: mapWarden.iterateHiddenStates())
      {
        double maxProbability = 0;
        HiddenState maxState = null;
        double factor = emissionMatrix.getEntry(k, mapWarden.observationToEmission(mapWarden.observationIndexToObservation(t)));

        // x is the x th hidden state
        for (HiddenState x: mapWarden.iterateHiddenStates())
        {
          int xIndex = mapWarden.hiddenStateToHiddenStateIndex(x);
          double tempProb = factor * transitionMatrix.getEntry(x,k) * VList.get(t-1).get(xIndex).getValue0();
          if (tempProb > maxProbability) {
            maxProbability = tempProb;
            maxState = x;
          }
        }
        int kIndex = mapWarden.hiddenStateToHiddenStateIndex(k);
        // store what the maximum probability was to emit observation t from hidden state k, as well as k represented as a hidden state type
        probabilities.set(kIndex, Pair.with(maxProbability, maxState));
      }

      VList.set(t, new ArrayList<>(probabilities));
    }

    // find the most probable state at the final observation
    HiddenState finalHiddenState = VList.get(observationHashes.size() - 1)
        .stream()
        .max((pair1, pair2) -> pair1.getValue0().compareTo(pair2.getValue0()))
        .get().getValue1();

    // set hidden state i T observation
    path.set(path.size() - 1, finalHiddenState);

    // follow backpointers to find the whole sequence of most probable hidden states
    for (int i = VList.size() - 1; i > 0; i--)
    {
      HiddenState previousH = path.get(i);
      int previousHIndex = mapWarden.hiddenStateToHiddenStateIndex(previousH);
      HiddenState h = VList.get(i).get(previousHIndex).getValue1();
      path.set(i-1, h);
    }

    return path;
  }

  @Override
  public void TakeFeedback(Action a1, Action a2)
  {

  }
}
