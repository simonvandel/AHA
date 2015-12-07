package Normaliser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brugeren on 03-12-2015.
 */
public class Sensor
{

  private boolean adaptiveNormalization = true; //determains whether or not we countinusly adapt our model. Is currently only settable in the code

  private String deviceID = ""; //signifies the device this data is associated with
  private int sensorIndex = -1; //signifies the sensor on the device this data is associated with
  private List<Integer> trainingData = new ArrayList<>(); //the history of this sensor, or trainingData
  private Model oModel = null; //the model, this is null until the first time we generate the model, after that
  // it will change each time we've recieved enough data according to trainingDataTreshhold

  private int trainingDataThreshhold = 500; //determines when we've got enough data to generate a new model

  public String getDeviceID()
  {
    return deviceID;
  }

  public int getSensorIndex()
  {
    return sensorIndex;
  }

  public Sensor(String device, int sensorIndex)
  {
    this.deviceID = device;
    this.sensorIndex = sensorIndex;
  }

  //normalizes the input according to the model, and initiates the generation of a new model of appropriate
  public int normalize(int toNormalize)
  {
    int toReturn = -1;


    trainingData.add(toNormalize);
    if (trainingData.size() % 100 == 0)
    {
      System.out.println("For index: " + sensorIndex + ", trainingdata: " + trainingData.size());
    }
    if (!trainingData.stream().allMatch(x -> x == 0 || x == 1))
    {
      //if model is null we it means we havnt had enough trainingdata for that sensor to make a model, so we start making one
      //else we normalize the model, as long as the model isn't being assigned by the model creation thread Then check wether or not we should update our model
      if (oModel == null)
      {
        if (trainingData.size() > trainingDataThreshhold)
        {
          createModelThread();
        }
      } else
      {
        if (!oModel.getModelBeingAssigned())
          toReturn = oModel.determineNormalization(toNormalize);
        if ((trainingData.size() - oModel.basedOnTrainingData) > trainingDataThreshhold && adaptiveNormalization)
          createModelThread();
      }
    } else
    toReturn = toNormalize;
    if (trainingData.size() > trainingDataThreshhold * 5)
      trainingData.subList(0, trainingDataThreshhold);
    return toReturn;
  }

  //starts the generation of a new model in a seperate thread
  private void createModelThread()
  {
    if (oModel == null)
      oModel = new Model();

    if (!(oModel.getModelBeingAssigned() || oModel.getModelBeingMade()))
    {
      //oModel.modelBeingMade.set(true);
      System.out.println("Model Changing");
      oModel.setTrainingData(trainingData);
      oModel.basedOnTrainingData = trainingData.size();
      (new Thread(oModel)).start();
    }
    //possibly check whether or not we should remove some of the training data?
    //if(trainingData.size() > )
  }


  @Override
  public int hashCode()
  {
    int hash = 0;
    for (int i = 0; i < this.deviceID.length(); i++)
    {
      hash += (int) this.deviceID.charAt(i);
    }
    hash += 31 + sensorIndex;
    return hash;
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof Sensor))
      return false;
    if (o == this)
      return true;

    Sensor toCompareTo = (Sensor) o;
    if ((toCompareTo.deviceID.equals(this.deviceID)) && (toCompareTo.sensorIndex == this.sensorIndex))
    {
      return true;
    }
    return false;
  }

}
