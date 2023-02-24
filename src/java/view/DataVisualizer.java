package view;

import javax.swing.*;

public interface DataVisualizer {
    public int getNumberOfPoints();
    public int getOriginalNumberOfPoints();
    public boolean isReady();
    public void loadClusters(int[] clusters, double[][] softClustering);
    public void loadClusters(int[] clusters);
}
