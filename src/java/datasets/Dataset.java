package datasets;

import util.BitSet;

public interface Dataset {
    public BitSet[] getInitialCuts();

    public double[] getCutCosts();

    public int[] getGroundTruth();
    public void setA(int a);
    public int[] kMeans(int clusters);
    public String getName();
    public String[] getSupportedAlgorithms();
    public boolean supportsAlgorithm(String algorithmName);

}
