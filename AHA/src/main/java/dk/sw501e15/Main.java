package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;
import Communication.SensorPacketWorker;
import Communication.SensorState;
import Database.DB;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Normaliser.Normalizer;
import Reasoner.Reasoner;
import Sampler.*;

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

    while (true)
    {
      while (!queueOfSensorState.isEmpty())
      {
        //nValueList = nm.Normalize(queueOfSensorState.poll()).getNormalizesValues();
        SensorState oST = queueOfSensorState.poll();
        nState = nm.Normalize(oST);
        if (nState != null)
        {
          List<NormalizedValue> oList = nState.getNormalizesValues();
          
          sample = sampler.getSample(nState);
          db.putStateScopeIntoDB(sample);

          oReasoner.reason(sample);
        }
      }
    }
  }
}