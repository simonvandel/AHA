package Sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kafuch on 10-12-2015.
 * Encapsulates a thread-safe collection of samples.
 */
public class SampleList{
  private static SampleList sampleList = null;
  private ConcurrentHashMap<Long, Sample> list = new ConcurrentHashMap<>();

  private Long sampleKey = new Long(0);

  private SampleList(){}
  public static SampleList getInstance(){
    if(sampleList == null){
      sampleList = new SampleList();
    }
    return sampleList;
  }
  /**
   * Gets the list of samples. Is thread-safe
   * @return a list of samples
   */
  public List<Sample> getSamples(){
    return new ArrayList<Sample>(list.values());
  }

  /**
   * Puts the sample s into the collection. Is thread-safe
   * @param s the sample to add
   */
  public void add(Sample s){
    list.put(sampleKey, s);
    sampleKey++;
  }
}
