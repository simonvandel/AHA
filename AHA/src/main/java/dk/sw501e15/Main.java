package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;
import Communication.SensorPacketWorker;
import Communication.SensorState;
import Database.DB;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Normaliser.Normalizer;
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

        int scopeSize = 6;
        int emulatableNum = 2;

        Sample sample;
        DB db = DB.getInstance(scopeSize, emulatableNum);
        db.createDB();
        Sampler sampler = Sampler.getInstance(scopeSize, emulatableNum);

        List<NormalizedValue> nValueList;
        NormalizedSensorState nState;

        while (true)
        {
            if (!queueOfSensorState.isEmpty())
            {
                nValueList = nm.Normalize(queueOfSensorState.poll()).getNormalizesValues();
                nState = nm.Normalize(queueOfSensorState.poll());
                for (int i = 0; i < nValueList.size(); i++)
                {
                    System.out.println("Got: " + nValueList.get(i).getValue() + ", isEmulatable: " + nValueList.get(i).isEmulatable());
                }

                sample = sampler.getSample(nState);
                db.putStateScopeIntoDB(sample);
            }
            nValueList = null;
        }
    }
}