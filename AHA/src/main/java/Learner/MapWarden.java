package Learner;

import Sampler.Sample;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by simon on 04/12/2015.
 */
public class MapWarden // dun dun du du dun dun
{
  private final List<Observation> observations;
  private List<HiddenState> hiddenStates;
  private List<EmissionState> emissionStates;
  private HashMap<HiddenState, Integer> hiddenStateMap;
  private HashMap<EmissionState, Integer> emissionStateMap;
  private HashMap<Observation, Integer> observationToIndexMap;
  private HashMap<Observation, EmissionState> observationToEmissionStateMap;
  private double[] scalingFactor;

  public EmissionState observationToEmission(Observation observation)
  {
    return observationToEmissionStateMap.get(observation);
  }

  public int observationToObservationIndex(Observation observation)
  {
    return observationToIndexMap.get(observation);
  }

  public int hiddenStateToHiddenStateIndex(HiddenState hiddenState) {
    return hiddenStateMap.get(hiddenState);
  }

  public int emissionStateToEmissionStateIndex(EmissionState emissionState) {
    return emissionStateMap.get(emissionState);
  }

  public MapWarden(List<Sample> sampleObservations){
    // filter null observations in sample
    sampleObservations = sampleObservations.stream().filter(s -> s != null && s.getHash().get(s.getHash().size()-1) != null).collect(Collectors.toList());
    observations = sampleObservations.stream().map(sample -> new Observation(sample)).collect(Collectors.toList());

    // an emission state is a snapshot along with an action to perform. We can only emit all distinct snapshots we have seen.
    //emissionStates = sampleObservations.stream().map(distinctSample -> new EmissionState(distinctSample)).distinct().collect(Collectors.toList());

    List<Integer> listOfHashesAlreadySeen = new ArrayList<>();
    emissionStates = new ArrayList<>();
    for (Sample sampleObservation : sampleObservations){
      Observation observation = new Observation(sampleObservation);
      if (! listOfHashesAlreadySeen.contains(observation.getHashCode())) {
        listOfHashesAlreadySeen.add(observation.getHashCode());
        emissionStates.add(new EmissionState(sampleObservation));
      }
    }

    // There are as many hidden states as there are unique patterns in our observations.
    // We only support patterns we have already seen, so we base this number on the number of different samples seen
    hiddenStates = sampleObservations
        .stream()
        .distinct()
        .filter(sample -> sample.getHash().stream().noneMatch(x -> x == null))
        .map(sample -> new HiddenState(sample.getHash()))
        .collect(Collectors.toList());

    hiddenStateMap = new HashMap<>(hiddenStates.size());
    for (int i = 0; i < hiddenStates.size(); i++){
      hiddenStateMap.putIfAbsent(hiddenStates.get(i), i);
    }

    emissionStateMap = new HashMap<>(emissionStates.size());
    for (int i = 0; i < emissionStates.size(); i++){
      emissionStateMap.putIfAbsent(emissionStates.get(i), i);
    }

    observationToEmissionStateMap = new HashMap<>(emissionStates.size());
    for (int i = 0; i < observations.size(); i++){
      for (int j = 0; j < emissionStates.size(); j++){
        EmissionState emissionState = emissionStates.get(j);
        Observation observation = observations.get(i);
        if (emissionState.equalsObservation(observation)) {
          observationToEmissionStateMap.putIfAbsent(observation, emissionState);
        }
      }
    }

    observationToIndexMap = new HashMap<>(observations.size());
    for (int i = 0; i < observations.size(); i++){
      Observation observation = observations.get(i);
      observationToIndexMap.putIfAbsent(observation, i);
    }

    scalingFactor = new double[getNumObservations()];

  }

  public void setScalingFactor(int index, double value) {
    scalingFactor[index] = value;
  }

  public double getScalingFactor(int index) {
    return scalingFactor[index];
  }

  public int getNumEmissionStates()
  {
    return emissionStates.size();
  }

  public int getNumHiddenStates()
  {
    return hiddenStates.size();
  }

  public HiddenState hiddenStateIndexToHiddenState(int mostProbableIndex)
  {
    return hiddenStates.get(mostProbableIndex);
  }

  public EmissionState emissionStateIndexToEmissionState(int mostProbableIndex)
  {
    return emissionStates.get(mostProbableIndex);
  }

  public Iterable<HiddenState> iterateHiddenStates() {
    return hiddenStates;
  }

  public Observation observationIndexToObservation(int index) {
    return observations.get(index);
  }

  public int getNumObservations()
  {
    return observations.size();
  }

  public Iterable<Observation> iterateObservations()
  {
    return observations;
  }

  public Observation lastObservation()
  {
    return observations.get(observations.size() - 1);
  }

  public Observation nextObservation(Observation observation)
  {
    int currentIndex = observationToObservationIndex(observation);
    if (currentIndex == observations.size() - 1) {
      return null;
    } else {
      return observationIndexToObservation(currentIndex + 1);
    }

  }

  public Observation previousObservation(Observation observation)
  {
    int currentIndex = observationToObservationIndex(observation);
    if (currentIndex == 0) {
      return null;
    } else {
      return observationIndexToObservation(currentIndex - 1);
    }

  }

  public Observation firstObservation()
  {
    return observations.get(0);
  }

  public Iterable<EmissionState> iterateEmissionStates()
  {
    return emissionStates;
  }

  public void addObservationMapping(Observation observation){
    for (int j = 0; j < emissionStates.size(); j++){
      EmissionState emissionState = emissionStates.get(j);
      if (emissionState.equalsObservation(observation)){
        observationToEmissionStateMap.putIfAbsent(observation, emissionState);
      }
    }
  }
}
