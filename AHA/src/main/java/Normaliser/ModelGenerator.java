package Normaliser;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by brugeren on 04-12-2015.
 */
public class ModelGenerator {

    //Elbow Method

    public int DetermineNumberOfClusters(List<Integer> trainingData) {
        //First sort the TrainingDAta
        Collections.sort(trainingData);

        //Assume that we have 2 clusters Then calculate the variance..
        double varianceDifference;
        double treshHold = 0.05;

        int numOfClusters = 2;
        do {
            //split trainingData into numOfClusers
            List<List<Integer>> clustersN = new ArrayList<>(splitIntoClusers(trainingData, numOfClusters));


            //calculate percent variance = pV1
            double cluster1Variance = findClusterVariance(clustersN);

            cluster1Variance /= 100;
            //System.out.println("cV1: " + cluster1Variance);

            //split trainingData into numOfClusters+1
            clustersN = new ArrayList<>(splitIntoClusers(trainingData, numOfClusters + 1));
            //calculate percent variance = pV2
            double cluster2Variance = findClusterVariance(clustersN);
            cluster2Variance /= 100;
            //System.out.println("cV2: " + cluster2Variance);
            numOfClusters++;

            // varianceDifference = pV1 - pV2;
            varianceDifference = Math.abs((cluster2Variance - cluster1Variance));
            // System.out.println("dV: " + varianceDifference);
        } while (varianceDifference > treshHold);

        return numOfClusters;
    }

    private ArrayList<ArrayList<Integer>> splitIntoClusers(List<Integer> superSet, int numOfSplit) {

        int subSetSize = (int) Math.floor(superSet.size() / numOfSplit);
        ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < numOfSplit; i++) {
            int indexTo = (i + 1) * subSetSize;
            toReturn.add(new ArrayList<>(superSet.subList(i * subSetSize, indexTo - 1)));
            //toReturn.add(superSet.subList(i * subSetSize, indexTo - 1));
        }
        if (superSet.size() > numOfSplit * subSetSize) {
            toReturn.get(numOfSplit - 1).add(superSet.get(superSet.size() - 1));
        }
        return toReturn;
    }

    private double findVarianceFromClusterDistance(int sizeOfCluster, double Dk) {
        double toReturn = -1;
        double brok = (1 / (2 * (double) sizeOfCluster));
        toReturn = brok * Dk;
        return toReturn;
    }

    private double findDistanceInCluster(List<Integer> cluster) {

        //assumes sorted
        int median = cluster.get(cluster.size() / 2);

        double Dk = 0;

        for (int i = 0; i < cluster.size(); i++) {
            Dk += findDistance(cluster.get(i), median);
        }
        return Dk;
    }

    private double findDistance(int point1, int point2) {
        return Math.abs(point1 - point2);
    }

    private double findClusterVariance(List<List<Integer>> clusters) {
        double clusterVariance = 0;
        for (int i = 0; i < clusters.size(); i++) {
            double Dk = findDistanceInCluster(clusters.get(i));
            clusterVariance += findVarianceFromClusterDistance(clusters.get(i).size(), Dk);
        }

        return clusterVariance;
    }
