package testsets;

import datasets.*;
import util.BitSet;
import util.Tuple;

import java.util.Date;

import model.Model;
import smile.validation.metric.NormalizedMutualInformation;

import javax.swing.*;

public class ClusteringTester {

    //This class is used to run different tests on clustering algorithms.

    public static double testProgress = 0.0; //How far the current test run is (between 0 and 1).

    private static final Model model = new Model();

    //Used to test clustering with tangles on feature based data, binary questionnaires and graph data. Prints results to the console.
    public static void testTangleClusterer() {

        /*TestSet testSet = new TestSet(FeatureBasedDataset.name);
        for (int i = 100; i <= 1000; i *= 10) {
            for (int j = 2; j <= 10; j++) {
                testSet.add(new TestCase(i, 2, j, 1));
            }
        }
        runTest(testSet, new String[] {Model.tangleName, Model.kMeansName, Model.spectralClusteringName, Model.linkageName});*/

        System.out.println("Testing tangle clusterer...");
        long time1 = new Date().getTime();
        double[] result1 = testTangleClustererFeatureBased();
        System.out.println();
        double[] result2 = testTangleClustererBinaryQuestionnaire();
        System.out.println();
        double[] result3 = testTangleClustererGraph();
        long time2 = new Date().getTime();
        System.out.println();
        System.out.println("Feature based average NMI score: " + result1[0]/result1[1]);
        System.out.println("Questionnaire average NMI score: " + result2[0]/result2[1]);
        System.out.println("Graph average NMI score: " + result3[0]/result3[1]);
        System.out.println("Tests finished. Total time: " + (time2 - time1) + " ms. Average NMI score: " + (result1[0]+result2[0]+result3[0])/(result1[1]+result2[1]+result3[1]));
    }

    //Used to test clustering with tangles on feature based data. Prints results to the console.
    //Returns sum of NMI scores and total datasets tested with NMI score.
    public static double[] testTangleClustererFeatureBased() {
        long totalTime1 = new Date().getTime();
        int totalCountWithoutNaN = 0;
        double NMISum = 0;
        for (int i = 1000; i <= 1000000; i *= 10) {
            long sizeTime1 = new Date().getTime();
            int sizeCount = 0;
            for (int j = 1; j <= 10; j++) {
                sizeCount++;
                long time1 = new Date().getTime();
                int a = (int)((i/j)*(2.0/3.0));
                Tuple<double[][], int[]> generated = DatasetGenerator.generateFeatureBasedDataPoints(i, j, 2);
                Dataset dataset = new FeatureBasedDataset(generated.x);
                int[] groundTruth = generated.y;
                model.setDataset(dataset);
                model.generateClusters(a, -1, null, null);
                model.getSoftClustering();
                int[] hardClustering = model.getHardClustering();
                long time2 = new Date().getTime();
                double nmiScore = NormalizedMutualInformation.joint(hardClustering, groundTruth);
                if (!Double.isNaN(nmiScore)) {
                    totalCountWithoutNaN++;
                    NMISum += nmiScore;
                }
                System.out.println("Feature based test with " + i + " datapoints and " + j + " clusters took " + (time2 - time1) + " ms. NMI score: " + nmiScore);
            }
            long sizeTime2 = new Date().getTime();
            System.out.println("Feature based tests with " + i + " datapoints finished. Total time: " + (sizeTime2 - sizeTime1) + " ms. Average time: " + (sizeTime2-sizeTime1)/sizeCount + " ms.");
        }
        long totalTime2 = new Date().getTime();
        System.out.println("Feature based tests finished in " + (totalTime2-totalTime1) + " ms. Average NMI score: " + NMISum/totalCountWithoutNaN);
        return new double[] {NMISum, totalCountWithoutNaN};
    }

