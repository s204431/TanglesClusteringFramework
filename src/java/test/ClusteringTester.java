package test;

import main.*;
import main.Util.Tuple;

import java.util.Date;
import smile.validation.metric.NormalizedMutualInformation;

public class ClusteringTester {

    //private int[][] randomFeatureBasedTestSet = new int[][] {{1000, }}; //{nDataPoints, nClusters}

    public static void testTangleClusterer() {
        System.out.println("Testing tangle clusterer...");
        long time1 = new Date().getTime();
        testTangleClustererFeatureBased();
        System.out.println();
        testTangleClustererBinaryQuestionnaire();
        long time2 = new Date().getTime();
        System.out.println("Tests finished. Total time: " + (time2 - time1) + " ms.");
    }

    public static void testTangleClustererFeatureBased() {
        long totalTime1 = new Date().getTime();
        int totalCount = 0;
        for (int i = 1000; i <= 1000000; i *= 10) {
            totalCount++;
            long sizeTime1 = new Date().getTime();
            int sizeCount = 0;
            for (int j = 1; j <= 10; j++) {
                sizeCount++;
                long time1 = new Date().getTime();
                int a = (int)((i/j)*(2.0/3.0));
                Tuple<double[][], int[]> generated = DatasetGenerator.generateGaussianMixturePoints(i, j);
                Dataset dataset = new FeatureBasedDataset(generated.x, a);
                int[] groundTruth = generated.y;
                TangleClusterer.generateClusters(dataset, a, -1);
                TangleClusterer.getSoftClustering();
                int[] hardClustering = TangleClusterer.getHardClustering();
                long time2 = new Date().getTime();
                double nmiScore = NormalizedMutualInformation.joint(hardClustering, groundTruth);
                System.out.println("Feature based test with " + i + " datapoints and " + j + " clusters took " + (time2 - time1) + " ms. NMI score: " + nmiScore + ".");
            }
            long sizeTime2 = new Date().getTime();
            System.out.println("Feature based tests with " + i + " datapoints finished. Total time: " + (sizeTime2 - sizeTime1) + " ms. Average time: " + (sizeTime2-sizeTime1)/sizeCount + " ms.");
        }
        long totalTime2 = new Date().getTime();
        System.out.println("Feature based tests finished in " + (totalTime2-totalTime1) + " ms.");
    }

    public static void testTangleClustererBinaryQuestionnaire() {
        long totalTime1 = new Date().getTime();
        int totalCount = 0;
        for (int i = 1000; i <= 1000000; i *= 10) {
            totalCount++;
            long sizeTime1 = new Date().getTime();
            int sizeCount = 0;
            for (int j = 1; j < 42; j+=5) {
                for (int k = 1; k <= 6; k++) {
                    sizeCount++;
                    long time1 = new Date().getTime();
                    int a = (int)((i/k)*(2.0/3.0));
                    Dataset dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(i, j, k).x);
                    TangleClusterer.generateClusters(dataset, a, -1);
                    TangleClusterer.getSoftClustering();
                    int[] hardClustering = TangleClusterer.getHardClustering();
                    long time2 = new Date().getTime();
                    System.out.println("Questionnaire test with " + i + " datapoints, " + j + " questions and " + k + " clusters took " + (time2 - time1) + " ms.");
                }
            }
            long sizeTime2 = new Date().getTime();
            System.out.println("Questionnaire tests with " + i + " datapoints finished. Total time: " + (sizeTime2 - sizeTime1) + " ms. Average time: " + (sizeTime2-sizeTime1)/sizeCount + " ms.");
        }
        long totalTime2 = new Date().getTime();
        System.out.println("Questionnaire tests finished in " + (totalTime2-totalTime1) + " ms.");
    }


}
