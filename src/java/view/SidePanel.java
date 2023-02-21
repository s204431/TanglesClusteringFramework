package view;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {
    protected View view;
    protected double[][] softClustering;
    protected int[] hardClustering;
    private double NMIScore = -1;
    private long clusteringTime = -1;

    public SidePanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (view.getDataset() == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("TimesRoman", Font.BOLD, 18));
        g2d.drawString("Total points: ", 30, 30);
        g2d.drawString(""+view.plottingView.originalNumberOfPoints, 30, 50);
        if (view.plottingView.originalNumberOfPoints != view.plottingView.getNumberOfPoints()) {
            g2d.drawString("Showing "+view.plottingView.getNumberOfPoints(), 30, 70);
        }
        if (NMIScore >= 0) {
            g2d.drawString("NMI score:", 30, 130);
            g2d.drawString(""+((int)(NMIScore*100000)/100000.0), 30, 150);
        }
        if (clusteringTime >= 0) {
            g2d.drawString("Clustering time:", 30, 180);
            g2d.drawString(clusteringTime + " ms", 30, 200);
        }
    }

    //Called when a value in the panel has changed.
    protected void valueChanged() {

    }

    //Updates the total number of data points.
    protected void update(int n) {

    }

    protected void setBounds() {
        setBounds(view.windowWidth - view.sidePanelWidth, view.topPanelHeight, view.windowWidth, view.windowHeight);
    }

    //Sets different values to be stored by the panel.
    protected void setValues(double NMIScore, long clusteringTime) {
        this.NMIScore = NMIScore;
        this.clusteringTime = clusteringTime;
    }

    protected void setClustering(int[] hardClustering, double[][] softClustering) {
        this.hardClustering = hardClustering;
        this.softClustering = softClustering;
    }

}