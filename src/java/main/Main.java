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
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);
        view.setController(controller);
        int a = 10000;
        long time1 = new Date().getTime();
        Tuple<double[][], int[]> generated = DatasetGenerator.generateFeatureBasedDataPoints(100000, 4, 2);
        FeatureBasedDataset dataset = new FeatureBasedDataset(generated);
        //BinaryQuestionnaire dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(1000000, 40, 4));
        model.setDataset(dataset);
        view.resetView();
        view.loadDataPoints();
        long time2 = new Date().getTime();
        System.out.println("File loading time: " + (time2-time1) + " ms");
        long time3 = new Date().getTime();
        System.out.println("Tangle total time: " + (time3-time2) + " ms");
        double nmiScore = model.getNMIScore();
        System.out.println("NMI score: " + nmiScore);
        view.loadDataPoints();
        long time4 = new Date().getTime();
        System.out.println("Total time: " + (time4-time1) + " ms");


/*
        //Test of feature based k-means
        View view = new View();
        Model model = new Model(view);
        view.setModel(model);

        int numOfPoints = 1500;
        int numOfClusters = 4;
        int numOfFeatures = 3;
        FeatureBasedDataset dataset = new FeatureBasedDataset(DatasetGenerator.generateGaussianMixturePoints(numOfPoints, numOfClusters));
        int[] kMeansClustering = dataset.kMeans(numOfClusters);
        dataset.printKMeansResults(kMeansClustering);

        view.loadPointsWithClustering(dataset.dataPoints, kMeansClustering);
 */
    }
}