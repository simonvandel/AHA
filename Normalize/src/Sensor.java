import java.util.*;

/**
 * Created by brugeren on 03-12-2015.
 */
public class Sensor {

    private String deviceID = "";
    private int sensorIndex = -1;
    private List<Integer> trainingData = new ArrayList<>();
    private Model oModel = null;

    private int trainingDataThreshhold = 1500;

    public String getDeviceID() {
        return deviceID;
    }

    public int getSensorIndex(){
        return sensorIndex;
    }

    public Sensor(String device, int sensorIndex) {
        this.deviceID = device;
        this.sensorIndex = sensorIndex;
    }

    public int normalize(int toNormalize) {
        int toReturn = -1;

        trainingData.add(toNormalize);
        //if model is null we it means we havnt had enough trainingdata for that sensor to make a model, so we start making one
        //else we normalize the model, as long as the model isn't being assigned by the model creation thread Then check wether or not we should update our model
        if (oModel == null) {
            if (trainingData.size() > trainingDataThreshhold) {
                createModelThread();
            }
        } else {
            if (!oModel.getModelBeingAssigned())
                toReturn = oModel.determineNormalization(toNormalize);
            if ((trainingData.size() - oModel.basedOnTrainingData) > trainingDataThreshhold)
                createModelThread();
        }
        return toReturn;
    }

    private void createModelThread() {
        if(oModel == null)
            oModel = new Model();

        if (!(oModel.getModelBeingAssigned() || oModel.getModelBeingMade())) {
            oModel.setTrainingData(trainingData);
            (new Thread(oModel)).start();
        }
        //possibly check whether or not we should remove some of the training data?
        //if(trainingData.size() > )
    }


    @Override
    public int hashCode() {
        int hash = 17 + deviceID.hashCode();
        hash = 31 + sensorIndex;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o  instanceof Sensor))
            return false;
        if(o == this)
            return true;

        Sensor toCompareTo = (Sensor) o;
        if((toCompareTo.deviceID == this.deviceID) && (toCompareTo.sensorIndex == this.sensorIndex)) {
            return true;
        }
        return false;
    }

}
