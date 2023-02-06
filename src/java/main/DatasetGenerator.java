package main;

import java.util.Random;

public class DatasetGenerator {

    //Generates a random main.BitSet of answers to a binary questionnaire and returns the result
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
    public static BitSet[] generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions, double distributionPercentage) {
        if (distributionPercentage < 0 || distributionPercentage > 1) {
            return null;
        }

        BitSet[] result = new BitSet[numberOfAnswers];
        Random r = new Random();
        int nPartition = (int)(numberOfAnswers * distributionPercentage);

        //Cluster of mainly false answers
        for (int i = 0; i < nPartition; i++) {
            result[i] = new BitSet(numberOfQuestions);
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextInt(100) >= 90) {
                    result[i].add(j);
                }
            }
        }

        //Cluster of mainly true answers
        for (int i = nPartition; i < numberOfAnswers; i++) {
            result[i] = new BitSet(numberOfQuestions);
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextInt(100) < 90) {
                    result[i].add(j);
                }
            }
        }
        return result;
    }

    public static BitSet[] generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions) {
        return generateBiasedBinaryQuestionnaireAnswers(numberOfAnswers, numberOfQuestions, 0.5);
    }

    //Generates biased binary questionnaire answers with a specific number of clusters.
    public static BitSet[] generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions, int numberOfClusters) {
        BitSet[] result = new BitSet[numberOfAnswers];
        Random r = new Random();
        int index = 0;
        for (int i = 0; i < numberOfClusters; i++) {
            int extra = numberOfAnswers % numberOfClusters;
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
                for (int k = 0; k < numberOfQuestions; k++) {
                    result[index].setValue(k, center.get(k));
                    if (r.nextDouble() >= 0.9) {
                        result[index].flip(k);
                    }
                }
                index++;
            }
        }
        return result;
    }

    //Generates an array of points based on a Gaussian Mixture function and returns the result
    public static Point[] generateGaussianMixturePoints(int numberOfPoints, int numberOfClusters) {
        Point[] result = new Point[numberOfPoints];
        int K = numberOfClusters;
        Random r = new Random();

        //Generate means of clusters
        Point[] meanPoints = new Point[K];
        double dist = numberOfPoints * 0.1;
        double rad = Math.PI / K;
        for (int i = 0; i < K; i++) {
            meanPoints[i] = new Point(dist * Math.cos(rad + 2*rad*i), dist * Math.sin(rad + 2*rad*i));
        }

        //Generate points around means
        double std = numberOfPoints * 0.02;
        for (int i = 0; i < numberOfPoints; i++) {
            int j = i % K;
            double meanX = meanPoints[j].getX();
            double meanY = meanPoints[j].getY();
            result[i] = new Point(r.nextGaussian(meanX, std), r.nextGaussian(meanY, std));
        }

        return result;
    }

}
