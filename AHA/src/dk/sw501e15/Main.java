package dk.sw501e15;

import Communication.Communicator;
import Communication.DataReceiver;

public class Main {

    public static void main(String[] args) {
        DataReceiver receiver = new DataReceiver();
	    Communicator com = new Communicator("/dev/ttyUSB0", 9600, receiver);
    }
}