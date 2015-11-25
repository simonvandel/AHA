package dk.sw501e15;

import Communication.*;
import Database.DB;
import Sampler.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int scopeSize = 6;
        int emulatableNum = 2;

        Sample sample;

        SensorPacketWorker sensorPacketWorker = new SensorPacketWorker();
        DataReceiver receiver = new DataReceiver(sensorPacketWorker);
        //Communicator com = new Communicator("/dev/ttyUSB0", 9600, receiver);

        DB db = DB.getInstance(scopeSize, emulatableNum);
        db.createDB();

        Sampler sampler = Sampler.getInstance(scopeSize, emulatableNum);

        for(int i = 0; i<8; i++)
        {
            List<SensorValue> sensorValues = new ArrayList<>();
            sensorValues.add(new SensorValue(52123 % (i * 4032 + 1), false, "Device1", 1));
            sensorValues.add(new SensorValue(121321 % (i * 1321 + 1), false, "Device1", 2));
            sensorValues.add(new SensorValue(121321 % (i * 156 + 1), true, "Device1", 4));
            sensorValues.add(new SensorValue(0 + i, true, "Device1", 3));
            sensorValues.add(new SensorValue(4212315 % (i * 232 + 1), false, "Device1", 5));
            sample = sampler.getSample(new SensorState(sensorValues, Instant.now()));
            db.putStateScopeIntoDB(sample);
        }
    }
}