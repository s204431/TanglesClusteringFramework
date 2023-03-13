package main;

import datasets.*;
import model.Model;
import smile.graph.Graph;
import view.View;

public class Controller {
    private Model model;
    private View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void loadDatasetFromFile(String datasetTypeName, String fileName, int startRow, int endRow, int startColumn, int endColumn) {
        if (!view.isReady()) {
            return;
        }
        Dataset dataset = null;
        if (datasetTypeName.equals(BinaryQuestionnaire.name)) {
            dataset = new BinaryQuestionnaire();
            ((BinaryQuestionnaire) dataset).loadAnswersFromFile(fileName, startRow, endRow, startColumn, endColumn);
        }
        else if (datasetTypeName.equals(FeatureBasedDataset.name)) {
            dataset = new FeatureBasedDataset();
            ((FeatureBasedDataset) dataset).loadDataFromFile(fileName, startRow, endRow, startColumn, endColumn);
        }
        else if (datasetTypeName.equals(GraphDataset.name)) {
            dataset = new GraphDataset();
            ((GraphDataset) dataset).loadGraphFromFile(fileName);
        }
        model.setDataset(dataset);
        view.resetView();
        view.loadDataPoints();
    }

    public void createNewDataset(String datasetTypeName, int nPoints, int nDimensions, int nClusters) {
        if (!view.isReady()) {
            return;
        }
        Dataset dataset = null;
        if (datasetTypeName.equals(BinaryQuestionnaire.name)) {
            dataset = new BinaryQuestionnaire(DatasetGenerator.generateBiasedBinaryQuestionnaireAnswers(nPoints, nDimensions, nClusters));
        }
        else if (datasetTypeName.equals(FeatureBasedDataset.name)) {
            dataset = new FeatureBasedDataset(DatasetGenerator.generateFeatureBasedDataPoints(nPoints, nClusters, nDimensions));
        }
        else if (datasetTypeName.equals(GraphDataset.name)) {
            dataset = new GraphDataset(DatasetGenerator.generateRandomGraph(nPoints, nClusters));
        }
        model.setDataset(dataset);
        view.resetView();
        view.loadDataPoints();
    }

    public void generateClusteringTangles(int a, int psi, String initialCutGenerator, String costFunctionName) {
        model.generateClusters(a, psi, initialCutGenerator, costFunctionName);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

    public void generateClusteringKMeans(int k) {
        model.generateClustersKMeans(k);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

    public void generateClusteringSpectral(int k, double sigma) {
        model.generateClustersSpectral(k, sigma);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

    public void generateClusteringLinkage(int k) {
        model.generateClustersLinkage(k);
        view.loadClusters(model.getHardClustering(), model.getSoftClustering());
        view.updateSelectedSidePanel(model.getNMIScore(), model.getClusteringTime());
    }

}
