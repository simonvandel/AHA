package Communication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by simon on 17/11/2015.
 */
public class SensorDataLogger implements Runnable {
    private File file;
    private List<SensorData> sensorData;

    public SensorDataLogger(File file, List<SensorData> snapshot) {
        this.file = file;
        this.sensorData = snapshot;
    }

    @Override
    public void run() {
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.append(serializeSensorData(sensorData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CharSequence serializeSensorData(List<SensorData> sensorData) {
        StringBuilder stringBuilder = new StringBuilder();
        // TODO: append data til stringBuilder. Find ud af hvilket format
        return stringBuilder;
    }
}
