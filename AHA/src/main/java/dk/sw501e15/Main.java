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

    Communicator oCommunicator = new Communicator("/dev/ttyUSB0", 9600, dr);
    Normalizer nm = Normalizer.getInstance();
    Queue<SensorState> queueOfSensorState = new LinkedTransferQueue<SensorState>();

    /*List<SensorState> tempDbSensorState = db.getSensorStates();
    if(tempDbSensorState != null){
      queueOfSensorState.addAll(tempDbSensorState);
      tempDbSensorState = null;
    }
    */
    oWorker.registerOutputTo(queueOfSensorState);

    Sample sample;
    Sampler sampler = Sampler.getInstance();

    Reasoner oReasoner = Reasoner.getInstance();
    oReasoner.setCommunicator(oCommunicator);

    NormalizedSensorState nState;

    Instant learnerRun = Instant.now();
    long learnerRunInverval = 90; //in seconds

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
    learnerThread.setDaemon(true);
    Learner oLearner = new Learner();
    List<Sample> learnerData = new ArrayList<>();
    SampleList sampleList = SampleList.getInstance();
    while (true)
    {
      while (!queueOfSensorState.isEmpty())
      {
        System.out.print('.');
        if(queueOfSensorState.size() >  100)
          Logger.getLogger("mainLogger").log(Level.SEVERE, "Behind in sensor queue: " + queueOfSensorState.size());

        SensorState oST = queueOfSensorState.poll();
       // db.putNewSensorState(oST); //TODO: Is there delay on the db write? If there is we should decouple this call from the main loop
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
    try{
      Handler mainHandler = new FileHandler("logs/logMain" + Instant.now().toString() + ".xml");
      Handler sampleHandler = new FileHandler("logs/logSample" + Instant.now().toString() + ".xml");
      Handler comHandler = new FileHandler("logs/logCom" + Instant.now().toString() + ".xml");
      Handler normHandler = new FileHandler("logs/logNorm" + Instant.now().toString() + ".xml");
      Handler aiHandler = new FileHandler("logs/logAI" + Instant.now().toString() + ".xml");
      Handler reasonHandler = new FileHandler("logs/logReason" + Instant.now().toString() + ".xml");
      Logger.getLogger("mainLogger").addHandler(mainHandler);
      Logger.getLogger("comLogger").addHandler(comHandler);
      Logger.getLogger("normLogger").addHandler(normHandler);
      Logger.getLogger("sampleLogger").addHandler(sampleHandler);
      Logger.getLogger("aiLogger").addHandler(aiHandler);
      Logger.getLogger("reasonLogger").addHandler(reasonHandler);

      Logger.getLogger("mainLogger").log(Level.SEVERE, "TEST");
      Logger.getLogger("comLogger").log(Level.SEVERE, "TEST");
      Logger.getLogger("normLogger").log(Level.SEVERE, "TEST");
      Logger.getLogger("sampleLogger").log(Level.SEVERE, "TEST");
      Logger.getLogger("aiLogger").log(Level.SEVERE, "TEST");
      Logger.getLogger("reasonLogger").log(Level.SEVERE, "TEST");
    } catch (IOException e){
      System.out.println("ERROR INSTANTIATING LOGGERS");
      e.printStackTrace();
    }

  }
}