    //Used to test clustering with tangles on binary questionnaires. Prints results to the console.
    //Returns sum of NMI scores and total datasets tested with NMI score.
    public static double[] testTangleClustererBinaryQuestionnaire() {
        long totalTime1 = new Date().getTime();
        int totalCountWithoutNaN = 0;
        double NMISum = 0;
        for (int i = 1000; i <= 1000000; i *= 10) {
            long sizeTime1 = new Date().getTime();
            int sizeCount = 0;
            for (int j = 10; j < 41; j+=5) {
                for (int k = 1; k <= 6; k++) {
                    sizeCount++;
                    long time1 = new Date().getTime();
                    int a = (int)((i/k)*(1.0/2.0));
                    Tuple<BitSet[], int[]> generated = DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(i, j, k);
                    Dataset dataset = new BinaryQuestionnaire(generated.x);
                    int[] groundTruth = generated.y;
                    model.setDataset(dataset);
                    model.generateClusters(a, -1, null, null);
                    model.getSoftClustering();
                    int[] hardClustering = model.getHardClustering();
                    long time2 = new Date().getTime();
                    double nmiScore = NormalizedMutualInformation.joint(hardClustering, groundTruth);
                    if (!Double.isNaN(nmiScore)) {
                        totalCountWithoutNaN++;
                        NMISum += nmiScore;
                    }
                    System.out.println("Questionnaire test with " + i + " datapoints, " + j + " questions and " + k + " clusters took " + (time2 - time1) + " ms. NMI score: " + nmiScore);
                }
            }
            long sizeTime2 = new Date().getTime();
            System.out.println("Questionnaire tests with " + i + " datapoints finished. Total time: " + (sizeTime2 - sizeTime1) + " ms. Average time: " + (sizeTime2-sizeTime1)/sizeCount + " ms.");
        }
        long totalTime2 = new Date().getTime();
        System.out.println("Questionnaire tests finished in " + (totalTime2-totalTime1) + " ms. Average NMI score: " + NMISum/totalCountWithoutNaN);
        return new double[] {NMISum, totalCountWithoutNaN};
    }

    //Used to test clustering with tangles on graph data. Prints results to the console.
    //Returns sum of NMI scores and total datasets tested with NMI score.
    public static double[] testTangleClustererGraph() {
        long totalTime1 = new Date().getTime();
        int totalCountWithoutNaN = 0;
        double NMISum = 0;
        for (int i = 20; i <= 500; i *= 2) {
            long sizeTime1 = new Date().getTime();
            int sizeCount = 0;
            for (int j = 2; j <= 6; j++) {
                sizeCount++;
                long time1 = new Date().getTime();
                int a = (int)((i/j)*(1.0/2.0));
                Tuple<int[][][], int[]> generated = DatasetGenerator.generateRandomGraph(i, j);
                Dataset dataset = new GraphDataset(generated.x);
                int[] groundTruth = generated.y;
                model.setDataset(dataset);
                model.generateClusters(a, -1, null, null);
                model.getSoftClustering();
                int[] hardClustering = model.getHardClustering();
                long time2 = new Date().getTime();
                double nmiScore = NormalizedMutualInformation.joint(hardClustering, groundTruth);
                if (!Double.isNaN(nmiScore)) {
                    totalCountWithoutNaN++;
                    NMISum += nmiScore;
                }
                System.out.println("Graph test with " + i + " datapoints and " + j + " clusters took " + (time2 - time1) + " ms. NMI score: " + nmiScore);
            }
            long sizeTime2 = new Date().getTime();
            System.out.println("Graph tests with " + i + " datapoints finished. Total time: " + (sizeTime2 - sizeTime1) + " ms. Average time: " + (sizeTime2-sizeTime1)/sizeCount + " ms.");
        }
        long totalTime2 = new Date().getTime();
        System.out.println("Graph tests finished in " + (totalTime2-totalTime1) + " ms. Average NMI score: " + NMISum/totalCountWithoutNaN);
        return new double[] {NMISum, totalCountWithoutNaN};
    }

