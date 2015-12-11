package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;
import Communication.SensorPacketWorker;
import Communication.SensorState;
import Database.DB;
import Database.HiDB;
import Learner.Learner;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Normaliser.Normalizer;
import Reasoner.Reasoner;
import Sampler.*;

import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;


public class Main
{

  public static void main(String[] args)
  {
    SensorPacketWorker oWorker = new SensorPacketWorker();
    DataReceiver dr = new DataReceiver(oWorker);

    Communicator oCommunicator = new Communicator("COM7", 9600, dr);
    Normalizer nm = Normalizer.getInstance();
    Queue<SensorState> queueOfSensorState = new LinkedTransferQueue<SensorState>();
    oWorker.registerOutputTo(queueOfSensorState);

    Sample sample;
//    HiDB db = HiDB.getInstance();

//    db.createDB();
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
          System.out.println("Ran learner");
          Learner oLearner = new Learner();
          List<Sample> samples = sampleList.getSamples();
          if(samples != null){
            oReasoner.setCurrentModel(oLearner.learn(samples));
          }
          //db.pushModel(oLearner.learn(db.getHistory())); //get the history to learn on and push the model once finished
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
    while (true)
    {
      while (!queueOfSensorState.isEmpty())
      {
        SensorState oST = queueOfSensorState.poll();
        nState = nm.Normalize(oST);
        if(queueOfSensorState.size() > 10) {
          System.out.println("Size of queue: " + queueOfSensorState.size());
        }
        if (nState != null)
        {
          sample = sampler.getSample(nState);
          if (sample != null) {
            //sampleList.add(sample);
            //db.putNewSample(sample);

            oReasoner.reasonAndSend(sample);
          }

        }
      }
      if(Instant.now().isAfter(learnerRun.plusSeconds(learnerRunInverval))){
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
}