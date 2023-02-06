package main;

public class FeatureBasedDataset implements Dataset {
    @Override
    public BitSet[] getInitialCuts() {
        return new BitSet[0];
    }

    @Override
    public int[] getCutCosts() {
        return new int[0];
    }
}
