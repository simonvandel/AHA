package Communication;

import Communication.Exceptions.InvalidValueSizeException;
import com.digi.xbee.api.models.XBeeMessage;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

/**
 * Created by simon on 12/11/2015.
 */
public class SensorData {
    private String device;
    private List<SensorValue> values = new ArrayList<>();
    private Date time;

    public SensorData(XBeeMessage msg) throws InvalidValueSizeException {
        device = msg.getDevice().get64BitAddress().generateDeviceID();
        values = decodeData(msg.getData());
    }

    /**
     * @param data
     * @return Returns a list of sensor values, parsed from the raw packet
     */
    private List<SensorValue> decodeData(byte[] data) throws InvalidValueSizeException {
        List<SensorValue> returnList = new ArrayList<>();
        BitSet bitSet = new BitSet().valueOf(data);
        BitSetWrapper bsWrapper = new BitSetWrapper(bitSet);

        // ----------------- HEADER --------------------
        int numAnalogValues = bsWrapper.getIntFromBits(3);
        int indexFirstAnalog = bsWrapper.getIntFromBits(3);

        // Contains the bits the values occupy and whether the sensor is emulatable
        List<Pair<Integer,Boolean>> analogValueInfo = new ArrayList<>();

        // Read numAnalogValues from header, and store how many bits the sensorValues occupy in the body
        for (int i = 0; i < numAnalogValues; i++) {
            int sizeInfo = bsWrapper.getIntFromBits(2);
            Boolean isEmulatable = false;

            // if the sensor value number is equal or bigger than the indexFirstAnalog
            if ((i+1) >= indexFirstAnalog){
                isEmulatable = true;
            }

            int bitSize = 0;
            switch (sizeInfo) {
                case 0: bitSize = 0; break;
                case 1: bitSize = 1; break;
                case 2: bitSize = 10; break;
                case 3: bitSize = 32; break;

                default: throw new InvalidValueSizeException("Size of sensor values must be between 0-3");
            }
            analogValueInfo.set(i, Pair.with(bitSize,isEmulatable));
        }

        int numDigitalValues = bsWrapper.getIntFromBits(4);
        int indexFirstDigital = bsWrapper.getIntFromBits(4);

        // BODY -----------------------
        // analogValues
        for (Pair<Integer,Boolean> pair: analogValueInfo) {
            // read the sensor values. The bits to read are stored in the pair
            int sensorValue = bsWrapper.getIntFromBits(pair.getValue0());
            returnList.add(new SensorValue(sensorValue, pair.getValue1()));
        }
        // digitalValues
        for (int i = 0; i < numDigitalValues; i++) {
            // read the sensor values. We are parsing digital values, so each value is 1 bit
            int sensorValue = bsWrapper.getIntFromBits(1);
            boolean isEmulatable = false;
            // if the sensor value number is equal or bigger than the indexFirstAnalog
            if ((i+1) >= indexFirstDigital){
                isEmulatable = true;
            }
            returnList.add(new SensorValue(sensorValue, isEmulatable));
        }

        return returnList;
    }

    public String getDevice() {
        return device;
    }

    public List<SensorValue> getValues() {
        return values;
    }

    public Date getTime() {
        return time;
    }
}
