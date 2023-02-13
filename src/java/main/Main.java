package main;

import datasets.BinaryQuestionnaire;
import datasets.DatasetGenerator;
import datasets.FeatureBasedDataset;
import model.Model;
import test.ClusteringTester;
import util.Util.Tuple;
import view.View;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        //ClusteringTester.testTangleClusterer();
        View view = new View();
        Model model = new Model(view);
        int a = 2500;
        long time1 = new Date().getTime();
        Tuple<double[][], int[]> generated = DatasetGenerator.generateFeatureBasedDataPoints(10000, 2, 1);
        FeatureBasedDataset dataset = new FeatureBasedDataset(generated, a);
        //BinaryQuestionnaire dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(10000, 40, 6));
        long time2 = new Date().getTime();
        System.out.println("File loading time: " + (time2-time1) + " ms");
        model.generateClusters(dataset, a, -1);
        long time3 = new Date().getTime();
        System.out.println("Tangle total time: " + (time3-time2) + " ms");
        double nmiScore = model.getNMIScore();
        System.out.println("NMI score: " + nmiScore);
        model.plotDatapoints();
        System.out.println("\nkMeans:");
        long time4 = new Date().getTime();
        System.out.println();

        System.out.println("K-means time: " + (time4-time3) + " ms");
        System.out.println("Total time: " + (time4-time1) + " ms");

    }
}