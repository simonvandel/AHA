package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;
import Communication.SensorPacketWorker;

public class Main {

    public static void main(String[] args) {
        SensorPacketWorker sensorPacketWorker = new SensorPacketWorker();
        DataReceiver receiver = new DataReceiver(sensorPacketWorker);
        Communicator com = new Communicator("/dev/ttyUSB0", 9600, receiver);
    }
}