package controller;

import datasets.*;
import model.Model;
import view.GraphView;
import view.ImageView;
import view.PlottingView;
import view.View;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Controller {

    //Responsible: Jens

    //This is the controller for the Model-View-Controller design pattern.

    private final Model model;
    private final View view;

    //Constructor taking references to the model and the view.
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    //Loads a dataset with a specific data type from a file and updates the view and the model.
    public void loadDatasetFromFile(String datasetTypeName, String fileName, int startRow, int endRow, int startColumn, int endColumn, boolean normalizeDataPoints) {
        if (!view.isReady()) {
            return;
        }
        Dataset dataset = null;
        switch (datasetTypeName) {
            case BinaryQuestionnaire.name -> {
                dataset = new BinaryQuestionnaire();
                ((BinaryQuestionnaire) dataset).loadAnswersFromFile(fileName, startRow, endRow, startColumn, endColumn);
            }
            case FeatureBasedDataset.name -> {
                dataset = new FeatureBasedDataset();
                ((FeatureBasedDataset) dataset).loadDataFromFile(fileName, startRow, endRow, startColumn, endColumn, normalizeDataPoints);
            }
            case GraphDataset.name -> {
                dataset = new GraphDataset();
                ((GraphDataset) dataset).loadGraphFromFile(fileName);
            }
        }
        model.setDataset(dataset);
        view.resetView(dataset instanceof GraphDataset ? GraphView.name : PlottingView.name);
        view.loadDataPoints();
    }

    //Loads an image from a file and updates the view and the model.
    public void loadImageFromFile(String inputFilePath) {
        try {
            BufferedImage image = ImageIO.read(new FileInputStream(new File(inputFilePath)));
            model.loadImage(image);
            view.resetView(ImageView.name);
            view.loadImage(image);
        } catch (IOException e) {}
    }

    //Generates a new dataset with a specific data type and updates the view and the model.
    public void createNewDataset(String datasetTypeName, int nPoints, int nDimensions, int nClusters) {
        if (!view.isReady()) {
            return;
        }
        Dataset dataset = switch (datasetTypeName) {
            case BinaryQuestionnaire.name -> new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(nPoints, nDimensions, nClusters));
            case FeatureBasedDataset.name -> new FeatureBasedDataset(DatasetGenerator.generateFeatureBasedDataPoints(nPoints, nClusters, nDimensions));
            case GraphDataset.name -> new GraphDataset(DatasetGenerator.generateRandomGraph(nPoints, nClusters));
            default -> null;
        };
        model.setDataset(dataset);
        view.resetView(dataset instanceof GraphDataset ? GraphView.name : PlottingView.name);
        view.loadDataPoints();
    }

    //Generates a clustering using tangles with a specific initial cut generator and cost function. Updates the model and the view.
    public void generateClusteringTangles(int a, int psi, String initialCutGenerator, String costFunctionName) {
        model.generateClusters(a, psi, initialCutGenerator, costFunctionName);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

    //Generates a clustering using K-Means. Updates the model and the view.
    public void generateClusteringKMeans(int k) {
        model.generateClustersKMeans(k);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

    //Generates a clustering using spectral clustering. Updates the model and the view.
    public void generateClusteringSpectral(int k, double sigma) {
        model.generateClustersSpectral(k, sigma);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

    //Generates a clustering using linkage clustering. Updates the model and the view.
    public void generateClusteringLinkage(int k) {
        model.generateClustersLinkage(k);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

}
