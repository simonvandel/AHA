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

        int scopeSize = 6;
        int emulatableNum = 2;

    Sample sample;
    DB db = DB.getInstance();

        List<NormalizedValue> nValueList;
        NormalizedSensorState nState;

        while (true)
        {
          sample = sampler.getSample(nState);
          oReasoner.reason(sample);
          db.putStateScopeIntoDB(sample);


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