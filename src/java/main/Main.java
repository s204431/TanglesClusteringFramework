package main;

import datasets.*;
import model.Model;
import util.Tuple;
import view.PlottingView;
import view.View;
import controller.Controller;

public class Main {

    //Main method for the program.
    public static void main(String[] args) {
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);
        view.setController(controller);
        Tuple<double[][], int[]> generated = DatasetGenerator.generateFeatureBasedDataPoints(1000, 4, 2);
        FeatureBasedDataset dataset = new FeatureBasedDataset(generated);
        model.setDataset(dataset);
        view.resetView(PlottingView.name);
        view.loadDataPoints();
    }
}