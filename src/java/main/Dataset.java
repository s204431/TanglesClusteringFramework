package main;

public interface Dataset {
    public BitSet[] getInitialCuts();

    public int[] getCutCosts();

    public int[] getCutCosts2();
}
