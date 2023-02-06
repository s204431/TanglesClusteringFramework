package main;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        long time1 = new Date().getTime();
        //BinaryQuestionnaire questionnaire = new main.BinaryQuestionnaire(main.DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(6000000, 40));
        //BinaryQuestionnaire questionnaire = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(6000, 40,4));
        BinaryQuestionnaire questionnaire = new main.BinaryQuestionnaire();
        questionnaire.loadAnswersFromFile("NPI.csv", 1, -1, 1, 40);
        System.out.println(questionnaire.getNumberOfParticipants());
        long time2 = new Date().getTime();
        System.out.println("File loading time: " + (time2-time1) + " ms");
        TangleClusterer.generateClusters(questionnaire, 1500, -1);
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

        System.out.println("K-means time: " + (time4-time3) + " ms");
        System.out.println("Total time: " + (time4-time1) + " ms");

        /*
        // Tests of the main.DatasetGenerator

        int participants = 20;
        int questions = 50;
        main.BitSet[] answers = main.DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(participants, questions, 0.75);
        for (int i = 0; i < participants; i++) {
            answers[i].print();
        }

        main.Point[] points = main.DatasetGenerator.generateGaussianMixturePoints(1000, 5);
        System.out.println("Copy-paste the following into Maple:");
        System.out.println("with(plots):");
        System.out.print("pointplot([");
        for (main.Point point : points) {
            System.out.print(point.getX() + ", ");
        }
        System.out.print("], [");

        for (main.Point point : points) {
            System.out.print(point.getY() + ", ");
        }
        System.out.println("])");

         */
    }
}