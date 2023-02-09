package main;

import java.util.Random;
import main.Util.Tuple;

public class DatasetGenerator {

    //Generates a random BitSet of answers to a binary questionnaire and returns the result
    public static BitSet[] generateRandomBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions) {
        BitSet[] result = new BitSet[numberOfAnswers];
        Random r = new Random();
        for (int i = 0; i < numberOfAnswers; i++) {
            result[i] = new BitSet(numberOfQuestions);
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextBoolean()) {
                    result[i].add(j);
                }
            }
        }
        return result;
    }

    //Generates a biased main.BitSet of answers to a binary questionnaire and returns the result
    public static Tuple<BitSet[], int[]> generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions, double distributionPercentage) {
        if (distributionPercentage < 0 || distributionPercentage > 1) {
            return null;
        }

        BitSet[] result = new BitSet[numberOfAnswers];
        int[] groundTruth = new int[numberOfAnswers];
        Random r = new Random();
        int nPartition = (int)(numberOfAnswers * distributionPercentage);

        //Cluster of mainly false answers
        for (int i = 0; i < nPartition; i++) {
            result[i] = new BitSet(numberOfQuestions);
            groundTruth[i] = 0;
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextInt(100) >= 90) {
                    result[i].add(j);
                }
            }
        }

        //Cluster of mainly true answers
        for (int i = nPartition; i < numberOfAnswers; i++) {
            result[i] = new BitSet(numberOfQuestions);
            groundTruth[i] = 1;
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextInt(100) < 90) {
                    result[i].add(j);
                }
            }
        }
        return new Tuple<>(result, groundTruth);
    }

    public static Tuple<BitSet[], int[]> generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions) {
        return generateBiasedBinaryQuestionnaireAnswers(numberOfAnswers, numberOfQuestions, 0.5);
    }

    //Generates biased binary questionnaire answers with a specific number of clusters.
    public static Tuple<BitSet[], int[]> generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions, int numberOfClusters) {
        BitSet[] result = new BitSet[numberOfAnswers];
        int[] groundTruth = new int[numberOfAnswers];
        Random r = new Random();
        int index = 0;
        int extra = numberOfAnswers % numberOfClusters;
        for (int i = 0; i < numberOfClusters; i++) {
            BitSet center = new BitSet(numberOfQuestions);
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextBoolean()) {
                    center.add(j);
                }
            }
            for (int j = 0; j < numberOfAnswers/numberOfClusters+1; j++) {
                if (j == numberOfAnswers/numberOfClusters) {
                    if (extra == 0) {
                        continue;
                    }
                    extra--;
                }
                result[index] = new BitSet(numberOfQuestions);
                groundTruth[index] = i;
                for (int k = 0; k < numberOfQuestions; k++) {
                    result[index].setValue(k, center.get(k));
                    if (r.nextDouble() >= 0.9) {
                        result[index].flip(k);
                    }
                }
                index++;
            }
        }
        return new Tuple<>(result, groundTruth);
    }

    //Generates an array of points based on a Gaussian Mixture function and returns the result
    public static Tuple<double[][], int[]> generateGaussianMixturePoints(int numberOfPoints, int numberOfClusters) {
        double[][] result = new double[numberOfPoints][2];
        int[] groundTruth = new int[numberOfPoints];
        int K = numberOfClusters;
        Random r = new Random();

        //Generate means of clusters
        double[][] meanPoints = new double[K][2];
        double dist = numberOfPoints * 0.15;
        double rad = Math.PI / K;
        for (int i = 0; i < K; i++) {
            meanPoints[i] = new double[] { dist * Math.cos(rad + 2*rad*i), dist * Math.sin(rad + 2*rad*i) };
        }

        //Generate points around means
        double std = numberOfPoints * 0.02;
        for (int i = 0; i < numberOfPoints; i++) {
            int j = i % K;
            double meanX = meanPoints[j][0];
            double meanY = meanPoints[j][1];
            result[i] = new double[] { r.nextGaussian(meanX, std), r.nextGaussian(meanY, std) };
            groundTruth[i] = j;
        }

        return new Tuple<>(result, groundTruth);
    }

}
