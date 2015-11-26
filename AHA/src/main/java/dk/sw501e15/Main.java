package dk.sw501e15;

import Communication.*;
import Database.DB;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Normaliser.Normalizer;
import Sampler.*;
import sun.text.normalizer.NormalizerImpl;

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
        Communicator oCommunicator = new Communicator("/dev/ttyUSB1", 9600, dr);
        Normalizer nm = Normalizer.getInstance();
        Queue<SensorState> queueOfSensorState = new LinkedTransferQueue<SensorState>();
        oWorker.registerOutputTo(queueOfSensorState);

        List<NormalizedValue> nStateList;

        while (true)
        {

            if (!queueOfSensorState.isEmpty())
            {
                nStateList = nm.Normalize(queueOfSensorState.poll()).getNormalizesValues();
                for(int i = 0; i < nStateList.size(); i++) {
                    System.out.println("Got: " +nStateList.get(i).getValue() + ", isEmulatable: " + nStateList.get(i).isEmulatable());
                }

            }
            nStateList = null;
        }


    }
}