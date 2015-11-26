package dk.sw501e15;

import Communication.DataReceiver;
import Communication.SensorPacketWorker;
import Database.DB;
import Normaliser.NormalizedSensorState;
import Normaliser.NormalizedValue;
import Sampler.Sample;
import Sampler.Sampler;

import java.time.Instant;

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
            NormalizedSensorState norm = new NormalizedSensorState(Instant.now());
            norm.AddNormalizedValue(new NormalizedValue(52123 % (i * 4032 + 1), false, "Device1", 1));
            norm.AddNormalizedValue(new NormalizedValue(121321 % (i * 1321 + 1), false, "Device1", 2));
            norm.AddNormalizedValue(new NormalizedValue(121321 % (i * 156 + 1), true, "Device1", 4));
            norm.AddNormalizedValue(new NormalizedValue(0 + i, true, "Device1", 3));
            norm.AddNormalizedValue(new NormalizedValue(4212315 % (i * 232 + 1), false, "Device1", 5));
            sample = sampler.getSample(norm);
            db.putStateScopeIntoDB(sample);
        }
    }
}