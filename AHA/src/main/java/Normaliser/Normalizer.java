package Normaliser;

import Communication.SensorState;
import Communication.SensorValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Zobair on 20-11-2015.
 */
public class Normalizer{
  private static Normalizer normalizer;
  private static Logger logger;

  /**
   * Initializes an object of normalizer class.
   */
  private Normalizer(Logger normLogger){
    logger = normLogger;
  }

  /**
   * Get instance method to ensure singleton pattern,
   *
   * @return the one and only object of the Normalizer class.
   */
  public static Normalizer getInstance(Logger normLogger){
    if (normalizer == null){
      logger = normLogger;
      normalizer = new Normalizer(logger);
    }

    return normalizer;
  }

  private Set<Sensor> sensors = new HashSet();

  public Sensor getSensor(String deviceId, int sensorIndex){
    for (Iterator<Sensor> sIte = sensors.iterator(); sIte.hasNext(); ){
      Sensor s = sIte.next();
      if (s.getDeviceID().equals(deviceId) && s.getSensorIndex() == sensorIndex){
        return s;
      }
    }
    throw new IllegalArgumentException("The item should always exsist when calling this method(Sensor norm)");
  }

  /**
   * The method normalizes the input data and returns an instance of NormalizedsensorState object.
   *
   * @param sensorState
   * @return a normalized sensorstate object.
   */
  public NormalizedSensorState Normalize(SensorState sensorState){
    Instant sTime = sensorState.getTime();
    List<SensorValue> values = sensorState.getValues();
    NormalizedSensorState normalizedSensorState = new NormalizedSensorState(sTime);

    for (SensorValue currSV : values){
      String currDeviceAddr = currSV.getDeviceAddress();
      int currSensorIndex = currSV.getSensorIndexOnDevice();
      sensors.add(new Sensor(currDeviceAddr, currSensorIndex));

      Sensor sensor = getSensor(currDeviceAddr, currSensorIndex);
      int normalizedValue = sensor.normalize(currSV.getValue());
      if (normalizedValue > -1){
        normalizedSensorState.AddNormalizedValue(new NormalizedValue(normalizedValue, currSV.isEmulatable(), currDeviceAddr, currSensorIndex, sensor.getNumberOfClusters()));
      } else return null;
    }

    return normalizedSensorState;
  }

}
