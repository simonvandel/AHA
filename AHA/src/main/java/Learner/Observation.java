package Learner;

import Sampler.Sample;

/**
 * Created by simon on 04/12/2015.
 */
public class Observation{
  private Integer hashCode;
  private int time;

  public Observation(Sample sample){
    if (sample.getTime() != null){
      time = sample.getTime().getNano();
      hashCode = sample.getHash().get(sample.getHash().size() - 1);
    }
  }

  public Observation(Integer hashCode){
    this.hashCode = hashCode;
  }


  public int getHashCode(){
    if(hashCode == null){
      hashCode = 0;
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Observation that = (Observation) o;

    if (time != that.time) return false;
    return hashCode.equals(that.hashCode);

  }

  @Override
  public int hashCode(){
    int result = hashCode.hashCode();
    result = 31 * result + time;
    return result;
  }
}
