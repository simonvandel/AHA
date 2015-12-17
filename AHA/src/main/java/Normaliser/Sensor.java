package Normaliser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by brugeren on 03-12-2015.
 */
public class Sensor{
  //util
  private  String newLine =  System.getProperty("line.separator");
  //
  //logger
  private Logger logger = Logger.getLogger("normLogger");
  //
  private boolean adaptiveNormalization = true; //determains whether or not we countinusly adapt our model. Is currently only settable in the code

  private String deviceID = ""; //signifies the device this data is associated with
  private int sensorIndex = -1; //signifies the sensor on the device this data is associated with
  private List<Integer> trainingData = new ArrayList<>(); //the history of this sensor, or trainingData
  private Model oModel = null; //the model, this is null until the first time we generate the model, after that
  // it will change each time we've recieved enough data according to trainingDataTreshhold

  //Tresholds
  private int trainingDataThreshhold = 5000; //determines when we've got enough data to generate a new model
  private final int MIN_HISTORY_TO_GENERATE_MODEL = 200;
  private final int MIN_VARIANCE_TO_GENERATE_MODEL = 50;
//
  public String getDeviceID(){
    return deviceID;
  }

  public int getSensorIndex(){
    return sensorIndex;
  }

  public int getNumberOfClusters(){
    if (oModel == null){
      return 2;
    }
    return oModel.getNumberOfClusters();
  }

  public Sensor(String device, int sensorIndex){
    this.deviceID = device;
    this.sensorIndex = sensorIndex;
  }

  //normalizes the input according to the model, and initiates the generation of a new model of appropriate
  public int normalize(int toNormalize){
    int toReturn = -1;

    trainingData.add(toNormalize);
    if (!trainingData.stream().allMatch(x -> x == 0 || x == 1)){
      //if model is null we it means we havnt had enough trainingdata for that sensor to make a model, so we start making one
      //else we normalize the model, as long as the model isn't being assigned by the model creation thread Then check wether or not we should update our model
      if (oModel == null){
        if (determainValidatyOfTrainingData()){
          trainingDataThreshhold = trainingData.size(); //sets the treshhold to the trainingdata size because this should be a baseline for future model gens.
          logger.log(Level.SEVERE, "in normalize: Size of training data treshold: " + trainingDataThreshhold + ". ID: " + sensorIndex + ". addr: " + deviceID);
          createModelThread();
        }
      } else{
        if (!oModel.getModelBeingAssigned()) toReturn = oModel.determineNormalization(toNormalize);
        if ((trainingData.size() - oModel.basedOnTrainingData) > trainingDataThreshhold && adaptiveNormalization){
          createModelThread();
        }

      }
    } else toReturn = toNormalize;
    //TODO: test, the removal is new
    if (trainingData.size() > trainingDataThreshhold * 10) trainingData.removeAll(trainingData.subList(0, trainingDataThreshhold));
    return toReturn;
  }

  private boolean determainValidatyOfTrainingData(){
    if (trainingData.size() > MIN_HISTORY_TO_GENERATE_MODEL){
      ModelGenerator oModelGen = new ModelGenerator();
      Collections.sort(trainingData);
      List<List<Integer>> cluters = new ArrayList<>(oModelGen.splitIntoClusers(trainingData, 3));
      double clusterVariance = oModelGen.findClusterVariance(cluters);
      System.out.println(clusterVariance);
      return clusterVariance > MIN_VARIANCE_TO_GENERATE_MODEL;
    }
    return false;
  }

  //starts the generation of a new model in a seperate thread
  private void createModelThread(){
    if (oModel == null) oModel = new Model();

    if (!(oModel.getModelBeingAssigned() || oModel.getModelBeingMade())){
      oModel.modelBeingMade.set(true);
      oModel.setTrainingData(trainingData);
      oModel.basedOnTrainingData = trainingData.size();
      String log = "";
      int i = 0;
      for (Range r : oModel.getRanges()){
        log += "range: " + i + newLine + "\tlowerbound: " + r.lowerBound + ", upperbound: " + r.upperBound + newLine;
        i++;
      }
      logger.log(Level.SEVERE, "Model for sensor: " + sensorIndex + ", on device: " + deviceID + "Model:"+ newLine + log);
      Thread thread = new Thread(oModel);
      thread.setDaemon(true);
      thread.start();
    }
  }


  @Override
  public int hashCode(){
    int hash = 0;
    for (int i = 0; i < this.deviceID.length(); i++){
      hash += (int) this.deviceID.charAt(i);
    }
    hash += 31 + sensorIndex;
    return hash;
  }

  @Override
  public boolean equals(Object o){
    if (!(o instanceof Sensor)) return false;
    if (o == this) return true;

    Sensor toCompareTo = (Sensor) o;
    if ((toCompareTo.deviceID.equals(this.deviceID)) && (toCompareTo.sensorIndex == this.sensorIndex)){
      return true;
    }
    return false;
  }

}
