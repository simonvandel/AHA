package Learner;

import Sampler.Sample;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

  public EmissionState observationToEmission(Observation observation)
  {
    return observationToEmissionStateMap.get(observation);
  }

  public int observationToObservationIndex(Observation observation)
  {
    return observationToIndexMap.get(observation);
  }

  public int hiddenStateToHiddenStateIndex(HiddenState hiddenState) {
    return 0;
  }

  public int emissionStateToEmissionStateIndex(EmissionState hiddenState) {
    return 0;
  }

  public MapWarden(List<Sample> sampleObservations)
  {
    observations = sampleObservations
        .stream()
        .map(sample -> new Observation(sample))
        .collect(Collectors.toList());

    // an emission state is a snapshot along with an action to perform. We can only emit all distinct snapshots we have seen.
    emissionStates = sampleObservations
        .stream()
        .distinct()
        .map(distinctSample -> new EmissionState(distinctSample))
        .collect(Collectors.toList());

    // There are as many hidden states as there are unique patterns in our observations.
    // We only support patterns we have already seen, so we base this number on the number of different samples seen
    hiddenStates = sampleObservations
        .stream()
        .distinct()
        .map(sample -> new HiddenState(sample.getHash()))
        .collect(Collectors.toList());


    hiddenStateMap = new HashMap<>(hiddenStates.size());
    for (int i = 0; i < hiddenStates.size(); i++)
    {
      hiddenStateMap.put(hiddenStates.get(i), i);
    }/*

    emissionStateMap = new HashMap<>(emissionStates.size());
    for (int i = 0; i < emissionStates.size(); i++)
    {
      emissionStateMap.put(emissionStates.get(i), i);
    }

    observationToEmissionStateMap = new HashMap<>(emissionStates.size());
    for (int i = 0; i < observations.size(); i++)
    {
      if (emissionStates.)
      observationToEmissionStateMap.put(emissionStates.get(i), i);*/
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
    return observationIndexToObservation(currentIndex + 1);
  }

  public Observation previousObservation(Observation observation)
  {
    int currentIndex = observationToObservationIndex(observation);
    return observationIndexToObservation(currentIndex - 1);
  }

  public Observation firstObservation()
  {
    return observations.get(0);
  }
}
