package Communication;

import Communication.Exceptions.InvalidValueSizeException;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 11/21/15.
 */
public class SensorDataTest {

    @Test
    public void testDecode() throws InvalidValueSizeException {
        // --------HEADER----------
        // numNonBinary = 1 = 0b00001
        // IndexNonBinary = 0 = 0b00000
        // 1stNonBinary (10 bits) = 0 = 0b0
        // numBinary = 2 = 0b00010
        // indexBinary = 3 = 0b00011 // hack index
        // --------BODY----------
        // 1stNonBinaryVal (NOT emulatable) = 900 = 0b1110000100
        // 1stBinaryVal (NOT emulatable) = 0 = 0b0
        // 2ndBinaryVal (emulatable) = 1 = 0b1

        // ----------SUMMARY-----------
        // byte0 = 0b000 00001 (numNonBinary + (first 3 bits least significant of indexNonBinary))
        // byte1 = 0b00010 0 00 ((last 2 bits of indexNonBinary) + 1stNonBinary + numBinary)
        // byte2 = 0b100 00011 (indexBinary + first 3 bits of 1stNonBinaryVal)
        // byte3 = 0b0 1110000 (last 7 bits of 1stNonBinaryVal + 1stBinaryVal)
        // byte4 = 0b0000000 1 (2ndBinaryVal + rest is nothing)
        byte[] bytes = new byte[]{(byte) 0b00000001, 0b00010000, (byte) 0b10000011, (byte) 0b01110000, 0b00000001};
        PacketDetails packetDetails = new PacketDetails(Instant.ofEpochMilli(0), bytes, "");
        List<SensorValue> sensorValues = new SensorData(packetDetails).getValues();
        // 1st analog value
        assertEquals(900, sensorValues.get(0).getValue());
        assertEquals(false, sensorValues.get(0).isEmulatable());
        // 1st digital value
        assertEquals(0, sensorValues.get(1).getValue());
        assertEquals(false, sensorValues.get(1).isEmulatable());
        // 2nd digital value
        assertEquals(1, sensorValues.get(2).getValue());
        assertEquals(true, sensorValues.get(2).isEmulatable());
    }
}