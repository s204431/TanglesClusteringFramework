package test;

import datasets.DatasetGenerator;
import util.BitSet;
import util.Util.Tuple;

import java.util.Date;

import datasets.BinaryQuestionnaire;
import datasets.Dataset;
import datasets.FeatureBasedDataset;
import model.Model;
import smile.validation.metric.NormalizedMutualInformation;

public class ClusteringTester {

    //private int[][] randomFeatureBasedTestSet = new int[][] {{1000, }}; //{nDataPoints, nClusters}
    private static Model model = new Model();

    public static void testTangleClusterer() {
        System.out.println("Testing tangle clusterer...");
        long time1 = new Date().getTime();
        double[] result1 = testTangleClustererFeatureBased();
        System.out.println();
        double[] result2 = testTangleClustererBinaryQuestionnaire();
        long time2 = new Date().getTime();
        System.out.println();
        System.out.println("Feature based average NMI score: " + result1[0]/result1[1]);
        System.out.println("Questionnaire average NMI score: " + result2[0]/result2[1]);
        System.out.println("Tests finished. Total time: " + (time2 - time1) + " ms. Average NMI score: " + (result1[0]+result2[0])/(result1[1]+result2[1]));
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
                Tuple<double[][], int[]> generated = DatasetGenerator.generateGaussianMixturePoints(i, j);
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


}
