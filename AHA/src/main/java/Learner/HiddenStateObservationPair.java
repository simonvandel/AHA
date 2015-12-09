package Learner;


public class HiddenStateObservationPair {
  private final HiddenState hiddenState;
  private final Observation observation;

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HiddenStateObservationPair that = (HiddenStateObservationPair) o;

    if (!hiddenState.equals(that.hiddenState)) return false;
    return observation.equals(that.observation);
  }

  @Override
  public int hashCode(){
    int result = hiddenState.hashCode();
    result = 31 * result + observation.hashCode();
    return result;
  }

  public HiddenStateObservationPair(HiddenState hiddenState, Observation observation){
    this.hiddenState = hiddenState;
    this.observation = observation;
  }
}