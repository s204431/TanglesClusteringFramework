package main;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        BinaryQuestionnaire questionnaire = new BinaryQuestionnaire();
        long time1 = new Date().getTime();
        questionnaire.loadAnswersFromFile("NPI.csv", 1, 2000, 1, 40);
        TangleClusterer.generateClusters(questionnaire, 150);
        long time2 = new Date().getTime();
        System.out.println("Total time: " + (time2-time1) + " ms");
    }
}