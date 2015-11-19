package Communication;

import Communication.Exceptions.InvalidValueSizeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;

/**
 * Created by simon on 19/11/2015.
 */
public class DataReceiver implements IDataReceiveListener {
    private SensorDataMerger merger;

    public DataReceiver() {
        merger = new SensorDataMerger();
    }

    @Override
    public void dataReceived(XBeeMessage xBeeMessage) {
        SensorData sensorData;
        try {
            sensorData = new SensorData(xBeeMessage);
            merger.add(sensorData);
            // if we have a complete snapshot, add it to the workqueue, to write to file
            if(merger.snapshotReady()) {
                //SensorDataLogger logger = new SensorDataLogger(sensorValueFile, merger.getSnapshot());
                //workQueue.add(logger);
            }

        } catch (InvalidValueSizeException e) {
            // packet received was malformed, so ignore the packet
        }
    }
}
