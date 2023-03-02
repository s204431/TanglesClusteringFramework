package view;

import datasets.BinaryQuestionnaire;
import datasets.Dataset;
import datasets.FeatureBasedDataset;
import datasets.GraphDataset;
import main.Controller;
import model.Model;
import util.BitSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class View extends JFrame {
    private Model model;
    private Controller controller;
    protected int windowWidth, windowHeight;

    protected JPanel mainComponent;
    protected DataVisualizer dataVisualizer;
    protected StatisticsPanel statisticsPanel;
    private JTabbedPane pane;
    private List<SidePanel> sidePanels = new ArrayList<>();
    public SidePanel selectedSidePanel;
    private TopPanel topPanel;
    private StatisticsTopPanel statisticsTopPanel;

    protected int topPanelHeight;
    protected int sidePanelWidth;

    public View(Model model) {
        this.model = model;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        windowWidth = screenSize.width - screenSize.width / 3;
        windowHeight = screenSize.height - screenSize.height / 3;

        //Create frame
        setTitle("View");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(null);

        //Create components
        mainComponent = new JPanel();
        mainComponent.setLayout(null);

        dataVisualizer = new PlottingView(this);
        statisticsPanel = new StatisticsPanel(this);
        pane = new JTabbedPane();
        pane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (((JTabbedPane) e.getSource()).getSelectedComponent() != null) {
                    ((JComponent)((JTabbedPane) e.getSource()).getSelectedComponent()).add((JPanel)dataVisualizer);
                    changeSidePanel(((JTabbedPane) e.getSource()).getSelectedIndex());
                }
            }
        });

        topPanel = new TopPanel(this);
        statisticsTopPanel = new StatisticsTopPanel(this);

        SidePanel sidePanel = new SidePanel(this);
        sidePanels.add(sidePanel);
        selectedSidePanel = sidePanel;
        pane.addTab("", new JPanel(null));
        ((JComponent)pane.getSelectedComponent()).add((JPanel)dataVisualizer);
        mainComponent.add(sidePanel);

        setBounds();

        mainComponent.add(pane);
        mainComponent.add(topPanel);

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
                setBounds();
                repaint();
            }
        });
    }

    //Adds a new side panel/tab.
    protected void addSidePanel(SidePanel sidePanel, String name) {
        mainComponent.add(sidePanel);
        sidePanels.add(sidePanel);
        sidePanel.setBounds();
        sidePanel.setVisible(false);
        pane.addTab(name, new JPanel(null));
    }

    //Changes the side panel/tab.
    protected void changeSidePanel(int index) {
        selectedSidePanel.setVisible(false);
        selectedSidePanel = sidePanels.get(index);
        selectedSidePanel.setVisible(true);
        selectedSidePanel.setBounds();
        selectedSidePanel.update(dataVisualizer.getOriginalNumberOfPoints());
        loadClusters(selectedSidePanel.hardClustering, selectedSidePanel.softClustering);
    }

    private void setBounds() {
        topPanelHeight = windowHeight / 20;
        sidePanelWidth = windowWidth / 8;

        topPanelHeight = Math.max(topPanelHeight, 30);
        sidePanelWidth = Math.max(sidePanelWidth, 200);

        mainComponent.setPreferredSize(new Dimension(windowWidth, windowHeight));
        mainComponent.setBounds(0, 0, windowWidth, windowHeight);
        ((JPanel)dataVisualizer).setBounds(0, 0, windowWidth - sidePanelWidth, windowHeight);
        pane.setBounds(0, topPanelHeight, windowWidth - sidePanelWidth, windowHeight);
        selectedSidePanel.setBounds();
        topPanel.setBounds();
        statisticsPanel.setBounds();
        statisticsTopPanel.setBounds();
    }

    //Loads the data points from the dataset currently loaded by the model.
    public void loadDataPoints() {
        Dataset dataset = model.getDataset();
        if (dataset == null) {
            return;
        }
        if (dataset instanceof BinaryQuestionnaire) {
            loadPoints(((BinaryQuestionnaire) dataset).answers);
        }
        else if (dataset instanceof FeatureBasedDataset) {
            loadPoints(((FeatureBasedDataset) dataset).dataPoints);
        }
        else if (dataset instanceof GraphDataset) {
            loadPoints(((GraphDataset) dataset).asDot());
        }
    }

    public void loadPoints(double[][] points) {
        ((PlottingView)dataVisualizer).loadPoints(points);
        selectedSidePanel.update(points.length);
    }

    public void loadPoints(BitSet[] points) {
        ((PlottingView)dataVisualizer).loadPoints(points);
        selectedSidePanel.update(points.length);
    }

    public void loadPoints(String graphDotString) {
        ((GraphView)dataVisualizer).loadGraphFromDotString(graphDotString);
        selectedSidePanel.update(dataVisualizer.getOriginalNumberOfPoints());
    }

    public void loadClusters(int[] clusters, double[][] softClustering) {
        selectedSidePanel.setClustering(clusters, softClustering);
        dataVisualizer.loadClusters(clusters, softClustering);
    }

    protected void showClustering(int a, int psi) {
        model.generateClusters(a, -1);
        loadClusters(model.getHardClustering(), model.getSoftClustering());
        selectedSidePanel.setValues(model.getNMIScore(), model.getClusteringTime());
    }

    protected void showClusteringKMeans(int k) {
        model.generateClustersKMeans(k);
        loadClusters(model.getHardClustering(), model.getSoftClustering());
        selectedSidePanel.setValues(model.getNMIScore(), model.getClusteringTime());
    }

    protected void showClusteringSpectral(int k, double sigma) {
        model.generateClustersSpectral(k, sigma);
        loadClusters(model.getHardClustering(), model.getSoftClustering());
        selectedSidePanel.setValues(model.getNMIScore(), model.getClusteringTime());
    }

    protected void showClusteringLinkage(int k) {
        model.generateClustersLinkage(k);
        loadClusters(model.getHardClustering(), model.getSoftClustering());
        selectedSidePanel.setValues(model.getNMIScore(), model.getClusteringTime());
    }

    public void resetView() {
        Dataset dataset = model.getDataset();
        dataVisualizer = dataset instanceof GraphDataset ? new GraphView(this) : new PlottingView(this);
        ((JPanel)dataVisualizer).setBounds(0, 0, windowWidth - sidePanelWidth, windowHeight);

        pane.removeAll();
        for (SidePanel sidePanel : sidePanels) {
            mainComponent.remove(sidePanel);
        }
        sidePanels = new ArrayList<>();

        //Add initial side tab.
        SidePanel sidePanel = dataset == null ? new SidePanel(this) : new TangleSidePanel(this);
        sidePanels.add(sidePanel);
        selectedSidePanel = sidePanel;
        pane.addTab(dataset == null ? "" : Model.tangleName, new JPanel(null));
        ((JComponent)pane.getSelectedComponent()).add((JPanel) dataVisualizer);
        mainComponent.add(sidePanel);

        if (dataset == null) {
            selectedSidePanel.setVisible(false);
        }
        else {
            //Add additional side tabs.
            if (dataset.supportsAlgorithm(Model.kMeansName)) {
                addSidePanel(new KMeansSidePanel(this), Model.kMeansName);
            }
            if (dataset.supportsAlgorithm(Model.spectralClusteringName)) {
                addSidePanel(new SpectralSidePanel(this), Model.spectralClusteringName);
            }
            if (dataset.supportsAlgorithm(Model.spectralClusteringName)) {
                addSidePanel(new LinkageSidePanel(this), Model.linkageName);
            }
        }
        ((JPanel)dataVisualizer).repaint();
    }

    protected void switchToStatistics() {
        mainComponent.remove(pane);
        mainComponent.remove(selectedSidePanel);
        mainComponent.remove(topPanel);
        mainComponent.add(statisticsPanel);
        mainComponent.add(statisticsTopPanel);
        repaint();
    }

    protected void switchToPlotting() {
        mainComponent.remove(statisticsPanel);
        mainComponent.remove(statisticsTopPanel);
        mainComponent.add(pane);
        mainComponent.add(selectedSidePanel);
        mainComponent.add(topPanel);
        repaint();
    }

    protected void createDataset(String datasetTypeName, int nPoints, int nDimensions, int nClusters) {
        controller.createNewDataset(datasetTypeName, nPoints, nDimensions, nClusters);
    }

    protected void loadDatasetFromFile(String datasetTypeName, String fileName, int startRow, int endRow, int startColumn, int endColumn) {
        controller.loadDatasetFromFile(datasetTypeName, fileName, startRow, endRow, startColumn, endColumn);
    }

    protected int getWindowHeight() {
        return windowHeight;
    }

    protected int getWindowWidth() {
        return windowWidth;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    protected double getNMIScore() {
        return model.getNMIScore();
    }

    protected long getClusteringTime() {
        return model.getClusteringTime();
    }

    protected boolean hasDataset() {
        return model.getDataset() != null;
    }

    protected Dataset getDataset() {
        return model.getDataset();
    }

    public boolean isReady() {
        return dataVisualizer.isReady();
    }
}
