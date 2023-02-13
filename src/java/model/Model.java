package model;

import datasets.BinaryQuestionnaire;
import datasets.Dataset;
import datasets.FeatureBasedDataset;
import util.BitSet;
import main.View;
import smile.validation.metric.NormalizedMutualInformation;

public class Model {
    private TangleClusterer tangleClusterer = new TangleClusterer();
    private Dataset dataset;
    private View view;

    public Model() {

    }

    public Model(View view) {
        this.view = view;
    }

    public void generateClusters(Dataset dataset, int a, int psi) {
        this.dataset = dataset;
        tangleClusterer.generateClusters(dataset, a, psi);
    }

    public void generateClusters(BitSet[] questionnaireAnswers, int a, int psi) {
        dataset = new BinaryQuestionnaire(questionnaireAnswers);
        tangleClusterer.generateClusters(dataset, a, psi);
    }

    public double[][] getSoftClustering() {
        return tangleClusterer.getSoftClustering();
    }

    public int[] getHardClustering() {
        return tangleClusterer.getHardClustering();
    }

    public void plotDatapoints() {
        if (dataset instanceof BinaryQuestionnaire) {

        }
        else if (dataset instanceof FeatureBasedDataset) {
            view.loadPointsWithClustering(((FeatureBasedDataset) dataset).dataPoints, tangleClusterer.getHardClustering(), tangleClusterer.getSoftClustering());
        }
    }

    public double getNMIScore() {
        if (dataset.getGroundTruth() == null) {
            System.out.println("Cannot calculate NMI score because ground truth is null.");
            return -1;
        }
        return NormalizedMutualInformation.joint(getHardClustering(), dataset.getGroundTruth());
    }

}
