package Communication;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBee64BitAddress;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.out;

/**
 * Created by simon on 19/11/2015.
 */
public class Communicator
{
  private XBeeDevice device;
  private XBeeNetwork network;
  private Logger logger;

  public Communicator(String devicePort, int baudRate, IDataReceiveListener dataListener, Logger comLogger)
  {
    //XBee device init
    device = new XBeeDevice(devicePort, baudRate);

//        merger = new SensorDataMerger();
//        workQueue = new LinkedBlockingDeque<>();
//        threadPoolExecutor = new ThreadPoolExecutor(2,4,0, TimeUnit.MILLISECONDS,workQueue);
//        sensorValueFile = new File("sensorValues.txt");

    //If device can open, device is succesfully connected
    try
    {
      device.open();
    } catch (XBeeException e)
    {
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
    logger = comLogger;
  }

  public boolean SendData(String addr64, byte[] toSend) throws XBeeException
  {
    for (int i = 0; i < network.getDevices().size(); i++)
    {
      String currID = network.getDevices().get(i).get64BitAddress().generateDeviceID();
      if (currID.equals(addr64))
      {
        device.sendData(network.getDevices().get(i), toSend);
        logger.log(Level.INFO ,"Sent data to device: " + currID);
        return true;
      }
    }
    network.startDiscoveryProcess(); //should we check if we found the device and try to send again?
    logger.log(Level.SEVERE, "Failed sending data. Started network discovery process");
    return false;

  }
}
