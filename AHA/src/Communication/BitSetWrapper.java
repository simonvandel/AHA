package Communication;

import java.util.BitSet;

/**
 * Created by simon on 12/11/2015.
 */
public class BitSetWrapper {
    private int currentBitIndex = 0;
    private BitSet bitSet;

    /**
     * @param bitSet bitSet in little endian order
     */
    public BitSetWrapper(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    /**
     * @param n The number of bits to retrieve
     * @return A bitset containing the n bits
     */
    public BitSet getBits(int n) {
        // get the bits from currentBitIndex and n bits forward (exclusive)
        BitSet tempBs = bitSet.get(currentBitIndex, currentBitIndex + n);
        // our new index is n bits forward
        currentBitIndex += n;
        return tempBs;
    }

    /**
     * @param n The number of bits to retrieve
     * @return The decimal representation of the n bits retrieved as a long
     */
    public long getLongFromBits(int n) {
        BitSet tempBs = getBits(n);
        return bitSetToLong(tempBs);
    }

    /**
     * @param n The number of bits to retrieve
     * @return The decimal representation of the n bits retrieved as an int
     */
    public int getIntFromBits(int n) {
        BitSet tempBs = getBits(n);
        return bitSetToInt(tempBs);
    }

    /**
     * @param n The number of bits to retrieve
     * @return The decimal representation of the n bits retrieved as a byte
     */
    public byte getByteFromBits(int n) {
        BitSet tempBs = getBits(n);
        return bitSetToByte(tempBs);
    }

    /**
     * @param bitSet the bitSet to convert
     * @return Converts a bitset to its decimal representation as an int
     */
    private int bitSetToInt(BitSet bitSet) {
        int bitInteger = 0;
        for(int i = 0 ; i < bitSet.size(); i++)
            if(bitSet.get(i))
                bitInteger |= (1 << i);
        return bitInteger;
    }

    /**
     * @param bitSet the bitSet to convert
     * @return Converts a bitset to its decimal representation as a byte
     */
    private byte bitSetToByte(BitSet bitSet) {
        byte bitByte = 0;
        for (int i = 0; i < bitSet.size(); i++)
            if (bitSet.get(i))
                bitByte |= (1 << i);
        return bitByte;
    }

    /**
     * @param bitSet the bitSet to convert
     * @return Converts a bitset to its decimal representation as a long
     */
    private long bitSetToLong(BitSet bitSet) {
        long bitLong = 0;
        for (int i = 0; i < bitSet.size(); i++)
            if (bitSet.get(i))
                bitLong |= (1L << i);
        return bitLong;
    }
}
