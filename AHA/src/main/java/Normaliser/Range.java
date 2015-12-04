package Normaliser;

/**
 * Created by brugeren on 03-12-2015.
 */
public class Range {
    int lowerBound = 0;
    int upperBound = 0;

    public Range(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean fits(int i) {
        return (lowerBound < i) && (upperBound > i);
    }
}
