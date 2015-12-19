package Learner;

import Reasoner.IModel;
import Reasoner.Reasoning;
import Sampler.Action;
import Sampler.Sample;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by simon on 11/30/15.
 */
public class HiddenMarkovModel implements IModel{
  private Logger logger;
  private InitialProbability initialProbability;
  private TransitionMatrix transitionMatrix;
  private EmissionMatrix emissionMatrix;
  private MapWarden mapWarden;
  private final double someThreshold = 0.6; // TODO change threshold

  public HiddenMarkovModel(InitialProbability initialProbability, TransitionMatrix transitionMatrix, EmissionMatrix emissionMatrix, MapWarden mapWarden,Logger aiLogger){
    this.initialProbability = initialProbability;
    this.transitionMatrix = transitionMatrix;
    this.emissionMatrix = emissionMatrix;
    this.mapWarden = mapWarden;
    this.logger = aiLogger;
  }

  public InitialProbability getInitialProbability(){
    return initialProbability;
  }

  public TransitionMatrix getTransitionMatrix(){
    return transitionMatrix;
  }

  public EmissionMatrix getEmissionMatrix(){
    return emissionMatrix;
  }

  @Override
  public Reasoning CalculateReasoning(Sample s){
    List<Observation> observationsFromSample = s.getHash().stream().map(observationHashcode -> new Observation(observationHashcode)).collect(Collectors.toList());

    List<HiddenState> viterbiPath = viterbi(observationsFromSample);
    // if the viterbiPath is null, it means something was wrong in the viterbi algorithm, so we can not predict the next action
    if (viterbiPath == null){
      return null;
    }

    // we find the confidence by summing all probabilities involved in our reasoning, and then dividing by the number of sums.
    // All probabilities involved are:
    // * the viterbi path
    // * the probability to transition from the last state in the viterbi pi to the next state that is most probable.
    // * the the probability to emit the most probable evidence variable
    double confidence = 0;
    int sums = 0;

    NormalisedTransitionMatrix normalisedTransitionMatrix = transitionMatrix.normalise();
    NormalisedEmissionMatrix normalisedEmissionMatrix = emissionMatrix.normalise();

    for (int pathIndex = 0; pathIndex < viterbiPath.size(); pathIndex++){
      HiddenState currentHiddenState = viterbiPath.get(pathIndex);

      // check if we reached the end of the list
      if (pathIndex != viterbiPath.size() - 1){
        HiddenState nextHiddenState = viterbiPath.get(pathIndex + 1);

        if (nextHiddenState != null){
          confidence += normalisedTransitionMatrix.getEntry(currentHiddenState, nextHiddenState);
          sums += 1;
        }
      }
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
    if (confidence > someThreshold){
      List<HiddenState> hiddenStatesPath = new ArrayList<>(viterbiPath);
      hiddenStatesPath.add(finalHiddenState);
      EmissionState emissionStatePredicted = emissionPair.getValue0();
      List<EmissionState> emissionStatesGenerated = new ArrayList<>();
      emissionStatesGenerated.addAll(observationsFromSample.stream().map(o -> mapWarden.observationToEmission(o)).collect(Collectors.toList()));
      emissionStatesGenerated.add(emissionStatePredicted);
      List<Action> actions = new ArrayList<>();
      for(Action aPrev : s.getActions()) {
        for(Action aNext: emissionStatePredicted.getActions()) {
          if(aPrev.getDevice() == aNext.getDevice()) {
            if(aPrev.getValTo() != aNext.getValTo()){
              actions.add(new Action(aPrev.getValTo(), aNext.getValTo(), aNext.getDevice()));
            }
          }
        }
      }
      Reasoning reasoning = new Reasoning(actions, hiddenStatesPath, emissionStatesGenerated);
      String log = "";
      log += "Confidence: " + confidence;
      if (reasoning.getActions().size() > 0){
        log += ". Actions: ";
        for (Action a : reasoning.getActions())
          log += "\n" + a.toString();
      }
      logger.log(Level.INFO, log);
      return reasoning;
    } else{
      // we are not confident enough, so return a null action
      logger.log(Level.INFO, "No Confidence: " + confidence);
      return null;
    }
  }

  private List<HiddenState> viterbi(List<Observation> observationHashes){
    int numHiddenStates = mapWarden.getNumHiddenStates();
    //List<HiddenState> hiddenStates = mapWarden.gethi
    // we only need to look back the sample size, because the Markov property dictates that older history is not relevant
    List<HiddenState> path = new ArrayList<>(observationHashes.size());

    // initialize path list
    for (int i = 0; i < observationHashes.size(); i++){
      path.add(i, null);
    }

    // the VList stores the probability calculations already calculated, to reduce the amount of work to duplicate.
    // VList.get(t).get(k) returns a tuple (v1,v2)
    // where v1 is the probability that the t th observation was emitted by the k th hidden state
    // and v2 is the k hidden state as Hidden State type
    List<List<Pair<Double, HiddenState>>> VList = new ArrayList<>(observationHashes.size());
    List<Pair<Double, HiddenState>> probabilities = new ArrayList<>(numHiddenStates);


    Observation firstObservation = observationHashes.get(0);
    mapWarden.addObservationMapping(firstObservation);
    EmissionState firstEmissionState = mapWarden.observationToEmission(firstObservation);
    if (firstEmissionState == null){
      logger.log(Level.SEVERE, "Observed something new, in viterbi method got null. Which infers either an error or a new observation");
      return null;
    }
    // Calculate the probabilities that each hidden state k emitted the first observed value.
    for (HiddenState i : mapWarden.iterateHiddenStates()){
      // taking the log probability
      double value = Math.log(initialProbability.getProbability(i) * emissionMatrix.getEntry(i, firstEmissionState));
      int iIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
      probabilities.add(iIndex, Pair.with(value, i));
    }

    // copy the probabilities calculated for the first observation value to VList
    VList.add(0, new ArrayList<>(probabilities));

    // t is the t th observation
    for (int t = 1; t < observationHashes.size(); t++){
      // clear previously calculated probabilities, to be ready for the next round
      probabilities.clear();

      // k is the k th hidden state
      for (HiddenState i : mapWarden.iterateHiddenStates()){
        double maxProbability = Double.NEGATIVE_INFINITY;
        HiddenState maxState = null;
        Observation tObservation = observationHashes.get(t); //mapWarden.observationIndexToObservation(t);
        mapWarden.addObservationMapping(tObservation);
        EmissionState tEmissionState = mapWarden.observationToEmission(tObservation);

        if (tEmissionState == null){
          // we have never seen an emission like that before, so we can not reason about it
          return null;
        }

        // we take the log of the probability
        double factor = Math.log(emissionMatrix.getEntry(i, tEmissionState));

        // x is the x th hidden state
        for (HiddenState j : mapWarden.iterateHiddenStates()){
          int jIndex = mapWarden.hiddenStateToHiddenStateIndex(j);
          double tempProb = VList.get(t - 1).get(jIndex).getValue0() + Math.log(transitionMatrix.getEntry(j, i)) + factor;
          if (tempProb > maxProbability){
            maxProbability = tempProb;
            maxState = j;
          }
        }
        int kIndex = mapWarden.hiddenStateToHiddenStateIndex(i);
        // store what the maximum probability was to emit observation t from hidden state k, as well as k represented as a hidden state type
        probabilities.add(kIndex, Pair.with(maxProbability, maxState));
      }

      VList.add(t, new ArrayList<>(probabilities));
    }

    // find the most probable state at the final observation
    HiddenState finalHiddenState = VList.get(observationHashes.size() - 1).stream().max((pair1, pair2) -> pair1.getValue0().compareTo(pair2.getValue0())).get().getValue1();

    // set hidden state i T observation
    path.set(path.size() - 1, finalHiddenState);

    // follow backpointers to find the whole sequence of most probable hidden states
    for (int i = VList.size() - 1; i > 0; i--){
      HiddenState previousH = path.get(i);
      int previousHIndex = mapWarden.hiddenStateToHiddenStateIndex(previousH);
      HiddenState h = VList.get(i).get(previousHIndex).getValue1();
      path.set(i - 1, h);
    }

    return path;

  }

  @Override
  public void TakeFeedback(Reasoning wrongReasoning){
    logger.log(Level.INFO, "TakeFeedback called with wrong actions: " + wrongReasoning.toString());
    if(wrongReasoning.getHiddenStates().size() == wrongReasoning.getEmissions().size()){
      int lastIndex = wrongReasoning.getHiddenStates().size() - 1;
      HiddenState wrongHiddenState = wrongReasoning.getHiddenStates().get(lastIndex);
      EmissionState wrongEmissionState = wrongReasoning.getEmissions().get(lastIndex);
      getEmissionMatrix().getEntry(wrongHiddenState, wrongEmissionState);

      // set the probability that currentHiddenState emits currentEmissionState to half of the current probability, as it was the wrong thing to do
      double newEmissionProbability = getEmissionMatrix().getEntry(wrongHiddenState, wrongEmissionState) * 0.5;
      emissionMatrix.setProbabilityAndNormalise(newEmissionProbability, wrongHiddenState, wrongEmissionState);
    } else
      logger.log(Level.SEVERE, "Wrong number of emissions states and hidden states are not the same in reasoning");
    /*for (int i = 0; i < wrongReasoning.getHiddenStates().size(); i++){
      HiddenState currentHiddenState = wrongReasoning.getHiddenStates().get(i);
      EmissionState currentEmission = wrongReasoning.getEmissions().get(i);
      HiddenState nextHiddenState;

      // check that we have not reached the end
      if (i != wrongReasoning.getHiddenStates().size() - 1){
        nextHiddenState = wrongReasoning.getHiddenStates().get(i + 1);
      } else{
        break;
      }

      // set the probability that currentHiddenState emits currentEmissionState to half of the current probability, as it was the wrong thing to do
      double newEmissionProbability = getEmissionMatrix().getEntry(currentHiddenState, currentEmission) * 0.5;
      emissionMatrix.setProbabilityAndNormalise(newEmissionProbability, currentHiddenState, currentEmission);

      double newTransitionProbability = getTransitionMatrix().getEntry(currentHiddenState, nextHiddenState) * 0.5;
      // set the probability that currenHiddenState transitions to nextHiddenState to half of the current probability
      transitionMatrix.setProbabilityAndNormalise(newTransitionProbability, currentHiddenState, nextHiddenState);
    }*/

  }
}
