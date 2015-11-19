package Communication;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class merges sensor data from many different devices into one snapshop.
 */
public class SensorDataMerger {
    // A list<SensorData> is a snapshot; a collection of sensordata
    private List<SensorData> snapshot;
    private List<String> activeDevices;
    private int devicesLeftToReceiveFrom = 0;

    // keeps track of the last known package received for a given device
    private Map<String,SensorData> lastKnownPackages;
    private long startNewSnapshotTime;

    // millis
    private final int timeout = 500;

    public SensorDataMerger() {
        lastKnownPackages = new HashMap<>();
        snapshot = new ArrayList<>();
        activeDevices = new ArrayList<>();
    }

    private int calcDevicesLeftToReceiveFrom () {
        return activeDevices.size() - snapshot.size();
    }

    public void add(SensorData sensorData) {
        // check if we have passed the timeout
        if(startNewSnapshotTime + timeout > System.currentTimeMillis()) {
            // find out which devices we have not received data from
            List<String> devicesInSnapshot = snapshot.stream().map(x -> x.getDevice()).collect(Collectors.toList());
            //union()
        }
        String device = sensorData.getDevice();
        // if the device has not been seen before, add it to our list of active devices
        if(!activeDevices.contains(device)){
            activeDevices.add(device);
            devicesLeftToReceiveFrom = calcDevicesLeftToReceiveFrom();
        }

        // we received a packet, so store it in lastKnownPackages
        lastKnownPackages.put(device, sensorData);

        // ensure that we do not add packets from the same device in the snapshot
        if (snapshot.stream()
                .filter(x -> Objects.equals(x.getDevice(), sensorData.getDevice())).count() == 0) {
            snapshot.add(sensorData);
            devicesLeftToReceiveFrom = calcDevicesLeftToReceiveFrom();
        }
    }

    public boolean snapshotReady() {
        return devicesLeftToReceiveFrom == 0;
    }

    public List<SensorData> getSnapshot() {
        List<SensorData> toReturn = new ArrayList<>(snapshot);
        beginNewSnapshot();
        return toReturn;
    }

    private void beginNewSnapshot() {
        // start clock
        startNewSnapshotTime = System.currentTimeMillis();
        snapshot.clear();
        devicesLeftToReceiveFrom = activeDevices.size();
    }

    /**
     * @param l1 collection one
     * @param l2 collection two.
     * @return Returns the union of two collections
     */
    private List<String> union(List<String> l1, List<String>l2){
        Set<String> union = new HashSet<>();

        union.addAll(l1);
        union.addAll(l2);
        return new ArrayList<>(union);
    }
}
