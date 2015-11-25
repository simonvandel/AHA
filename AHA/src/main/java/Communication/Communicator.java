package Communication;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;

import static java.lang.System.out;

/**
 * Created by simon on 19/11/2015.
 */
public class Communicator {
    private XBeeDevice device;
    private XBeeNetwork network;

    public Communicator(String devicePort, int baudRate, IDataReceiveListener dataListener) {
        //XBee device init
        device = new XBeeDevice(devicePort, baudRate);

//        merger = new SensorDataMerger();
//        workQueue = new LinkedBlockingDeque<>();
//        threadPoolExecutor = new ThreadPoolExecutor(2,4,0, TimeUnit.MILLISECONDS,workQueue);
//        sensorValueFile = new File("sensorValues.txt");

        //If device can open, device is succesfully connected
        try {
            device.open();
        }
        catch (XBeeException e) {
            out.printf("Could not open device %s", devicePort);
            System.exit(2);
        }

        out.println("Device succesfully connected");

        //Invokes dataReceived when data is received, other listener available that raises on "packet received"
        device.addDataListener(dataListener);

        network = device.getNetwork();
        //network.addDiscoveryListener(this);
        //network.setDiscoveryTimeout(5000);
        //DiscoverNetwork();

        //starts timer to update network or some other thing
            /*
            timer = new Timer();
            timer.schedule(timerTask, 15000);
            */
    }
}
