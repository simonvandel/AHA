package Database;

import Communication.SensorState;
import Communication.SensorValue;
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
  List<SensorValue> svs = new ArrayList<>();
  List<Sample> samples = new ArrayList<>();
  static HiDB db = HiDB.getInstance();

  @Before
  public void setUp() {
    Sampler sampler = Sampler.getInstance(2,1);
    Normalizer norm = Normalizer.getInstance();
    for(int i=0; i<4;i++){
      svs.add(new SensorValue(1337*i, true, "fuckdigsimon", 7331*i));
      svs.add(new SensorValue(7331*i, false, "fuckdigsimon", 1337*i));
      SensorState ss = new SensorState(svs, Instant.now());
      samples.add(sampler.getSample(norm.Normalize(ss)));
    }
  }

  @Test
  public void putSampleTest() {
    db.putNewSample(samples.get(0));
  }

  @Test
  public void getSamplesTest() {
    db.getSamples();
  }
}
