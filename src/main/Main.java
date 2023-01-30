package main;

public class Main {
    public static void main(String[] args) {
        BinaryQuestionnaire questionnaire = new BinaryQuestionnaire();
        questionnaire.loadAnswersFromFile("NPI.csv", 1, 1000, 1, 40);
        TangleClusterer.generateClusters(questionnaire, 150);
    }
}