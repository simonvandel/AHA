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
        // numAnalog = 1 = 0b001
        // analogIndex = 0 = 0b000
        // 1stAnalog (10 bits) = 2 = 0b10
        // numDigital = 2 = 0b0010
        // indexDigital = 2 = 0b0010
        // --------BODY----------
        // 1stAnalogVal (NOT emulatable) = 900 = 0b1110000100
        // 1stDigitalVal (NOT emulatable) = 0 = 0b0
        // 2ndDigitalVal (emulatable) = 1 = 0b1

        // ----------SUMMARY-----------
        // byte0 = 0b10000001 (numAnalog + analogIndex + 1stAnalog)
        // byte1 = 0b00100010 (numDigital + indexDigital)
        // byte2 = 0b10000100 (first 8 bits least significant of 1stAnalog)
        // byte3 = 0b00001011 (last 2 bits of 1stAnalog + 1stDigital + 2ndDigital)
        byte[] bytes = new byte[]{(byte) 0b10000001, 0b00100010, (byte) 0b10000100, 0b00001011};
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