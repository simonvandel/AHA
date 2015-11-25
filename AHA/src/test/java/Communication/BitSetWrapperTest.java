package Communication;


import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.generator.InRange;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class BitSetWrapperTest {

    private BitSetWrapper makeBitSetWrapper(byte[] bytes) {
        BitSet bitSet = BitSet.valueOf(bytes);
        return new BitSetWrapper(bitSet);
    }

    @Test
    public void getBitsFromInt() {
        byte[] bytes = new byte[]{0b01011111};
        BitSetWrapper bitSetWrapper = makeBitSetWrapper(bytes);
        byte res1 = bitSetWrapper.getByteFromBits(4);
        byte res2 = bitSetWrapper.getByteFromBits(1);
        byte res3 = bitSetWrapper.getByteFromBits(2);
        assertEquals(0b1111, res1);
        assertEquals(0b1, res2);
        assertEquals(0b10, res3);
    }

    @Theory
    public void intParsedShouldGiveSameInt(@ForAll int n) throws Exception {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(n).array();

        int result = makeBitSetWrapper(bytes).getIntFromBits(32);
        assertEquals(n, result);
    }

    @Theory
    public void longParsedShouldGiveSameLong(@ForAll long n) throws Exception {
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(n).array();

        long result = makeBitSetWrapper(bytes).getLongFromBits(64);
        assertEquals(n, result);
    }

    @Theory
    public void byteParsedShouldGiveSameByte(@ForAll byte n) throws Exception {
        byte[] bytes = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put(n).array();

        byte result = makeBitSetWrapper(bytes).getByteFromBits(8);
        assertEquals(n, result);
    }

    @Theory
    public void getBitsReturnedBitSetHasLengthOfInput(@ForAll @InRange(min = "0", max = "32") int n) throws Exception {
        byte[] bytes = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        BitSet bitSet = makeBitSetWrapper(bytes).getBits(n);
        assertEquals(n, bitSet.length());
    }
}