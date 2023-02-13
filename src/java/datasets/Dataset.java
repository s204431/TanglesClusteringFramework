package datasets;

import util.BitSet;

public interface Dataset {
    public BitSet[] getInitialCuts();

    public double[] getCutCosts();

    public int[] getGroundTruth();

}
