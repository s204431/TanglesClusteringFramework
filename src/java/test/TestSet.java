package test;

import java.util.ArrayList;
import java.util.List;

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

}
