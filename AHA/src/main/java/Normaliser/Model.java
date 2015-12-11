package Normaliser;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Model implements Runnable{
  private List<Range> ranges = new ArrayList<>(); //what is acctually checked against when normalizing
  private int numberOfClusters = -1;

  //mutex variables**
  public AtomicBoolean modelBeingMade = new AtomicBoolean();
  private AtomicBoolean modelBeingAssigned = new AtomicBoolean();
  private AtomicBoolean modelBeingUsed = new AtomicBoolean();


  //**
  private List<Integer> trainingData = new ArrayList<>(); //temporary data for generating a model, is null except when
  //generating the model (ie. in the method run())
  public int basedOnTrainingData = 0; //tells us how much history the last model where generated using

  public void setTrainingData(List<Integer> trainingData){
    if (!(modelBeingAssigned.get() || modelBeingMade.get())) //makes sure the training data isn't changed doing execution of run()
    {
      this.trainingData.addAll(trainingData);
      // = trainingData;
    }
  }

  public boolean getModelBeingMade(){
    return modelBeingMade.get();
  }

  public boolean getModelBeingAssigned(){
    return modelBeingAssigned.get();
  }

  //determines which cluster the input fits into and returns it. The 'cluster' is the index of the range as defined in the normalizer class
  public int determineNormalization(int toNormalize){

    modelBeingUsed.set(true);
    int i = 0;
    if (toNormalize < 0) return -1;
    for (Range r : ranges){
      if (r.fits(toNormalize)){
        modelBeingUsed.set(false);
        return i;
      }
      i++;
    }
    modelBeingUsed.set(false);
    return i;
  }

  //modelBeingMade and modelBeingAssigned are mutex variables, there are two because we dont want to start generating a new model in the middle of generating one
  //but we also dont want to lock out the main loop while generating, when its only nessacary when implementing/assigning the new model
  public void run(){
    modelBeingMade.set(true);
    System.out.println("Model being generated, on: " + trainingData.size());
    List<Range> rangesHolder;

    //call normalizer
    ModelGenerator oModelGen = new ModelGenerator();
    if (numberOfClusters == -1) numberOfClusters = oModelGen.DetermineNumberOfClusters(trainingData);

    rangesHolder = oModelGen.generateModel(trainingData, numberOfClusters);
    //**
    while (modelBeingUsed.get()) ; //busy wait for mutex
    modelBeingAssigned.set(true);
    modelBeingMade.set(false);
    //assign the returned normalized list of ranges
    ranges = rangesHolder;
    modelBeingAssigned.set(false);
    basedOnTrainingData = trainingData.size();
    this.trainingData.clear();
    System.out.println("Model changed");

  }

}
