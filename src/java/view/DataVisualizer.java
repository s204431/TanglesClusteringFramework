package view;

public interface DataVisualizer {

    //Responsible: Jens

    public int getNumberOfPoints();
    public int getOriginalNumberOfPoints();
    public boolean isReady();
    public void loadClusters(int[] clusters, double[][] softClustering);
    public void loadClusters(int[] clusters);
}
