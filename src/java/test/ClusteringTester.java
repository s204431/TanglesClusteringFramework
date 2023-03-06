package test;

import datasets.*;
import model.TangleClusterer;
import util.BitSet;
import util.Tuple;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import model.Model;
import smile.validation.metric.NormalizedMutualInformation;

public class ClusteringTester {
    private static Model model = new Model();

    public static void testTangleClusterer() {

        TestSet testSet = new TestSet(GraphDataset.name);
        for (int i = 20; i <= 250; i *= 2) {
            for (int j = 2; j <= 10; j++) {
                testSet.add(new TestCase(i, 0, j, 1));
            }
        }
        runTest(testSet, new String[] {Model.tangleName});

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

    public static double[] testTangleClustererFeatureBased() {
        long totalTime1 = new Date().getTime();
        int totalCount = 0;
        int totalCountWithoutNaN = 0;
        double NMISum = 0;
        for (int i = 1000; i <= 1000000; i *= 10) {
            totalCount++;
            long sizeTime1 = new Date().getTime();
            int sizeCount = 0;
            for (int j = 1; j <= 10; j++) {
                sizeCount++;
                long time1 = new Date().getTime();
                int a = (int)((i/j)*(1.0/2.0));
                Tuple<double[][], int[]> generated = DatasetGenerator.generateFeatureBasedDataPoints(i, j, 2);
                Dataset dataset = new FeatureBasedDataset(generated.x);
                int[] groundTruth = generated.y;
                model.setDataset(dataset);
                model.generateClusters(a, -1);
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

    //Returns sum of NMI scores and total datasets tested with NMI score.
    public static double[] testTangleClustererBinaryQuestionnaire() {
        long totalTime1 = new Date().getTime();
        int totalCount = 0;
        int totalCountWithoutNaN = 0;
        double NMISum = 0;
        for (int i = 1000; i <= 1000000; i *= 10) {
            totalCount++;
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
                    model.generateClusters(a, -1);
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

    public static double[] testTangleClustererGraph() {
        long totalTime1 = new Date().getTime();
        int totalCount = 0;
        int totalCountWithoutNaN = 0;
        double NMISum = 0;
        for (int i = 20; i <= 500; i *= 2) {
            totalCount++;
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
                model.generateClusters(a, -1);
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

    //Runs a test set on a specific algorithm. Returns {Time, NMI score} for each test case.
    public static double[][][] runTest(TestSet testSet, String[] algorithmNames) {
        double[][][] result = new double[algorithmNames.length][testSet.size()][2];
        System.out.println("Running tests for " + algorithmNames[0] + " on " + testSet.dataTypeName);
        long totalTime = new Date().getTime();
        for (int i = 0; i < testSet.size(); i++) {
            TestCase testCase = testSet.get(i);
            long[] testCaseTimes = new long[algorithmNames.length];
            double[] testCaseNMIScores = new double[algorithmNames.length];
            for (int j = 0; j < testCase.nRuns; j++) {
                Dataset dataset = null;
                if (testSet.dataTypeName.equals(FeatureBasedDataset.name)) {
                    dataset = new FeatureBasedDataset(DatasetGenerator.generateFeatureBasedDataPoints(testCase.nPoints, testCase.nClusters, testCase.nDimensions));
                }
                else if (testSet.dataTypeName.equals(BinaryQuestionnaire.name)) {
                    dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(testCase.nPoints, testCase.nDimensions, testCase.nClusters));
                }
                else if (testSet.dataTypeName.equals(GraphDataset.name)) {
                    dataset = new GraphDataset(DatasetGenerator.generateRandomGraph(testCase.nPoints, testCase.nClusters));
                }
                int[] hardClustering = null;
                for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
                    String algorithmName = algorithmNames[algorithm];
                    long time1 = new Date().getTime();
                    if (algorithmName.equals(Model.tangleName)) {
                        int a = (int)((testCase.nPoints/testCase.nClusters)*(1.0/2.0));
                        hardClustering = model.generateClusters(dataset, a, -1);
                    }
                    else if (algorithmName.equals(Model.kMeansName)) {
                        hardClustering = dataset.kMeans(testCase.nClusters);
                    }
                    else if (algorithmName.equals(Model.spectralClusteringName)) {
                        hardClustering = dataset.spectralClustering(testCase.nClusters, testCase.nPoints/10.0);
                    }
                    else if (algorithmName.equals(Model.linkageName)) {
                        hardClustering = dataset.hierarchicalClustering(testCase.nClusters);
                    }
                    testCaseTimes[algorithm] += new Date().getTime() - time1;
                    testCaseNMIScores[algorithm] += NormalizedMutualInformation.joint(hardClustering, dataset.getGroundTruth());
                }
            }
            for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
                result[algorithm][i] = new double[] {testCaseTimes[algorithm]/testCase.nRuns, testCaseNMIScores[algorithm]/testCase.nRuns};
            }
            System.out.println(testSet.dataTypeName + " test with " + + testCase.nRuns + " runs, " + testCase.nPoints + " datapoints, " + testCase.nDimensions + " dimensions and " + testCase.nClusters + " clusters took " + ((long)result[0][i][0]) + " ms on average. Average NMI score: " + ((int)(result[0][i][1]*10000))/10000.0);
        }
        double totalNMI = 0;
        int notNaNCount = 0;
        for (int i = 0; i < result[0].length; i++) {
            if (!Double.isNaN(result[0][i][1])) {
                totalNMI += result[0][i][1];
                notNaNCount++;
            }
        }
        System.out.println("Tests for " + algorithmNames[0] + " on " + testSet.dataTypeName + " finished in " + (new Date().getTime() - totalTime) + " ms. Average NMI score: " + totalNMI/notNaNCount);
        return result;
    }

}
