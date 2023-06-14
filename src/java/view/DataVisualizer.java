package view;

public interface DataVisualizer {

    //Responsible: Jens

    //Returns the number of points shown by the data visualizer.
    public int getNumberOfPoints();

    //Returns the total number of points in the data set.
    public int getOriginalNumberOfPoints();

    //Returns whether the data visualizer is ready to show the data set.
    public boolean isReady();

    //Loads a hard- and soft clustering into the data visualizer.
    public void loadClusters(int[] clusters, double[][] softClustering);

    //Loads a hard clustering into the data visualizer.
    public void loadClusters(int[] clusters);

    //Tells the data visualizer to show the ground truth if "show" is true.
    public void showGroundTruth(boolean show);
}
