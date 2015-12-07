package Database;

import Communication.SensorState;
import Communication.SensorValue;
import Normaliser.NormalizedSensorState;
import Normaliser.Normalizer;
import Sampler.Sample;
import org.junit.Before;
import org.junit.Test;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;

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
  HiDB db;

  @Before
  public void setUp() {
    svs.add(new SensorValue(1337,false,"fuckdigsimon",7331));
    SensorState ss = new SensorState(svs, Instant.now());
    Normalizer norm = Normalizer.getInstance();
    List<NormalizedSensorState> norms = new ArrayList<>();
    norms.add(norm.Normalize(ss));
    samples.add(new Sample(norms,ss.getTime(), new ArrayList<>()));
    db = HiDB.getInstance();
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
