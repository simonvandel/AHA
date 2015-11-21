package Communication;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class merges sensor data from many different devices into one snapshot.
 * A timeout specifies for how long the merger has to wait to receive packets from all devices.
 * If the timeout is passed, the packets still missing will be replaced with the last received sensor values of that device.
 */
public class SensorDataMerger {
    // millis
    public final Duration timeout = Duration.ofMillis(500);
    // A list<SensorData> is a snapshot; a collection of sensordata
    private List<SensorData> snapshot;
    private List<String> activeDevices;
    private int devicesLeftToReceiveFrom = 0;
    // keeps track of the last known package received for a given device. The key is the device, and the value the last know sensorData
    private Map<String, SensorData> lastKnownSensorData;
    private Instant startNewSnapshotTime;
    // initialize to do nothing
    private Consumer<SensorState> sensorStateComplete = sensorState -> {
    };

    public SensorDataMerger() {
        lastKnownSensorData = new HashMap<>();
        snapshot = new ArrayList<>();
        activeDevices = new ArrayList<>();
    }

    private int calcDevicesLeftToReceiveFrom () {
        return activeDevices.size() - snapshot.size();
    }

    public void add(SensorData sensorData) {
        // if startNewSnapshotTIme is null, this is the first call to add, meaning the first snapshot time must start
        if (startNewSnapshotTime == null) {
            startNewSnapshotTime = Instant.now();
        }

        // check if we have passed the timeout
        if (startNewSnapshotTime.isAfter(Instant.now().plus(timeout))) {
            // We passed the timeout, so we should finish up the merging.
            // If we are at this stage, it means that we did not receive packets from all devices in time.
            // This could be due to the device being offline, or simply that the packet did not arrive to us.

            // We now find which devices we did not receive a packet from.
            List<String> devicesInSnapshot = snapshot
                    .stream()
                    .map(SensorData::getDeviceAddress)
                    .collect(Collectors.toList());

            activeDevices.stream().forEach(device -> {
                // if the device is not in our list of devices received from, we have not received a packet from it
                if (!devicesInSnapshot.contains(device)) {
                    // As we did not receive any sensor data, we will use the sensor data received earlier in time.
                    // If no prior sensor data has been recorded for the device,
                    // no sensor data is added to the snapshot
                    if (lastKnownSensorData.containsKey(device)) {
                        snapshot.add(lastKnownSensorData.get(device));

                        // decrement, as we have just added a device to the snapshot
                        devicesLeftToReceiveFrom--;
                    }
                }
            });

            // the timeout passed, so we begin a new snapshot, marking the old as complete
            beginNewSnapshot();
        }

        // Normal operation

        String device = sensorData.getDeviceAddress();
        // if the device has not been seen before, add it to our list of active devices
        if(!activeDevices.contains(device)){
            activeDevices.add(device);
            devicesLeftToReceiveFrom = calcDevicesLeftToReceiveFrom();
        }

        // we received a packet, so store it in lastKnownSensorData
        lastKnownSensorData.put(device, sensorData);

        // ensure that we do not add packets from the same device in the snapshot
        if (snapshot.stream()
                .filter(x -> Objects.equals(x.getDeviceAddress(), sensorData.getDeviceAddress())).count() == 0) {
            snapshot.add(sensorData);
            devicesLeftToReceiveFrom = calcDevicesLeftToReceiveFrom();
        }

        // check if we have received from all devices, and are therefore ready for the next snapshot
        if (devicesLeftToReceiveFrom == 0) {
            beginNewSnapshot();
        }
    }

    private SensorState makeSensorState() {
        // we need to return the same order of devices every time, so we sort it based on device address
        snapshot.sort(Comparator.comparing(SensorData::getDeviceAddress));
        List<SensorValue> sensorValues = new ArrayList<>(snapshot)
                .stream()
                .flatMap(sv -> sv.getValues().stream())
                .collect(Collectors.toList());

        // The time of the sensorState is simply the newest sensor data
        Instant newestSensorData = snapshot
                .stream()
                .max(Comparator.comparing(SensorData::getTimeReceived))
                .get()
                .getTimeReceived();

        return new SensorState(sensorValues, newestSensorData);
    }

    private void beginNewSnapshot() {
        // Tell the listener that we completed a sensorState
        sensorStateComplete.accept(makeSensorState());
        snapshot.clear();
        devicesLeftToReceiveFrom = activeDevices.size();
        // start clock
        startNewSnapshotTime = Instant.now();
    }

    public void registerSensorStateComplete(Consumer<SensorState> callback) {
        sensorStateComplete = callback;
    }
}
