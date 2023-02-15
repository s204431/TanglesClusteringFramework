package model;

import datasets.BinaryQuestionnaire;
import datasets.Dataset;
import datasets.FeatureBasedDataset;
import view.View;
import smile.validation.metric.NormalizedMutualInformation;
import java.util.Date;

public class Model {
    private TangleClusterer tangleClusterer = new TangleClusterer();
    private Dataset dataset;
    private View view;
    private long clusteringTime = 0;

    public Model() {

    }

    public Model(View view) {
        this.view = view;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void generateClusters(Dataset dataset, int a, int psi) {
        long time = new Date().getTime();
        this.dataset = dataset;
        tangleClusterer.generateClusters(dataset, a, psi);
        clusteringTime = new Date().getTime() - time;
    }

    public void regenerateClusters(int a) {
        long time = new Date().getTime();
        tangleClusterer.generateClusters(dataset, a, -1);
        clusteringTime = new Date().getTime() - time;
    }

    public double[][] getSoftClustering() {
        return tangleClusterer.getSoftClustering();
    }

    public int[] getHardClustering() {
        return tangleClusterer.getHardClustering();
    }

    public void plotDataPoints() {
        if (dataset instanceof BinaryQuestionnaire) {
            view.loadPoints(((BinaryQuestionnaire) dataset).answers);
        }
        else if (dataset instanceof FeatureBasedDataset) {
            view.loadPoints(((FeatureBasedDataset) dataset).dataPoints);
        }
    }

    public double getNMIScore() {
        if (!tangleClusterer.doneClustering || dataset.getGroundTruth() == null) {
            return -1;
        }
        return NormalizedMutualInformation.joint(getHardClustering(), dataset.getGroundTruth());
    }

    public long getClusteringTime() {
        return clusteringTime;
    }

}
