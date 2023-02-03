package main;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        long time1 = new Date().getTime();
        BinaryQuestionnaire questionnaire = new BinaryQuestionnaire(DatasetGenerator.generateRandomBinaryQuestionnaireAnswers(6000000, 40));
        //BinaryQuestionnaire questionnaire = new BinaryQuestionnaire();
        //questionnaire.loadAnswersFromFile("NPI.csv", 1, -1, 1, 40);
        long time2 = new Date().getTime();
        System.out.println("File loading time: " + (time2-time1) + " ms");
        TangleClusterer.generateClusters(questionnaire, 750000, -1);
        long time3 = new Date().getTime();
        System.out.println("Tangle total time: " + (time3-time2) + " ms");
        System.out.println("\nkMeans:");
        int[] kMeansResult = questionnaire.kMeans();
        System.out.println("Resulting clustering for first 50 participants: ");
        for (int i = 0; i < 50; i++) {
            System.out.print(kMeansResult[i] + " ");
        }
        long time4 = new Date().getTime();
        System.out.println();

        /*
        int participants = 20;
        int questions = 50;
        BitSet[] answers = DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(participants, questions, 0.75);
        for (int i = 0; i < participants; i++) {
            answers[i].print();
        }
         */
        System.out.println("K-means time: " + (time4-time3) + " ms");
        System.out.println("Total time: " + (time4-time1) + " ms");
    }
}