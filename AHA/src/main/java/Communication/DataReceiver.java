package Communication;

import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;

import java.time.Instant;

public class DataReceiver implements IDataReceiveListener {
    private IWorker worker;

    public DataReceiver(IWorker worker) {this.worker = worker;}

    @Override
    public void dataReceived(XBeeMessage xBeeMessage) {
        Instant timeReceived = Instant.now();
        String deviceAddress = xBeeMessage.getDevice().get64BitAddress().generateDeviceID();
        byte[] content = xBeeMessage.getData();
        PacketDetails packetDetails = new PacketDetails(timeReceived, content, deviceAddress);

        worker.process(packetDetails);
        /*SensorData sensorData;
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
        }*/
    }
}
