package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;
import Communication.SensorPacketWorker;
import Communication.SensorState;
import Database.DB;
import Learner.Learner;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Normaliser.Normalizer;
import Reasoner.Reasoner;
import Sampler.*;

import java.sql.Time;
import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;


public class Main
{

  public static void main(String[] args)
  {
    SensorPacketWorker oWorker = new SensorPacketWorker();
    DataReceiver dr = new DataReceiver(oWorker);

    Communicator oCommunicator = new Communicator("/dev/ttyUSB1", 9600, dr);
    Normalizer nm = Normalizer.getInstance();
    Queue<SensorState> queueOfSensorState = new LinkedTransferQueue<SensorState>();
    oWorker.registerOutputTo(queueOfSensorState);

    Sample sample;
    DB db = DB.getInstance();

    db.createDB();
    Sampler sampler = Sampler.getInstance();

    Reasoner oReasoner = Reasoner.getInstance();
    oReasoner.setCommunicator(oCommunicator);

    NormalizedSensorState nState;

    Instant learnerRun = Instant.now();
    long learnerRunInverval = 600; //in seconds

    Thread learnerThread = new Thread(){
      public void run(){
        DB db = DB.getInstance();
        Learner oLearner = new Learner();
        //db.pushModel(oLearner.learn(db.getHistory())); //get the history to learn on and push the model once finished

      }
    };

    while (true)
    {
      while (!queueOfSensorState.isEmpty())
      {
        SensorState oST = queueOfSensorState.poll();
        nState = nm.Normalize(oST);
        if (nState != null)
        {
          List<NormalizedValue> oList = nState.getNormalizesValues();

          sample = sampler.getSample(nState);
          oReasoner.reason(sample);
          db.putStateScopeIntoDB(sample);

        }
      }
      if(Instant.now().isAfter(learnerRun.plusSeconds(learnerRunInverval))){
        if(!learnerThread.isAlive()){
          learnerThread.start();
          learnerRun = Instant.now();
        }
      }
    }
  }
}