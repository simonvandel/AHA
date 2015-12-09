package Database;

import Communication.SensorState;
import Communication.SensorValue;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Normaliser.Normalizer;
import Sampler.Sample;
import Sampler.Sampler;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by heider on 07/12/15.
 */
public class HiDBTest
{
  List<Sample> samples = new ArrayList<>();
  static HiDB db = HiDB.getInstance();

  @Before
  public void setUp() {
    Sampler sampler = Sampler.getInstance();
    for(int i=0; i<10;i++){
      NormalizedSensorState ns = new NormalizedSensorState(Instant.now());
      ns.AddNormalizedValue(new NormalizedValue(1337*i, true, "fuckdigsimon", 7331*i));
      samples.add(sampler.getSample(ns));
    }
  }

  @Test
  public void putSampleTest() {
    db.putNewSample(samples.get(0));
  }

  @Test
  public void getSamplesTest() {
    System.out.print(db.getSamples());
  }

  @Test
  public void putNewSensorStateTest() {
    List<SensorValue> svs = new ArrayList<>();
    svs.add(new SensorValue(1337, true, "fuckdigsimon", 7331));
    svs.add(new SensorValue(7331, false, "fuckdigsimon", 1337));
    svs.add(new SensorValue(1337, true, "fuckdigsimon", 7331));
    svs.add(new SensorValue(7331, false, "fuckdigsimon", 1337));
    svs.add(new SensorValue(1337, true, "fuckdigsimon", 7331));
    svs.add(new SensorValue(7331, false, "fuckdigsimon", 1337));
    db.putNewSensorState(new SensorState(svs,Instant.now()));
  }

  @Test
  public void getSensorStateTest() {
      db.getSensorStates();
  }

  @Test
  public void putNewSensorValueTest() {
    db.putNewSensorValue(new SensorValue(1337, true, "fuckdigsimon", 7331));
  }

  @Test
  public void getSensorValuesTest() {
    System.out.print(db.getSensorValues());
  }
}