    //Runs a test set on specific algorithms. Returns {Time, NMI score} for each test case.
    //Optionally takes a JPanel to be repainted when progress is made (simply use null if there is no panel to be repainted).
    public static double[][][] runTest(TestSet testSet, String[] algorithmNames, JPanel repaintPanel) {
        double[][][] result = new double[algorithmNames.length][testSet.size()][2];
        System.out.print("Running tests for ");
        for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
            if (algorithm == 0) {
                System.out.print(algorithmNames[algorithm]);
            }
            else if (algorithm == algorithmNames.length-1) {
                System.out.print(" and " + algorithmNames[algorithm]);
            }
            else {
                System.out.print(", " + algorithmNames[algorithm]);
            }
        }
        System.out.println(" on " + testSet.dataTypeName);
        for (int i = 0; i < testSet.size(); i++) {
            testProgress = i/(double)testSet.size();
            if (repaintPanel != null) {
                repaintPanel.repaint();
            }
            TestCase testCase = testSet.get(i);
            long[] testCaseTimes = new long[algorithmNames.length];
            double[] testCaseNMIScores = new double[algorithmNames.length];
            for (int j = 0; j < testCase.nRuns; j++) {
                Dataset dataset = switch (testSet.dataTypeName) {
                    case FeatureBasedDataset.name -> new FeatureBasedDataset(DatasetGenerator.generateFeatureBasedDataPoints(testCase.nPoints, testCase.nClusters, testCase.nDimensions));
                    case BinaryQuestionnaire.name -> new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(testCase.nPoints, testCase.nDimensions, testCase.nClusters));
                    case GraphDataset.name -> new GraphDataset(DatasetGenerator.generateRandomGraph(testCase.nPoints, testCase.nClusters));
                    default -> null;
                };
                int[] hardClustering = null;
                for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
                    String algorithmName = algorithmNames[algorithm];
                    long time1 = new Date().getTime();
                    switch (algorithmName) {
                        case Model.tangleName -> {
                            int a = (int) ((testCase.nPoints / testCase.nClusters) * (1.0 / 2.0));
                            hardClustering = model.generateClusters(dataset, a, -1, null, null);
                        }
                        case Model.kMeansName -> hardClustering = dataset.kMeans(testCase.nClusters);
                        case Model.spectralClusteringName -> hardClustering = dataset.spectralClustering(testCase.nClusters, testCase.nPoints / 10.0);
                        case Model.linkageName -> hardClustering = dataset.hierarchicalClustering(testCase.nClusters);
                    }
                    testCaseTimes[algorithm] += new Date().getTime() - time1;
                    testCaseNMIScores[algorithm] += NormalizedMutualInformation.joint(hardClustering, dataset.getGroundTruth());
                }
            }
            for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
                result[algorithm][i] = new double[] {testCaseTimes[algorithm]/testCase.nRuns, testCaseNMIScores[algorithm]/testCase.nRuns};
                System.out.println(algorithmNames[algorithm]+" test on " + testSet.dataTypeName + " with " + + testCase.nRuns + " runs, " + testCase.nPoints + " datapoints, " + testCase.nDimensions + " dimensions and " + testCase.nClusters + " clusters took " + ((long)result[algorithm][i][0]) + " ms on average. Average NMI score: " + ((int)(result[algorithm][i][1]*10000))/10000.0);
            }
        }
        for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
            double totalNMI = 0;
            long totalTime = 0;
            int notNaNCount = 0;
            for (int i = 0; i < result[algorithm].length; i++) {
                totalTime += result[algorithm][i][0];
                if (!Double.isNaN(result[algorithm][i][1])) {
                    totalNMI += result[algorithm][i][1];
                    notNaNCount++;
                }
            }
            System.out.println("Tests for " + algorithmNames[algorithm] + " on " + testSet.dataTypeName + " finished in " + totalTime + " ms. Average NMI score: " + totalNMI/notNaNCount);
        }
        return result;
    }

}
