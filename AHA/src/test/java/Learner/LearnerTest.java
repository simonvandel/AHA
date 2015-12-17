package Learner;

import Communication.SensorState;
import Communication.SensorValue;
import Normaliser.NormalizedSensorState;
import Normaliser.Normalizer;
import Reasoner.Reasoning;
import Sampler.Sample;
import Sampler.Sampler;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by simon on 07/12/2015.
 */
public class LearnerTest{

  static private Logger aiLogger;
  static private Logger sampleLogger;
  static private Logger reasonLogger;

  private int timeCounter = 0;
  @Test
  public void testLearner() {
    try{
      Handler aiHandler = new FileHandler("logs/learner/logAI" + Instant.now().toString() + ".xml");
      Handler reasonHandler = new FileHandler("logs/reasoner/logReason" + Instant.now().toString() + ".xml");
      Handler sampleHandler = new FileHandler("logs/sampler/logSample" + Instant.now().toString() + ".xml");

      aiLogger = Logger.getLogger("aiLogger");
      reasonLogger = Logger.getLogger("reasonLogger");
      sampleLogger = Logger.getLogger("sampleLogger");

      aiLogger.addHandler(aiHandler);
      reasonLogger.addHandler(reasonHandler);
      sampleLogger.addHandler(sampleHandler);
    }catch (IOException e){
      System.out.println("ERROR INSTANTIATING LOGGERS");
      e.printStackTrace();
    }
    String deviceAddress = "123";
    // test data: when first sensor in sensorValues list is 0,
    // the second sensor value (emulatable) in sensorValues should be 1
    // when 1, 0 is given as input, it is expected that the HMM should infer that the action should be to turn on sensor 2

    // sensor1 (movement in room), sensor2 (lamp), sensor3 (random noise)
    Learner learner = new Learner(aiLogger);

    List<Sample> allSamples = new ArrayList<>();

    Random r = new Random(0);
    for (int i = 0; i < 100; i++){

      /*mkSampleandAdd(0, 0, 0, allSamples);
      mkSampleandAdd(0, 0, 1, allSamples);
      mkSampleandAdd(0, 1, 0, allSamples);
      mkSampleandAdd(0, 1, 1, allSamples);
      mkSampleandAdd(0, 0, 0, allSamples);*/

      /*mkSampleandAdd(0, 0, 1, allSamples);
      mkSampleandAdd(1, 0, 1, allSamples);
      mkSampleandAdd(1, 1, 0, allSamples);
      mkSampleandAdd(1, 1, 0, allSamples);
      mkSampleandAdd(1, 1, 1, allSamples);
      mkSampleandAdd(0, 0, 1, allSamples);
      mkSampleandAdd(0, 0, 0, allSamples);
      mkSampleandAdd(0, 0, 0, allSamples);
      mkSampleandAdd(0, 0, 1, allSamples);
      mkSampleandAdd(1, 0, 1, allSamples);
      mkSampleandAdd(1, 1, 1, allSamples);*/

      /*int random1 = r.nextBoolean() ? 1 : 0;
      int random2 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd( random1, 1, 1, allSamples);
      mkSampleandAdd( random2, 0, 1, allSamples);*/

      int random1 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd(0, 0, random1, allSamples);
      random1 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd(0, 0, random1, allSamples);
      random1 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd(1, 0, random1, allSamples);
      random1 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd(1, 1, random1, allSamples);
      random1 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd(1, 1, random1, allSamples);
      random1 = r.nextBoolean() ? 1 : 0;
      mkSampleandAdd(1, 1, random1, allSamples);

      /*mkSampleandAdd(1, 1, 1, allSamples);*/
    }

    HiddenMarkovModel model = learner.learn(allSamples);

    Sample inputSample = mkSample(1, 0, 0);
    Reasoning reasoning = model.CalculateReasoning(inputSample);
    // there is only 1 action, so look at index 0
    assertEquals(1, reasoning.getActions().get(0).getChangeToValue());
  }

  private Sample mkSample(int value1, int value2, int value3) {
    List<SensorValue> sensorValues = new ArrayList<>();
    sensorValues.add(new SensorValue(value1, false, "123", 0));
    sensorValues.add(new SensorValue(value2, true, "123", 1));
    sensorValues.add(new SensorValue(value3, false, "123", 2));
    SensorState sensorState = new SensorState(sensorValues, Instant.ofEpochMilli(timeCounter));
    timeCounter++;

    //Normalizer nm = Normalizer.getInstance();
    Sampler sampler = Sampler.getInstance(sampleLogger,reasonLogger);

    NormalizedSensorState normalizedSensorState = new NormalizedSensorState(sensorState, 2); //nm.Normalize(sensorState);

    return sampler.getSample(normalizedSensorState);
  }

  // makes sensorState where the first value is not emulatable and the second value is emulatable
  private void mkSampleandAdd(int value1, int value2, int value3, List<Sample> allSamples) {
    Sample sample = mkSample(value1, value2, value3);
    if (sample != null) {
      allSamples.add(sample);
    }
  }
}