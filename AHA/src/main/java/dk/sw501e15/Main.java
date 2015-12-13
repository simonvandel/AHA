package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;
import Communication.SensorPacketWorker;
import Communication.SensorState;
import Database.HiDB;
import Learner.Learner;
import Normaliser.NormalizedSensorState;
import Normaliser.Normalizer;
import Reasoner.Reasoner;
import Sampler.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.logging.*;


public class Main
{

  public static void main(String[] args)
  {
    instantiateLoggers();
    SensorPacketWorker oWorker = new SensorPacketWorker();
    DataReceiver dr = new DataReceiver(oWorker);

    HiDB db = HiDB.getInstance();

    Communicator oCommunicator = new Communicator("COM6", 9600, dr);
    Normalizer nm = Normalizer.getInstance();
    Queue<SensorState> queueOfSensorState = new LinkedTransferQueue<SensorState>();
    oWorker.registerOutputTo(queueOfSensorState);

    Sample sample;
    Sampler sampler = Sampler.getInstance();

    Reasoner oReasoner = Reasoner.getInstance();
    oReasoner.setCommunicator(oCommunicator);

    NormalizedSensorState nState;

    Instant learnerRun = Instant.now();
    long learnerRunInverval = 60; //in seconds

    Thread learnerThread = new Thread(){
      public synchronized void run(){
        Reasoner oReasoner = Reasoner.getInstance();
        SampleList sampleList = SampleList.getInstance();

        while(true){
          Learner oLearner = new Learner();
          List<Sample> samples = sampleList.getSamples();
          if(samples != null){
            oReasoner.setCurrentModel(oLearner.learn(samples));
          }
          try{
            this.wait();
          } catch(InterruptedException e) {

          }
        }
      }
    };
    Learner oLearner = new Learner();
    List<Sample> learnerData = new ArrayList<>();
    SampleList sampleList = SampleList.getInstance();
    queueOfSensorState.addAll(db.getSensorStates());
    while (true)
    {
      while (!queueOfSensorState.isEmpty())
      {
        System.out.print('.');
        if(queueOfSensorState.size() >  100)
          Logger.getLogger("mainLogger").log(Level.SEVERE, "Behind in sensor queue: " + queueOfSensorState.size());

        SensorState oST = queueOfSensorState.poll();
        db.putNewSensorState(oST); //TODO: Is there delay on the db write? If there is we should decouple this call from the main loop
        nState = nm.Normalize(oST);
        if (nState != null)
        {
          sample = sampler.getSample(nState);
          if (sample != null) {
            oReasoner.reasonAndSend(sample);
          }
        }
      }
      if(Instant.now().isAfter(learnerRun.plusSeconds(learnerRunInverval)) && sampleList.getSamples().size() > 10){
        if(learnerThread.getState() == Thread.State.NEW){
          learnerThread.start();
          learnerRun = Instant.now();
        } else if(learnerThread.getState() == Thread.State.WAITING) {
          synchronized (learnerThread){
            learnerThread.notify();
          }
          learnerRun = Instant.now();
        }
      }
    }
  }

  private static void instantiateLoggers(){
    Logger logger = Logger.getLogger("mainLogger");
    try{
      Handler handler = new FileHandler("log");
      logger.addHandler(handler);
      Logger.getLogger("comLogger").addHandler(handler);
      Logger.getLogger("normLogger").addHandler(handler);
      Logger.getLogger("sampleLogger").addHandler(handler);
      Logger.getLogger("aiLogger").addHandler(handler);
      Logger.getLogger("reasonLogger").addHandler(handler);
    } catch (IOException e){
      e.printStackTrace();
    }

  }
}