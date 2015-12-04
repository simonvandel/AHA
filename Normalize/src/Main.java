import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class Main {


    public static void main(String[] args) {
        // write your code here
        // write your code here
        Sensor s = new Sensor("ss", 1);

        Random rnd = new Random();
        for (int i = 0; i < 1499; i++)
            s.normalize(rnd.nextInt(550));
        while (true) {
            int temp = s.normalize(rnd.nextInt(550));
            try{
                Thread.sleep(1);
            } catch(InterruptedException e) {

            }

        }



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
