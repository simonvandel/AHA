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
  static private Logger mainLogger;
  static private Logger comLogger;
  static private Logger normLogger;
  static private Logger aiLogger;
  static private Logger reasonLogger;
  static private Logger sampleLogger;

  public static void main(String[] args)
  {
    instantiateLoggers();
    mainLogger.log(Level.SEVERE, "TEST");
    comLogger.log(Level.SEVERE, "TEST");
    normLogger.log(Level.SEVERE, "TEST");
    aiLogger.log(Level.SEVERE, "TEST");
    reasonLogger.log(Level.SEVERE, "TEST");
    sampleLogger.log(Level.SEVERE, "TEST");

    SensorPacketWorker oWorker = new SensorPacketWorker();
    DataReceiver dr = new DataReceiver(oWorker);

    HiDB db = HiDB.getInstance();

    Communicator oCommunicator = new Communicator("/dev/ttyUSB0", 9600, dr, comLogger);
    Normalizer nm = Normalizer.getInstance(normLogger);
    Queue<SensorState> queueOfSensorState = new LinkedTransferQueue<SensorState>();

    /*List<SensorState> tempDbSensorState = db.getSensorStates();
    if(tempDbSensorState != null){
      queueOfSensorState.addAll(tempDbSensorState);
      tempDbSensorState = null;
    }
    */
    oWorker.registerOutputTo(queueOfSensorState);

    Sample sample;
    Sampler sampler = Sampler.getInstance(sampleLogger, reasonLogger);

    Reasoner oReasoner = Reasoner.getInstance(reasonLogger);
    oReasoner.setCommunicator(oCommunicator);

    NormalizedSensorState nState;

    Instant learnerRun = Instant.now();
    long learnerRunInverval = 120; //in seconds

    Thread learnerThread = new Thread(){
      public synchronized void run(){
        Reasoner oReasoner = Reasoner.getInstance(reasonLogger);
        SampleList sampleList = SampleList.getInstance();

        while(true){
          Learner oLearner = new Learner(aiLogger);
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
    Learner oLearner = new Learner(aiLogger);
    List<Sample> learnerData = new ArrayList<>();
    SampleList sampleList = SampleList.getInstance();
    long loopStart;
    while (true)
    {
      while (!queueOfSensorState.isEmpty()){
        loopStart = System.currentTimeMillis();
        System.out.print('.');
        if (queueOfSensorState.size() > 100)
          mainLogger.log(Level.SEVERE, "Behind in sensor queue: " + queueOfSensorState.size());

        SensorState oST = queueOfSensorState.poll();
        comLogger.log(Level.INFO, "Sensor State Received: " + oST.toString());
        // db.putNewSensorState(oST); //TODO: Is there delay on the db write? If there is we should decouple this call from the main loop
        nState = nm.Normalize(oST);
        if (nState != null){
          sample = sampler.getSample(nState);
          if (sample != null){
            oReasoner.reasonAndSend(sample);
            mainLogger.log(Level.INFO, "Loop time: " + (System.currentTimeMillis() - loopStart));
          }
        }
      }
      if(Instant.now().isAfter(learnerRun.plusSeconds(learnerRunInverval)) && sampleList.getSamples().size() > 10){
        if (learnerThread.getState() == Thread.State.NEW){
          learnerThread.start();
          learnerRun = Instant.now();
        } else if (learnerThread.getState() == Thread.State.WAITING){
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
      String fileEnding = Instant.now().toString() + ".xml";
      fileEnding = fileEnding.replace(':', '-');
      Handler mainHandler = new FileHandler("logs/main/logMain" + fileEnding);
      Handler sampleHandler = new FileHandler("logs/sampler/logSample" + fileEnding);
      Handler comHandler = new FileHandler("logs/com/logCom" + fileEnding);
      Handler normHandler = new FileHandler("logs/normalizer/logNorm" + fileEnding);
      Handler aiHandler = new FileHandler("logs/learner/logAI" + fileEnding);
      Handler reasonHandler = new FileHandler("logs/reasoner/logReason" + fileEnding);

      mainLogger = Logger.getLogger("mainLogger");
      comLogger = Logger.getLogger("comLogger");
      normLogger = Logger.getLogger("normLogger");
      aiLogger = Logger.getLogger("aiLogger");
      reasonLogger = Logger.getLogger("reasonLogger");
      sampleLogger = Logger.getLogger("sampleLogger");

      mainLogger.addHandler(mainHandler);
      comLogger.addHandler(comHandler);
      normLogger.addHandler(normHandler);
      aiLogger.addHandler(aiHandler);
      reasonLogger.addHandler(reasonHandler);
      sampleLogger.addHandler(sampleHandler);

      mainLogger.setUseParentHandlers(false);
      normLogger.setUseParentHandlers(false);
      comLogger.setUseParentHandlers(false);

    } catch (IOException e){
      System.out.println("ERROR INSTANTIATING LOGGERS");
      e.printStackTrace();
    }

  }
}