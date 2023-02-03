package main;

import java.util.Random;

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

    //Generates a biased BitSet of answers to a binary questionnaire and returns the result
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

}
