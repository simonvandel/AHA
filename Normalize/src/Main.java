import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Main {


    public static void main(String[] args) {
        // write your code here



    }

    //portential implementation of Sensor list. HashSet ensures object exclusivity
    static Set<Sensor> sensors = new HashSet<>();
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
