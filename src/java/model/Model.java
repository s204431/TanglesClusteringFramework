package model;

import datasets.BinaryQuestionnaire;
import datasets.Dataset;
import datasets.FeatureBasedDataset;
import util.BitSet;
import view.View;
import smile.validation.metric.NormalizedMutualInformation;

public class Model {
    private TangleClusterer tangleClusterer = new TangleClusterer();
    private Dataset dataset;
    private View view;
    private int a;
    private int psi;

    public Model() {

    }

    public Model(View view) {
        this.view = view;
    }

    public void generateClusters(Dataset dataset, int a, int psi) {
        this.dataset = dataset;
        this.a = a;
        this.psi = psi;
        tangleClusterer.generateClusters(dataset, a, psi);
    }

    public void regenerateClusters(int a) {
        this.a = a;
        tangleClusterer.generateClusters(dataset, a, psi);
        double[][] softClustering = tangleClusterer.getSoftClustering();
        view.loadClusters(tangleClusterer.getHardClustering(), softClustering);
    }

    public double[][] getSoftClustering() {
        return tangleClusterer.getSoftClustering();
    }

    public int[] getHardClustering() {
        return tangleClusterer.getHardClustering();
    }

    public void plotDataPoints() {
        if (dataset instanceof BinaryQuestionnaire) {
            view.loadPointsWithClustering(((BinaryQuestionnaire) dataset).answers, tangleClusterer.getHardClustering(), tangleClusterer.getSoftClustering());
        }
        else if (dataset instanceof FeatureBasedDataset) {
            view.loadPointsWithClustering(((FeatureBasedDataset) dataset).dataPoints, tangleClusterer.getHardClustering(), tangleClusterer.getSoftClustering());
        }
        view.updateAValue(a);
    }

    public double getNMIScore() {
        if (dataset.getGroundTruth() == null) {
            System.out.println("Cannot calculate NMI score because ground truth is null.");
            return -1;
        }
        return NormalizedMutualInformation.joint(getHardClustering(), dataset.getGroundTruth());
    }

}
