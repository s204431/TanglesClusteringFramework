package model;

import datasets.Dataset;
import smile.validation.metric.NormalizedMutualInformation;

import java.util.Date;

public class Model {

    //This is the model for the Model-View-Controller design pattern.

    //Names of supported clustering algorithms.
    public static final String tangleName = "Tangle";
    public static final String kMeansName = "K-Means";
    public static final String spectralClusteringName = "Spectral";
    public static final String linkageName = "Linkage";

    private final TangleClusterer tangleClusterer = new TangleClusterer();
    private Dataset dataset;
    private long clusteringTime = -1;
    private double[][] softClustering;
    private int[] hardClustering;
    private double NMIScore = -1;

    public Model() {

    }

    //Sets the current dataset for the model.
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    //Returns the current dataset.
    public Dataset getDataset() {
        return dataset;
    }

    //Generates a hard clustering using tangles without updating the model.
    public int[] generateClusters(Dataset dataset, int a, int psi, String initialCutGenerator, String costFunctionName) {
        TangleClusterer clusterer = new TangleClusterer();
        clusterer.generateClusters(dataset, a, psi, initialCutGenerator, costFunctionName);
        return clusterer.getHardClustering();
    }

    //Generates a clustering using tangles and updates the model.
    public void generateClusters(int a, int psi, String initialCutGenerator, String costFunctionName) {
        long time = new Date().getTime();
        tangleClusterer.generateClusters(dataset, a, psi, initialCutGenerator, costFunctionName);
        softClustering = tangleClusterer.getSoftClustering();
        hardClustering = tangleClusterer.getHardClustering();
        clusteringTime = new Date().getTime() - time;
        updateNMIScore();
    }

    //Generates a clustering using K-Means and updates the model.
    public void generateClustersKMeans(int k) {
        long time = new Date().getTime();
        softClustering = null;
        hardClustering = dataset.kMeans(k);
        clusteringTime = new Date().getTime() - time;
        updateNMIScore();
    }

    //Generates a clustering using spectral clustering and updates the model.
    public void generateClustersSpectral(int k, double sigma) {
        long time = new Date().getTime();
        softClustering = null;
        hardClustering = dataset.spectralClustering(k, sigma);
        clusteringTime = new Date().getTime() - time;
        updateNMIScore();
    }

    //Generates a clustering using linkage clustering and updates the model.
    public void generateClustersLinkage(int k) {
        long time = new Date().getTime();
        softClustering = null;
        hardClustering = dataset.hierarchicalClustering(k);
        clusteringTime = new Date().getTime() - time;
        updateNMIScore();
    }

    //Updates the NMI score using the last generated hard clustering and the ground truth for the current dataset.
    private void updateNMIScore() {
        NMIScore = -1;
        if (getHardClustering() != null && dataset.getGroundTruth() != null) {
            NMIScore = NormalizedMutualInformation.joint(getHardClustering(), dataset.getGroundTruth());
        }
    }

    //Returns the last generated soft clustering.
    public double[][] getSoftClustering() {
        return softClustering;
    }

    //Returns the last generated hard clustering.
    public int[] getHardClustering() {
        return hardClustering;
    }

    //Returns the last calculated NMI score.
    public double getNMIScore() {
        return NMIScore;
    }

    //Returns the last measured clustering time.
    public long getClusteringTime() {
        return clusteringTime;
    }

}
