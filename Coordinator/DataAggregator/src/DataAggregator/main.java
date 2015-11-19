/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataAggregator;

import DataAggregator.Exceptions.InvalidValueSizeException;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;

import java.io.File;
import java.util.BitSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static java.lang.System.out;

/**
 *
 * @author brugeren
 */
public class main implements IDataReceiveListener,
        IDiscoveryListener {

    private static String PORT = "/dev/ttyUSB0";
    // TODO Replace with the baud rate of you receiver module.
    private static final int BAUD_RATE = 9600;
    private XBeeDevice device;
    private XBeeNetwork network;
    private ThreadPoolExecutor threadPoolExecutor;
    private BlockingQueue<Runnable> workQueue;

    //setup timer to continuisly discover network
    Timer timer;
    final TimerTask timerTask = new TimerTask() {
        public void run() {
            DiscoverNetwork();
        }
    };
    private SensorDataMerger merger;
    private File sensorValueFile;

    /**
     * Creates new form main
     */
    public main() {

        //XBee device init
        device = new XBeeDevice(PORT, BAUD_RATE);

        merger = new SensorDataMerger();
        workQueue = new LinkedBlockingDeque<>();
        threadPoolExecutor = new ThreadPoolExecutor(2,4,0, TimeUnit.MILLISECONDS,workQueue);
        sensorValueFile = new File("sensorValues.txt");

        try {
            //If device can open, device is succesfully connected
            try {
                device.open();
            }
            catch (XBeeException e) {
                out.printf("Could not open device %s", PORT);
                System.exit(2);
            }

            out.println("Device succesfully connected");

            //Invokes dataReceived when data is received, other listener available that raises on "packet received"
            device.addDataListener(this);

            network = device.getNetwork();
            network.addDiscoveryListener(this);
            network.setDiscoveryTimeout(5000);
            DiscoverNetwork();

            //starts timer to update network or some other thing
            /*
            timer = new Timer();
            timer.schedule(timerTask, 15000);
            */
        } catch (XBeeException e) {
            out.println("Device not found:\n" + e.getMessage());
        }

    }

    public void DiscoverNetwork() {
        out.println("\nDiscovering network");
        network.startDiscoveryProcess();
    }

    private void SendData(byte[] dataToSend, XBee64BitAddress sendTo) {
        try {
            // Obtain the remote XBee device from the XBee network.
            RemoteXBeeDevice remoteDevice = network.getDevice(sendTo);
            if (remoteDevice == null) {
                out.println("\nCouldn't find the remote XBee device with '" + sendTo + "'64bit address.");
            }

            out.println(String.format("\nSending data to %s >> %s | %s... ", remoteDevice.get64BitAddress(),
                    new String(dataToSend)));

            device.sendData(remoteDevice, dataToSend);

            out.println("\nSuccess");

        } catch (XBeeException e) {
            out.println("\nError sending data to specific address: ");
            e.printStackTrace();
        }
    }

    private void BroadcastData(String toSend) {
        try {
            out.println("\n"
                    + String.format("Sending broadcast data: '%s'...%n",
                            new String(toSend)));
            device.sendBroadcastData(toSend.getBytes());

            out.println("\nSuccess");

        } catch (XBeeException e) {
            out.println("\nError:\n" + e.getMessage());
        }
    }

    @Override
    public void dataReceived(XBeeMessage xbeeMessage) {
        /*out.println("\n"
                + String.format("From %s >> %s | %s%n", xbeeMessage.getDevice()
                        .get64BitAddress(), new String(xbeeMessage.getData())));*/
        //decodeMessage(xbeeMessage)
        SensorData sensorData;
        try {
            sensorData = new SensorData(xbeeMessage);
            merger.add(sensorData);
            // if we have a complete snapshot, add it to the workqueue, to write to file
            if(merger.snapshotReady()) {
                SensorDataLogger logger = new SensorDataLogger(sensorValueFile, merger.getSnapshot());
                workQueue.add(logger);
            }

        } catch (InvalidValueSizeException e) {
            // packet received was malformed, so ignore the packet
        }
    }

    @Override
    public void deviceDiscovered(RemoteXBeeDevice discoveredDevice) {
        out.println("\n"
                + String.format(">> Device discovered: %s%n",
                        discoveredDevice.toString()));
    }

    @Override
    public void discoveryError(String error) {
        out.println("\n>> There was an error discovering devices: "
                + error);
    }

    @Override
    public void discoveryFinished(String error) {
        if (error == null) {
            out.println("\n>> Discovery process finished successfully.");
        } else {
            out.println("\n>> Discovery process finished due to the following error: "
                            + error);
        }

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if (args.length > 0) {
            PORT = args[0];
        }
        //byte[] x = {127, 1};

        //System.out.println( "" + bitInteger );
        new main();
    }
}
