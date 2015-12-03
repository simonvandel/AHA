import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Main {

    static Set<Sensor> sensors = new HashSet<>();

    public static void main(String[] args) {
        // write your code here


        String deviceId = "ss";
        int sensorIndex = 0;
        for (sensorIndex = 0; sensorIndex < 110; sensorIndex++) {
            if (!sensors.add(new Sensor(deviceId, sensorIndex))) {
                norm(deviceId, sensorIndex).normalize(2);
            }
        }
        for (sensorIndex = 0; sensorIndex < 110; sensorIndex++) {
            if (!sensors.add(new Sensor(deviceId, sensorIndex))) {
                norm(deviceId, sensorIndex).normalize(2);
            }
        }
        sensorIndex++;

    }

    public static Sensor norm(String deviceId, int sensorIndex)  {
        for (Iterator<Sensor> sIte = sensors.iterator(); sIte.hasNext(); ) {
            Sensor s = sIte.next();
            if(s.getDeviceID().equals(deviceId) && s.getSensorIndex() == sensorIndex) {
                return s;
            }
        }
        throw new IllegalArgumentException("The item should always exsist when calling this method(Sensor norm)");
    }
}
