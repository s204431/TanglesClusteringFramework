package main;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        int a = 15000;
        //new PlottingView().loadPoints(dataset.dataPoints);
        long time1 = new Date().getTime();
        FeatureBasedDataset dataset = new FeatureBasedDataset(DatasetGenerator.generateGaussianMixturePoints(100000, 4), a);
        //FeatureBasedDataset dataset = new FeatureBasedDataset(900);
        //dataset.loadDataFromFile("LyngbyWeatherData.csv", 4, 5000, 1, -1);
        //BinaryQuestionnaire dataset = new main.BinaryQuestionnaire(main.DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(6000000, 40));
        //BinaryQuestionnaire dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(6000, 40,4));
        //BinaryQuestionnaire dataset = new main.BinaryQuestionnaire();
        //dataset.loadAnswersFromFile("NPI.csv", 1, -1, 1, 40);
        long time2 = new Date().getTime();
        System.out.println("File loading time: " + (time2-time1) + " ms");
        TangleClusterer.generateClusters(dataset, a, -1);
        long time3 = new Date().getTime();
        System.out.println("Tangle total time: " + (time3-time2) + " ms");
        new PlottingView().loadPointsWithClustering(dataset.dataPoints, TangleClusterer.getHardClustering(), TangleClusterer.getSoftClustering());
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
        //Tests of PlottingView
        int n = 5000;
        int clusters = 7;

        double[][] points = DatasetGenerator.generateGaussianMixturePoints(n, clusters);

        int[] clustering = new int[n];
        for (int i = 0; i < n; i++) {
            clustering[i] = i % clusters;
        }

        double[][] softClustering = new double[n][clusters];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < clusters; j++) {
                softClustering[i][j] = i % 3 * 0.4;
            }
        }

        new PlottingView().loadPointsWithClustering(points, clustering, softClustering);

 */

    }
}