package testsets;

public class TestCase {

    //This class represents a specific test case.

    public int nPoints; //Number of points for the test case.
    public int nDimensions; //Number of dimensions for the test case.
    public int nClusters; //Number of clusters for the test case.
    public int nRuns = 1; //Number of runs of the test case. Results are averaged over all runs.

    //Constructor without specifying the number of runs. nRuns is set to 1 by default.
    public TestCase(int numberOfPoints, int numberOfDimension, int numberOfClusters) {
        nPoints = numberOfPoints;
        nDimensions = numberOfDimension;
        nClusters = numberOfClusters;
    }

    //Constructor taking all values for the test case.
    public TestCase(int numberOfPoints, int numberOfDimension, int numberOfClusters, int numberOfRuns) {
        this(numberOfPoints, numberOfDimension, numberOfClusters);
        nRuns = numberOfRuns;
    }

    //Checks if this object is equal to the given object.
    @Override
    public boolean equals(Object o) {
        if (o instanceof TestCase) {
            TestCase t = (TestCase) o;
            return t.nClusters == nClusters && t.nDimensions == nDimensions && t.nPoints == nPoints && t.nRuns == nRuns;
        }
        return false;
    }

}
