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
    private int a;
    private int psi;
    private long clusteringTime = 0;

    public Model() {

    }

    public Model(View view) {
        this.view = view;
    }

    public void generateClusters(Dataset dataset, int a, int psi) {
        long time = new Date().getTime();
        this.dataset = dataset;
        this.a = a;
        this.psi = psi;
        tangleClusterer.generateClusters(dataset, a, psi);
        clusteringTime = new Date().getTime() - time;
    }

    public void regenerateClusters(int a) {
        long time = new Date().getTime();
        this.a = a;
        tangleClusterer.generateClusters(dataset, a, psi);
        double[][] softClustering = tangleClusterer.getSoftClustering();
        clusteringTime = new Date().getTime() - time;
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
        if (!tangleClusterer.doneClustering || dataset.getGroundTruth() == null) {
            return -1;
        }
        return NormalizedMutualInformation.joint(getHardClustering(), dataset.getGroundTruth());
    }

    public long getClusteringTime() {
        return clusteringTime;
    }

}
