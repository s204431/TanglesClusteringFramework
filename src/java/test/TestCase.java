package test;

public class TestCase {

    public int nPoints;
    public int nDimensions;
    public int nClusters;
    public int nRuns = 1;

    public TestCase(int numberOfPoints, int numberOfDimension, int numberOfClusters) {
        nPoints = numberOfPoints;
        nDimensions = numberOfDimension;
        nClusters = numberOfClusters;
    }

    public TestCase(int numberOfPoints, int numberOfDimension, int numberOfClusters, int numberOfRuns) {
        this(numberOfPoints, numberOfDimension, numberOfClusters);
        nRuns = numberOfRuns;
    }

}
