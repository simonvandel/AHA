package Communication;

import Communication.Exceptions.InvalidValueSizeException;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by simon on 12/11/2015.
 */
public class SensorData {
    private String deviceAddress;
    private List<SensorValue> values = new ArrayList<SensorValue>();
    private Instant timeReceived;

    public SensorData(PacketDetails packetDetails) throws InvalidValueSizeException {
        deviceAddress = packetDetails.deviceAddress;
        timeReceived = packetDetails.timeReceived;
        values = decodeData(packetDetails.content);
    }

    /**
     * @param data a little endian representation of a sequence of bytes
     * @return Returns a list of sensor values, parsed from the raw packet
     */
    private List<SensorValue> decodeData(byte[] data) throws InvalidValueSizeException {
        List<SensorValue> returnList = new ArrayList<SensorValue>();
        BitSet bitSet = BitSet.valueOf(data);
        BitSetWrapper bsWrapper = new BitSetWrapper(bitSet);


        // ----------------- HEADER --------------------
        int numAnalogValues = bsWrapper.getIntFromBits(3);
        int indexFirstAnalog = bsWrapper.getIntFromBits(3);

        // Contains the bits the values occupy and whether the sensor is emulatable
        List<Pair<Integer, Boolean>> analogValueInfo = new ArrayList<>(numAnalogValues);

        // Read numAnalogValues from header, and store how many bits the sensorValues occupy in the body
        for (int i = 0; i < numAnalogValues; i++) {
            int sizeInfo = bsWrapper.getIntFromBits(2);
            Boolean isEmulatable = false;

            // if the index of emutability is 0, there is no emutable sensors
            if (indexFirstAnalog == 0) {
                isEmulatable = false;
            }
            // else, if we are looking at sensor at index indexFirstAnalog+1, it is emulatable
            else if ((i + 1) >= indexFirstAnalog) {
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
            analogValueInfo.add(i, Pair.with(bitSize, isEmulatable));
        }

        int numDigitalValues = bsWrapper.getIntFromBits(4);
        int indexFirstDigital = bsWrapper.getIntFromBits(4);

        // ----------------------- BODY -----------------------
        // analogValues
        // sensorIndex marks the sensor number on the device
        int sensorIndex = 0;
        for (Pair<Integer,Boolean> pair: analogValueInfo) {
            // read the sensor values. The bits to read are stored in the pair
            int sensorValue = bsWrapper.getIntFromBits(pair.getValue0());
            returnList.add(new SensorValue(sensorValue, pair.getValue1(), deviceAddress, sensorIndex));
            // go to the next sensor index
            sensorIndex++;
        }
        // digitalValues
        for (int i = 0; i < numDigitalValues; i++) {
            // read the sensor values. We are parsing digital values, so each value is 1 bit
            int sensorValue = bsWrapper.getIntFromBits(1);
            boolean isEmulatable = false;
            // if the index of emutability is 0, there is no emutable sensors
            if (indexFirstDigital == 0) {
                isEmulatable = false;
            }
            // else, if we are looking at sensor at index indexFirstDigital+1, it is emulatable
            else if ((i + 1) >= indexFirstDigital) {
                isEmulatable = true;
            }
            returnList.add(new SensorValue(sensorValue, isEmulatable, deviceAddress, sensorIndex));
            // go to the next sensor index
            sensorIndex++;
        }

        return returnList;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public List<SensorValue> getValues() {
        return values;
    }

    public Instant getTimeReceived() {
        return timeReceived;
    }
}
