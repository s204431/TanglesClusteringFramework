package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSet {

    //This class represents a test set (a number of test cases and a data type).

    public final String dataTypeName; //Type of data for the test set.
    private final List<TestCase> testCases = new ArrayList<>(); //Test cases for the test set.

    //Constructor used to create an empty test set with a specific data type.
    public TestSet(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    //Returns the number of test cases.
    public int size() {
        return testCases.size();
    }

    //Adds a test case to the test set.
    public void add(TestCase testCase) {
        testCases.add(testCase);
    }

    //Returns the test case at a specific index.
    public TestCase get(int index) {
        return testCases.get(index);
    }

    //Stores the test set in a file.
    public void saveTestSet(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(dataTypeName + "\n");
            for (int i = 0; i < size(); i++) {
                TestCase testCase = get(i);
                writer.write(""+testCase.nPoints);
                writer.write(" "+testCase.nDimensions);
                writer.write(" "+testCase.nClusters);
                writer.write(" "+testCase.nRuns);
                if (i != size()-1) {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException ignored) {} //Do nothing if saving failed.
    }

    //Creates a new test set loaded from a file.
    public static TestSet loadTestSet(File file) {
        try {
            Scanner scanner = new Scanner(file);
            TestSet testSet = new TestSet(scanner.nextLine());
            while (scanner.hasNextInt()) {
                testSet.add(new TestCase(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
            }
            return testSet;
        } catch (Exception ignored) {}
        return null; //Return null if loading failed.
    }

}
