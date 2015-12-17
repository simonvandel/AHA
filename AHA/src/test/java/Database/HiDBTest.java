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

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Created by heider on 07/12/15.
 */
public class HiDBTest
{
  static private Logger sampleLogger;
  static private Logger reasonLogger;
  List<Sample> samples = new ArrayList<>();
  static HiDB db = HiDB.getInstance();

  @Before
  public void setUp() {    try{
    Handler reasonHandler = new FileHandler("logs/reasoner/logReason" + Instant.now().toString() + ".xml");
    Handler sampleHandler = new FileHandler("logs/sampler/logSample" + Instant.now().toString() + ".xml");

    reasonLogger = Logger.getLogger("reasonLogger");
    sampleLogger = Logger.getLogger("sampleLogger");

    reasonLogger.addHandler(reasonHandler);
    sampleLogger.addHandler(sampleHandler);
  }catch (IOException e){
    System.out.println("ERROR INSTANTIATING LOGGERS");
    e.printStackTrace();
  }
    Sampler sampler = Sampler.getInstance(sampleLogger,reasonLogger);
    for(int i=0; i<60;i++){
      NormalizedSensorState ns = new NormalizedSensorState(Instant.now());
      ns.AddNormalizedValue(new NormalizedValue(1337*i, true, "fuckdigsimon", 7331*i,2));
      ns.AddNormalizedValue(new NormalizedValue(2337*i, false, "fuckdigahmed", 2331*i,2));
      ns.AddNormalizedValue(new NormalizedValue(1337*i, true, "fuckdi213gsimon", 73323*i,2));
      ns.AddNormalizedValue(new NormalizedValue(2337*i, false, "fuckdiga123hmed", 2321431*i,2));
      Sample sample = sampler.getSample(ns);
      if(sample != null){
        samples.add(sample);
      }
    }
  }

  @Test
  public void putSampleTest() {
    db.putNewSample(samples.get(37));
  }

  @Test
  public void getSamplesTest() {
    System.out.print(db.getSamples());
  }

  @Test
  public void putNewSensorStateTest() {
    List<SensorValue> svs = new ArrayList<>();
    svs.add(new SensorValue(1337, true, "fuckdigsimon", 7331));
    svs.add(new SensorValue(7331, false, "fuckdig123simon", 12337));
    svs.add(new SensorValue(133137, true, "fuckdigs1121223imon", 7331));
    svs.add(new SensorValue(73131, false, "fuckdig155623simon", 1321237));
    svs.add(new SensorValue(13237, true, "fuc854889kdigsimon", 732131));
    svs.add(new SensorValue(73231, false, "fuckdig5748simon", 13137));
    db.putNewSensorState(new SensorState(svs,Instant.now()));
  }

  @Test
  public void getSensorStateTest() {
    System.out.print(db.getSensorStates());
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
