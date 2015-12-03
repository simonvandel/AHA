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
  private List<HiddenState> hiddenStates;

  public HiddenMarkovModel(InitialProbability initialProbability, TransitionMatrix transitionMatrix, EmissionMatrix emissionMatrix, List<HiddenState> hiddenStates)
  {
    this.initialProbability = initialProbability;
    this.transitionMatrix = transitionMatrix;
    this.emissionMatrix = emissionMatrix;
    this.hiddenStates = hiddenStates;
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
    List<HiddenState> mostLikelyHiddenStates = viterbi(s.getHash());
    // 2. find the most likely transition h will make. call This state h2
    //transitionMatrix.getEntry()

    // 3. find the emission state e that h2 will most likely emit
    // 4. Compare s and e, and make an action based of changes of the two

    // 1. kig i emission matrix efter rækken der indeholder s. Find højest probability og vælg det som dit h
    // 2. kig i transition matrix rækk
    return null;
  }

  private List<HiddenState> viterbi(List<Integer> sampleHashes)
  {
    // we only need to look back the sample size, because the Markov property dictates that older history is not relevant
    List<HiddenState> path = new ArrayList<>(sampleHashes.size());

    List<List<Pair<Double, HiddenState>>> VList = new ArrayList<>(sampleHashes.size());
    List<Pair<Double, HiddenState>> probabilities = new ArrayList<>(hiddenStates.size());

    for (int k = 0; k < probabilities.size(); k++)
    {
      double value = emissionMatrix.getEntry(k, sampleHashes.get(0)) * initialProbability.getProbability(k);
      probabilities.set(k,Pair.with(value, hiddenStates.get(k)));
    }

    VList.set(0, new ArrayList<>(probabilities)); // if debugging, check if probabilities is COPIED and not referenced
    for (int t = 1; t < sampleHashes.size(); t++)
    {
      probabilities = new ArrayList<>(hiddenStates.size());

      for (int k = 0; k < probabilities.size(); k++)
      {
        double maxProbability = 0;
        HiddenState maxState = null;
        double factor = emissionMatrix.getEntry(k, sampleHashes.get(t));
        for (int x = 0; x < hiddenStates.size(); x++)
        {
          double tempProb = factor * transitionMatrix.getEntry(x,k) * VList.get(t-1).get(x).getValue0();
          if (tempProb > maxProbability) {
            maxProbability = tempProb;
            maxState = hiddenStates.get(x);
          }
        }
        probabilities.set(k, Pair.with(maxProbability, maxState));
      }
      VList.set(t, new ArrayList<>(probabilities));
    }

    // set hidden state i T observation
    path.set(path.size() - 1, VList.get(sampleHashes.size() - 1).stream().max((pair1, pair2) -> pair1.getValue0().compareTo(pair2.getValue0())).get().getValue1());

    for (int i = VList.size() - 1; i > 0; i--)
    {
      HiddenState h = null; // TODO
      path.set(i-1, h);
    }

    return null; // TODO
  }

  @Override
  public void TakeFeedback(Action a1, Action a2)
  {

  }
}
