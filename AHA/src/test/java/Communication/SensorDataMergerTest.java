package Communication;

import Communication.Exceptions.InvalidValueSizeException;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SensorDataMergerTest {

    @Test
    public void threeSensorDataAdded() throws InvalidValueSizeException, InterruptedException {
        // use a countDownLatch to make sure that we wait for the callback for the merger to happen
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SensorDataMerger sensorDataMerger = new SensorDataMerger();

        // register for when the merger is done, and count the latch down
        sensorDataMerger.registerSensorStateComplete(x -> countDownLatch.countDown());

        byte[] bytes = new byte[]{(byte) 0b10000001, 0b00100010, (byte) 0b10000100, 0b00001011};

        PacketDetails packetDetails1 = new PacketDetails(Instant.ofEpochMilli(0), bytes, "1");
        SensorData sensorData1 = new SensorData(packetDetails1);

        PacketDetails packetDetails2 = new PacketDetails(Instant.ofEpochMilli(1), bytes, "2");
        SensorData sensorData2 = new SensorData(packetDetails2);

        PacketDetails packetDetails3 = new PacketDetails(Instant.ofEpochMilli(3), bytes, "3");
        SensorData sensorData3 = new SensorData(packetDetails3);

        sensorDataMerger.add(sensorData1);
        sensorDataMerger.add(sensorData2);
        sensorDataMerger.add(sensorData3);
        // wait for the timeout to trigger, so the merger will complete the snapshot
        Thread.sleep(sensorDataMerger.timeout.toMillis());

        // if it takes more than double the timeout for the callback to happen, something has gone wrong
        countDownLatch.await(sensorDataMerger.timeout.toMillis() * 2, TimeUnit.MILLISECONDS);
    }

}