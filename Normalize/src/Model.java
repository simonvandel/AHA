import java.util.*;

public class Model implements Runnable {
    private List<Range> ranges = new ArrayList<>(); //what is acctually checked against when normalizing

    //mutex variables**
    private boolean modelBeingMade = false;
    private boolean modelBeingAssigned = false;
    private boolean modelBeingUsed = false;
    //**
    private  List<Integer> trainingData = new ArrayList<>(); //temporary data for generating a model, is null except when
                                                            //generating the model (ie. in the method run())
    public int basedOnTrainingData = 0; //tells us how much history the last model where generated using

    public void setTrainingData(List<Integer> trainingData) {
        if(!(modelBeingAssigned || modelBeingMade)) //makes sure the training data isn't changed doing execution of run()
            this.trainingData = trainingData;
    }

    public boolean getModelBeingMade() {
        return modelBeingMade;
    }

    public boolean getModelBeingAssigned() {
        return modelBeingAssigned;
    }

    //determines which cluster the input fits into and returns it. The 'cluster' is the index of the range as defined in the normalizer class
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

    //TODO implement the normalizer
    //modelBeingMade and modelBeingAssigned are mutex variables, there are two because we dont want to start generating a new model in the middle of generating one
    //but we also dont want to lock out the main loop while generating, when its only nessacary when implementing/assigning the new model
    public void run() {
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
        basedOnTrainingData = trainingData.size();
        this.trainingData = null;

    }

}
