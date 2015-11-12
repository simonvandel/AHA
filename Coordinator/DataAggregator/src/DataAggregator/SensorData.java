package DataAggregator;

import com.digi.xbee.api.models.XBeeMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 12/11/2015.
 */
public class SensorData {
    private String device;
    private List<Integer> values = new ArrayList<>();

    public SensorData(XBeeMessage msg) {
        device = msg.getDevice().toString(); // TODO: probably do not just use toString method
        values = decodeData(msg.getData());
    }

    /**
     * @param data
     * @return Returns a list of sensor values, parsed from the raw packet
     */
    private List<Integer> decodeData(byte[] data) {
        return null;
    }

    public String getDevice() {
        return device;
    }
}
