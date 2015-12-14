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
        int numNonBinaryValues = bsWrapper.getIntFromBits(5);
        int indexFirstNonBinaryEmulatable = bsWrapper.getIntFromBits(5);

        // Contains the bits the values occupy and whether the sensor is emulatable
        List<Pair<Integer, Boolean>> analogValueInfo = new ArrayList<>(numNonBinaryValues);

        // Read numNonBinaryValues from header, and store how many bits the sensorValues occupy in the body
        for (int i = 0; i < numNonBinaryValues; i++) {
            int sizeInfo = bsWrapper.getIntFromBits(1);
            Boolean isEmulatable = false;

            // if the index of emutability is 0, there is no emutable sensors
            if (indexFirstNonBinaryEmulatable == 0) {
                isEmulatable = false;
            }
            // else, if we are looking at sensor at index indexFirstNonBinaryEmulatable+1, it is emulatable
            else if ((i + 1) >= indexFirstNonBinaryEmulatable) {
                isEmulatable = true;
            }

            int bitSize = 0;
            switch (sizeInfo) {
                case 0: bitSize = 10; break;
                case 1: bitSize = 32; break;

                default: throw new InvalidValueSizeException("Size of sensor values must be between 0-1");
            }
            analogValueInfo.add(i, Pair.with(bitSize, isEmulatable));
        }

        int numDigitalValues = bsWrapper.getIntFromBits(5);
        int indexFirstBinary = bsWrapper.getIntFromBits(5);

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
            if (indexFirstBinary == 0 || indexFirstBinary == 1) {
                isEmulatable = false;
            }
            // else, if we are looking at sensor at index indexFirstBinary+1, it is emulatable
            //Because unknown error we cant define index 1, but we still want to address it so,
            // we address index 2 technically on the sender, and changes it here to index one. Hence the indexFirstBinary-1
            else if ((i + 1) >= indexFirstBinary-1) {
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
