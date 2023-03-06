package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSet {
    public String dataTypeName;
    private List<TestCase> testCases = new ArrayList<>();

    public TestSet(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public int size() {
        return testCases.size();
    }

    public void add(TestCase testCase) {
        testCases.add(testCase);
    }

    public TestCase get(int index) {
        return testCases.get(index);
    }

    //Store a test set in a file.
    public static void saveTestSet(TestSet testSet, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(testSet.dataTypeName + "\n");
            for (int i = 0; i < testSet.size(); i++) {
                TestCase testCase = testSet.get(i);
                writer.write(""+testCase.nPoints);
                writer.write(" "+testCase.nDimensions);
                writer.write(" "+testCase.nClusters);
                writer.write(" "+testCase.nRuns);
                if (i != testSet.size()-1) {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException e) {}
    }

    //Load a test set from a file.
    public static TestSet loadTestSet(File file) {
        try {
            Scanner scanner = new Scanner(file);
            TestSet testSet = new TestSet(scanner.nextLine());
            while (scanner.hasNextInt()) {
                testSet.add(new TestCase(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
            }
            return testSet;
        } catch (Exception e) {}
        return null; //Return null if loading failed.
    }

}