//End of elbow

    public List<Range> generateModel(List<Integer> trainingData) {
        int numClusters = DetermineNumberOfClusters(trainingData);
        List<List<Integer>> clusters = new ArrayList<>(splitIntoClusers(trainingData, numClusters));

        List<Double> gvfs = new ArrayList<>();
        double sdam = sumOfSquaredDeviationFromMean(trainingData);
        int elementToMove;
        double sdbc = 0;
        double sdcm = 0;
        int j = 0;
        boolean enough = false;
        boolean preEnough = false;

        double gvf = 0;
        double treshhold = 0.90;

        do {
            int indexForLowestSdbc = -1;
            int indexForHighestSdbc = -1;
            double highestSdbc = -1;
            double lowestSdbc = Double.MAX_VALUE;

            int i = 0;
            for (List<Integer> cluster : clusters) {
                sdbc = findDeviation(cluster);
                if (sdbc > highestSdbc) {
                    highestSdbc = sdbc;
                    indexForHighestSdbc = i;
                }
                if (sdbc < lowestSdbc) {
                    lowestSdbc = sdbc;
                    indexForLowestSdbc = i;
                }
                i++;
            }

            int elementindex = 0;
            if (indexForHighestSdbc > indexForLowestSdbc) {
                elementindex = 0;
                elementToMove = clusters.get(indexForHighestSdbc).get(0);

                clusters.get(indexForHighestSdbc - 1).add(elementToMove);
                clusters.get(indexForHighestSdbc).remove(0);
                Collections.sort(clusters.get(indexForHighestSdbc - 1));
            } else if (indexForHighestSdbc < indexForLowestSdbc) {
                elementindex = clusters.get(indexForHighestSdbc).size() - 1;
                elementToMove = clusters.get(indexForHighestSdbc).get(elementindex);
                clusters.get(indexForHighestSdbc + 1).add(elementToMove);
                clusters.get(indexForHighestSdbc).remove(elementindex);
                Collections.sort(clusters.get(indexForHighestSdbc + 1));
            }

            sdbc = findDeviation(clusters.get(indexForLowestSdbc));
            sdcm = (sdam - sdbc);
            gvf = (sdam - sdcm) / sdam;

            gvfs.add(j, gvf);
            j++;
            if (gvfs.size() >= 4) {
                enough = (Double.compare(gvfs.get(0), gvfs.get(2)) == 0 && gvfs.get(1).compareTo(gvfs.get(3)) == 0);
                if (enough == false)
                    gvfs.clear();
                else {
                    int evt = Double.compare(gvfs.get(0), gvfs.get(1));
                    enough = evt != 0;
                    switch (evt){
                        case -1:
                            enough = Double.compare(gvfs.get(1), gvf) == 0;
                            break;
                        case 1:
                            enough = Double.compare(gvfs.get(0), gvf) == 0;
                            break;
                        default:
                            gvfs.clear();
                    }
                }


                j = 0;
                // System.out.println(enough);
            }

        } while (!enough);
        return createRanges(clusters);
    }

    private List<Range> createRanges(List<List<Integer>> normCluters) {
        int i = 0;
        int lowerBound = 0;
        int upperBound = 0;


        List<Range> toReturn = new ArrayList<Range>();
        for(List<Integer> cluster : normCluters) {
            upperBound = cluster.get(cluster.size()-1);
            toReturn.add(new Range(lowerBound, upperBound));
            lowerBound = upperBound +1;
        }
        return toReturn;
    }
    //helper methods for jenks


    private double findAverage(List<Integer> cluster) {
        long sum = cluster.stream().mapToInt(Integer::intValue).sum();
        return (double) sum / cluster.size();
    }

    private double findDeviation(List<Integer> cluster) {
        long sum = 0;
        double avg = findAverage(cluster);
        for (Integer i : cluster) {

            sum += Math.pow(i - avg, 2);
        }
        return Math.sqrt(sum / cluster.size());
    }

    //this method finds SDBC
    private double sumOfSquaredDeviation(List<List<Integer>> listOfClusters) {
        double sum = 0;
        for (List<Integer> cluster : listOfClusters) {
            sum += Math.pow(findDeviation(cluster), 2);
        }
        return sum;
    }

    //this method finds SDAM
    private double sumOfSquaredDeviationFromMean(List<Integer> array) {
        return findDeviation(array);
    }

    public void PrintThemClusts(List<List<Integer>> clusts) {
        int i = 0;
        for (List<Integer> cluster : clusts) {
            System.out.println("\nCluster: " + i);
            for (Integer j : cluster)
                System.out.print(j + ", ");
            i++;
        }
    }
    //ENd of helper methods
}

