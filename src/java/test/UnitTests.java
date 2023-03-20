package test;

import org.junit.jupiter.api.Test;
import util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTests {

    //Unit test for the BitSet class.
    @Test
    public void testBitSet() {
        String testBitString = "01010010110101111000111010001"+"1".repeat(60);

        //Tests of Constructors, toString, size, count, countFlipped.
        assertEquals(testBitString, new BitSet(testBitString).toString());

        assertEquals("0".repeat(50), new BitSet(50).toString());

        assertEquals(testBitString.length(), new BitSet(testBitString).size());

        assertEquals(testBitString.chars().filter(ch -> ch == '1').count(), new BitSet(testBitString).count());

        assertEquals(testBitString.chars().filter(ch -> ch == '1').count(), new BitSet(testBitString).countFlipped(false));
        assertEquals(testBitString.chars().filter(ch -> ch == '0').count(), new BitSet(testBitString).countFlipped(true));

        //Tests of add.
        BitSet bitSet = new BitSet(testBitString);
        bitSet.add(4);
        assertEquals(setCharAt(4, '1', testBitString), bitSet.toString());

        bitSet = new BitSet(testBitString);
        bitSet.add(3);
        assertEquals(testBitString, bitSet.toString());

        //Tests of remove.
        bitSet = new BitSet(testBitString);
        bitSet.remove(3);
        assertEquals(setCharAt(3, '0', testBitString), bitSet.toString());

        bitSet = new BitSet(testBitString);
        bitSet.remove(4);
        assertEquals(testBitString, bitSet.toString());

        //Test of setAll.
        bitSet = new BitSet(testBitString);
        bitSet.setAll();
        assertEquals("1".repeat(testBitString.length()), bitSet.toString());

        //Tests of flip.
        bitSet = new BitSet(testBitString);
        bitSet.flip(0);
        assertEquals(setCharAt(0, '1', testBitString), bitSet.toString());

        bitSet = new BitSet(testBitString);
        bitSet.flip(1);
        assertEquals(setCharAt(1, '0', testBitString), bitSet.toString());

        //Tests of setValue.
        for (int i = 0; i < 2; i++) {
            bitSet = new BitSet(testBitString);
            bitSet.setValue(7, i != 0);
            assertEquals(setCharAt(7, Character.forDigit(i, 10), testBitString), bitSet.toString());

            bitSet = new BitSet(testBitString);
            bitSet.setValue(6,i != 0);
            assertEquals(setCharAt(6, Character.forDigit(i, 10), testBitString), bitSet.toString());
        }

        //Tests of get.
        bitSet = new BitSet(testBitString);
        assertEquals(testBitString.charAt(2) != '0', bitSet.get(2));
        assertEquals(testBitString.charAt(3) != '0', bitSet.get(3));

        String otherTestBitString = "11001001110011000010101110000"+"0".repeat(60);

        //Test of XNor.
        assertEquals(15, BitSet.XNor(new BitSet(testBitString), new BitSet(otherTestBitString)));

        //Test of unionWith.
        bitSet = new BitSet(testBitString);
        bitSet.unionWith(new BitSet(otherTestBitString));
        assertEquals("11011011110111111010111110001"+"1".repeat(60), bitSet.toString());

        //Tests of intersection.
        assertEquals(7, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), false, false));
        assertEquals(6, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), true, false));
        assertEquals(68, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), false, true));
        assertEquals(8, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), true, true));

        String thirdTestBitString = "00110100111001010110001100111"+"0".repeat(60);

        assertEquals(4, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), false, false, false));
        assertEquals(2, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), true, false, false));
        assertEquals(3, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), false, true, false));
        assertEquals(6, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), true, true, false));
        assertEquals(3, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), false, false, true));
        assertEquals(4, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), true, false, true));
        assertEquals(65, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), false, true, true));
        assertEquals(2, BitSet.intersection(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), true, true, true));

        //Tests of intersectionEarlyStop.
        assertEquals(43, BitSet.intersectionEarlyStop(new BitSet(testBitString), new BitSet(otherTestBitString), false, true, 5));
        assertEquals(68, BitSet.intersectionEarlyStop(new BitSet(testBitString), new BitSet(otherTestBitString), false, true, 50));

        assertEquals(40, BitSet.intersectionEarlyStop(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), false, true, true, 15));
        assertEquals(65, BitSet.intersectionEarlyStop(new BitSet(testBitString), new BitSet(otherTestBitString), new BitSet(thirdTestBitString), false, true, true, 55));

    }

    //Sets the char at the specified index of string.
    private String setCharAt(int index, char ch, String string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        stringBuilder.setCharAt(index, ch);
        return stringBuilder.toString();
    }
}
