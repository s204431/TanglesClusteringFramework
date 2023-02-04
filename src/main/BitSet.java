package main;

//Custom BitSet implementation with fast intersection.
public class BitSet {
    protected long[] set;
    private int size = 0;
    public int cutCost;

    //Creates BitSet with specified maximum size.
    public BitSet(int maxSize) {
        set = new long[(maxSize-1)/64+1];
        size = maxSize;
    }

    //Adds participant with specific index to set (constant time complexity).
    public void add(int index) {
        int longIndex = index >> 6; //index / 64
        index = index & 63; //index % 64
        set[longIndex] = set[longIndex] | (1L << index);
    }

    //Removes participant with specific index to set (constant time complexity).
    public void remove(int index) {
        int longIndex = index >> 6; //index / 64
        index = index & 63; //index % 64
        set[longIndex] = set[longIndex] & ~(1L << index);
    }

    public void flip(int index) {
        if (get(index)) {
            remove(index);
        }
        else {
            add(index);
        }
    }

    public void setValue(int index, boolean value) {
        if (value) {
            add(index);
        }
        else {
            remove(index);
        }
    }

    //Returns true if given index is part of the set.
    public boolean get(int index) {
        int longIndex = index >> 6; //index / 64
        index = index & 63; //index % 64
        return (set[longIndex] & (1L << index)) != 0;
    }

    public int size() {
        return size;
    }

    //Returns the amount of participants that are part of the cut.
    public int count() {
        int count = 0;
        for (long l : set) {
            count += Long.bitCount(l);
        }
        return count;
    }

    //Calculates the similarity of two BitSets using XNor.
    public static int XNor(BitSet set1, BitSet set2) {
        int count = 0;
        for (int i = 0; i < set1.set.length; i++) {
            count += Long.bitCount((~set1.set[i]) ^ set2.set[i]);
            if (i == set1.set.length-1) { //Last part of last long is not part of the set.
                int bitsInLastLong = set1.size() % 64;
                if (bitsInLastLong > 0) {
                    count -= 64 - bitsInLastLong;
                }
            }
        }
        return count;
    }

    //Unions this bitset with otherSet.
    public void unionWith(BitSet otherSet) {
        for (int i = 0; i < set.length; i++) {
            set[i] = set[i] | otherSet.set[i];
        }
    }

    //Returns the size of the intersection between two bitsets.
    //Requires same maximum size. partOfSet specifies if false or true means part of the set.
    public static int intersection(BitSet set1, BitSet set2, boolean partOfSet1, boolean partOfSet2) {
        return intersectionEarlyStop(set1, set2, partOfSet1, partOfSet2, Integer.MAX_VALUE);
    }

    //Returns the size of the intersection between three bitsets.
    public static int intersection(BitSet set1, BitSet set2, BitSet set3, boolean partOfSet1, boolean partOfSet2, boolean partOfSet3) {
        return intersectionEarlyStop(set1, set2, set3, partOfSet1, partOfSet2, partOfSet3, Integer.MAX_VALUE);
    }

    //Intersection that stops when greater than a.
    public static int intersectionEarlyStop(BitSet set1, BitSet set2, boolean partOfSet1, boolean partOfSet2, int a) {
        int count = 0;
        for (int i = 0; i < set1.set.length; i++) {
            int amountFlipped = 0;
            long long1 = set1.set[i];
            long long2 = set2.set[i];
            if (!partOfSet1) {
                long1 = ~long1;
                amountFlipped++;
            }
            if (!partOfSet2) {
                long2 = ~long2;
                amountFlipped++;
            }
            count += Long.bitCount(long1 & long2);
            if (i == set1.set.length-1 && amountFlipped == 2) { //Last part of last long is not part of the set.
                int bitsInLastLong = set1.size() % 64;
                if (bitsInLastLong > 0) {
                    count -= 64 - bitsInLastLong;
                }
            }
            if (count >= a) {
                return count;
            }
        }
        return count;
    }


    public static int intersectionEarlyStop(BitSet set1, BitSet set2, BitSet set3, boolean partOfSet1, boolean partOfSet2, boolean partOfSet3, int a) {
        int count = 0;
        for (int i = 0; i < set1.set.length; i++) {
            int amountFlipped = 0;
            long long1 = set1.set[i];
            long long2 = set2.set[i];
            long long3 = set3.set[i];
            if (!partOfSet1) {
                long1 = ~long1;
                amountFlipped++;
            }
            if (!partOfSet2) {
                long2 = ~long2;
                amountFlipped++;
            }
            if (!partOfSet3) {
                long3 = ~long3;
                amountFlipped++;
            }
            count += Long.bitCount(long1 & long2 & long3);
            if (i == set1.set.length-1 && amountFlipped == 3) { //Last part of last long is not part of the set.
                int bitsInLastLong = set1.size() % 64;
                if (bitsInLastLong > 0) {
                    count -= 64 - bitsInLastLong;
                }
            }
            if (count >= a) {
                return count;
            }
        }
        return count;
    }

    public void print() {
        System.out.println(size + " " + set.length);
        for (long l : set) {
            for (int i = 0; i < Long.numberOfLeadingZeros(l); i++) {
                System.out.print("0");
            }
            System.out.print(Long.toBinaryString(l));
        }
        System.out.println();
    }

}
