package main;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        //new PlottingView().loadPoints(dataset.dataPoints);
        long time1 = new Date().getTime();
        //FeatureBasedDataset dataset = new FeatureBasedDataset(DatasetGenerator.generateGaussianMixturePoints(5000, 4), 600);
        FeatureBasedDataset dataset = new FeatureBasedDataset(300);
        dataset.loadDataFromFile("LyngbyWeatherData.csv", 4, 5000, 1, -1);
        //BinaryQuestionnaire dataset = new main.BinaryQuestionnaire(main.DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(6000000, 40));
        //BinaryQuestionnaire dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(6000, 40,4));
        //BinaryQuestionnaire dataset = new main.BinaryQuestionnaire();
        //dataset.loadAnswersFromFile("NPI.csv", 1, -1, 1, 40);
        long time2 = new Date().getTime();
        System.out.println("File loading time: " + (time2-time1) + " ms");
        TangleClusterer.generateClusters(dataset, 300, -1);
        long time3 = new Date().getTime();
        System.out.println("Tangle total time: " + (time3-time2) + " ms");
        new PlottingView().loadPointsWithClustering(dataset.dataPoints, TangleClusterer.getHardClustering());
        System.out.println("\nkMeans:");
        /*int[] kMeansResult = dataset.kMeans();
        System.out.println("Resulting clustering for first 50 participants: ");
        for (int i = 0; i < 50; i++) {
            System.out.print(kMeansResult[i] + " ");
        }*/
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
/*
        int n = 1000;
        int clusters = 4;

        double[][] points = DatasetGenerator.generateGaussianMixturePoints(n, clusters);
        int[] clustering = new int[n];
        for (int i = 0; i < n; i++) {
            clustering[i] = i%clusters;
        }

        new PlottingView().loadPointsWithClustering(points, clustering);*/
    }
}