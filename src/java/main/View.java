package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

public class View extends JFrame {
    private int windowWidth, windowHeight;

    private JPanel mainComponent;
    private PlottingView plottingView;

    public View() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        windowWidth = screenSize.width - screenSize.width / 10;
        windowHeight = screenSize.height - screenSize.height / 10;

        //Create frame
        setTitle("View");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(null);

        //Create components
        mainComponent = new JPanel();
        mainComponent.setPreferredSize(new Dimension(windowWidth, windowHeight));
        mainComponent.setBounds(0, 0, windowWidth, windowHeight);
        mainComponent.setLayout(null);

        plottingView = new PlottingView(this);
        plottingView.setBounds(0, 0, getPreferredSize().width, getPreferredSize().height);
        plottingView.setLayout(null);

        //Add components
        mainComponent.add(plottingView);
        add(mainComponent);
        pack();
        setLocationByPlatform(true);
        setLocationRelativeTo(null);
        setVisible(true);

        addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                windowWidth = getWidth();
                windowHeight = getHeight();
                repaint();
            }
        });
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters, double[][] softClustering) {
        plottingView.loadPointsWithClustering(points, clusters, softClustering);
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters) {
        plottingView.loadPointsWithClustering(points, clusters);
    }

    public void loadPoints(double[][] points) {
        plottingView.loadPoints(points);
    }

    public void loadClusters(int[] clusters) {
        plottingView.loadClusters(clusters);
    }

    public void loadClusters(int[] clusters, double[][] softClustering) {
        plottingView.loadClusters(clusters, softClustering);
    }

    protected int getWindowHeight() {
        return windowHeight;
    }

    protected int getWindowWidth() {
        return windowWidth;
    }
}
