package DataAggregator;

import java.util.BitSet;

/**
 * Created by simon on 12/11/2015.
 */
public class BitSetWrapper {
    private int currentBit = 0;
    private BitSet bitSet;

    public BitSetWrapper(BitSet bitSet) {
        this.bitSet = bitSet;
    }


    /**
     * @param n The number of bits to retrieve
     * @return A bitset containing the n bits
     */
    public BitSet getBits(int n) {
        BitSet tempBs = bitSet.get(currentBit,n+1);
        currentBit = n;
        return tempBs;
    }

    /**
     * @param n The number of bits to retrieve
     * @return The decimal representation of the n bits retrieved
     */
    public int getIntFromBits(int n) {
        BitSet tempBs = getBits(n);
        return bitSetToInt(tempBs);
    }

    /**
     * @param bitSet
     * @return Converts a bitset to its decimal representation
     */
    private int bitSetToInt(BitSet bitSet) {
        int bitInteger = 0;
        for(int i = 0 ; i < bitSet.size(); i++)
            if(bitSet.get(i))
                bitInteger |= (1 << i);
        return bitInteger;
    }
}
