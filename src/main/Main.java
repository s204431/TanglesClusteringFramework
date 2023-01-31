package main;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        BinaryQuestionnaire questionnaire = new BinaryQuestionnaire();
        questionnaire.loadAnswersFromFile("NPI.csv", 1, -1, 1, 40);
        TangleClusterer.generateClusters(questionnaire, 1500);
    }
}