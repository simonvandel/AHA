import java.util.*;

public class Model implements Runnable {
    private List<Range> ranges = new ArrayList<>();

    private boolean modelBeingMade = false;
    private boolean modelBeingAssigned = false;
    private boolean modelBeingUsed = false;

    private  List<Integer> trainingData = new ArrayList<>();
    public int basedOnTrainingData = 0;

    public void setTrainingData(List<Integer> trainingData) {
        if(!(modelBeingAssigned || modelBeingMade))
            this.trainingData = trainingData;
    }

    public boolean getModelBeingMade() {
        return modelBeingMade;
    }

    public boolean getModelBeingAssigned() {
        return modelBeingAssigned;
    }

    public int determineNormalization(int toNormalize) {
        modelBeingUsed = true;
        int i = 0;
        for(Range r : ranges) {
            if (r.fits(toNormalize))
                return i;
            i++;
        }
        modelBeingUsed = false;
        return -1;
    }

    public void run() {
        System.out.println("Started thread");
        List<Range> rangesHolder;
        modelBeingMade = true;
        //call normalizer
        //PLACEHOLDER code
        rangesHolder = new ArrayList<>();
        //**
        while(modelBeingUsed); //busy wait for mutex
        modelBeingAssigned = true;
        modelBeingMade = false;
        //assign the returned normalized list of ranges
        ranges = rangesHolder;
        modelBeingAssigned = false;
        this.trainingData = null;
        System.out.println("ended thread");

    }

}
