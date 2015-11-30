package Communication;

import Communication.Exceptions.InvalidValueSizeException;

import java.util.Queue;

public class SensorPacketWorker implements IWorker<PacketDetails, SensorState> {
    private Queue<SensorState> outputQueue;
    private SensorDataMerger merger;

    public SensorPacketWorker() {
        merger = new SensorDataMerger();
        // when a complete SensorState had been collected, add it to the outputQueue to pass it to the next stage
        merger.registerSensorStateComplete(sensorState -> outputQueue.add(sensorState));
    }

    @Override
    public void process(PacketDetails packetDetails) {
        try {
            SensorData sensorData = new SensorData(packetDetails);
            System.out.println(sensorData.getValues().get(0).getValue() + ", " + sensorData.getValues().get(1).getValue());

            merger.add(sensorData);
        } catch (InvalidValueSizeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerOutputTo(Queue<SensorState> outputQueue) {
        this.outputQueue = outputQueue;
    }
}
