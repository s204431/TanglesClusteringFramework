package main;

import datasets.*;
import model.Model;
import model.TangleClusterer;
import util.Tuple;
import view.PlottingView;
import view.View;
import controller.Controller;

public class Main {
    public static void main(String[] args) {
        //ClusteringTester.testTangleClusterer();
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);
        view.setController(controller);
        Tuple<double[][], int[]> generated = DatasetGenerator.generateFeatureBasedDataPoints(1000, 4, 2);
        //Tuple<double[][], int[]> generated = DatasetGenerator.generateFixedMoonFeatureBasedDataPoints(1000); //Generates a dataset with two moons.
        //Tuple<double[][], int[]> generated = DatasetGenerator.generateMoonFeatureBasedDataPoints(1000, 4); //Generates a dataset with moons.
        FeatureBasedDataset dataset = new FeatureBasedDataset(generated);
        //BinaryQuestionnaire dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(10000, 40, 4));
        model.setDataset(dataset);
        view.resetView(PlottingView.name);
        view.loadDataPoints();
    }
}