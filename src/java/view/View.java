package view;

import datasets.BinaryQuestionnaire;
import datasets.Dataset;
import datasets.FeatureBasedDataset;
import datasets.GraphDataset;
import controller.Controller;
import model.Model;
import util.BitSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class View extends JFrame {

    //This class represents the GUI of the program.

    private Model model;
    protected Controller controller;
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

    //Constructor receiving a Model.
    public View(Model model) {
        this.model = model;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        windowWidth = screenSize.width - screenSize.width / 10;
        windowHeight = screenSize.height - screenSize.height / 10;

        //Create components.
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
        statisticsTopPanel = new StatisticsTopPanel(this, statisticsPanel);

        SidePanel sidePanel = new SidePanel(this);
        sidePanels.add(sidePanel);
        selectedSidePanel = sidePanel;
        pane.addTab("", new JPanel(null));
        ((JComponent)pane.getSelectedComponent()).add((JPanel)dataVisualizer);

        setBounds();

        //Add components to mainComponent.
        mainComponent.add(sidePanel);
        mainComponent.add(pane);
        mainComponent.add(topPanel);
        mainComponent.add(statisticsPanel);
        mainComponent.add(statisticsTopPanel);

        switchToPlotting();

        //Create frame
        setTitle("View");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(null);
        add(mainComponent);
        pack();
        setLocationByPlatform(true);
        setLocationRelativeTo(null);
        setVisible(true);

        //ComponentListener that resizes View and the components in View.
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

    //Sets the bounds of all the components in View.
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

    //Calls the loadPoints method of PlottingView for a feature based data set.
    public void loadPoints(double[][] points) {
        ((PlottingView)dataVisualizer).loadPoints(points);
        selectedSidePanel.update(points.length);
    }

    //Calls the loadPoints method of PlottingView for a binary data set.
    public void loadPoints(BitSet[] points) {
        ((PlottingView)dataVisualizer).loadPoints(points);
        selectedSidePanel.update(points.length);
    }

    //Calls the loadGraphFromDotString method of GraphView.
    public void loadPoints(String graphDotString) {
        ((GraphView)dataVisualizer).loadGraphFromDotString(graphDotString);
        selectedSidePanel.update(dataVisualizer.getOriginalNumberOfPoints());
    }

    //Calls the loadClusters method of the currently displayed dataVisualizer and updates necessary values in
    // the currently selected side panel.
    public void loadClusters(int[] clusters, double[][] softClustering) {
        selectedSidePanel.setClustering(clusters, softClustering);
        dataVisualizer.loadClusters(clusters, softClustering);
    }

    //Updates values in the currently selected side panel.
    public void updateSelectedSidePanel(double NMIScore, long clusteringTime) {
        selectedSidePanel.setValues(NMIScore, clusteringTime);
    }

    //Sets the data set in Model.
    protected void setDataset(Dataset dataset) {
        model.setDataset(dataset);
    }

    //Resets View and all of its components.
    public void resetView() {
        Dataset dataset = model.getDataset();
        dataVisualizer = dataset instanceof GraphDataset ? new GraphView(this) : new PlottingView(this);
        ((JPanel)dataVisualizer).setBounds(0, 0, windowWidth - sidePanelWidth, windowHeight);

        //Remove components from pane and mainComponent.
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

        //Add additional side tabs if the data set is not null.
        if (dataset == null) {
            selectedSidePanel.setVisible(false);
        }
        else {
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

        //Determine if axes and gridlines should be shown
        if (dataVisualizer instanceof PlottingView) {
            if (!topPanel.showAxesButtonIsOn()) switchShowingOfAxes();
            if (!topPanel.showGridlinesButtonIsOn()) switchShowingOfGridlines();
            topPanel.makeOnOffButtonsVisible(true);
        } else {
            topPanel.makeOnOffButtonsVisible(false);
        }

        ((JPanel)dataVisualizer).repaint();
    }

    //Switches from displaying the data visualiser to displaying the statistics panel.
    protected void switchToStatistics() {
        pane.setVisible(false);
        selectedSidePanel.setVisible(false);
        topPanel.setVisible(false);
        statisticsTopPanel.setVisible(true);
        statisticsPanel.setVisible(true);
        setBounds();
        repaint();
    }

    //Switches from displaying the statistics panel to displaying the data visualizer.
    protected void switchToPlotting() {
        pane.setVisible(true);
        selectedSidePanel.setVisible(true);
        topPanel.setVisible(true);
        statisticsTopPanel.setVisible(false);
        statisticsPanel.setVisible(false);
        setBounds();
        repaint();
    }

    //Creates a data set of type datasetTypeName with input parameters describing number of points, dimensions and clusters.
    protected void createDataset(String datasetTypeName, int nPoints, int nDimensions, int nClusters) {
        controller.createNewDataset(datasetTypeName, nPoints, nDimensions, nClusters);
    }

    //Loads a data set from a file.
    protected void loadDatasetFromFile(String datasetTypeName, String fileName, int startRow, int endRow, int startColumn, int endColumn) {
        controller.loadDatasetFromFile(datasetTypeName, fileName, startRow, endRow, startColumn, endColumn);
    }

    //Turns axes ON or OFF in the plotting view.
    protected void switchShowingOfAxes() {
        if (dataVisualizer instanceof PlottingView) {
            ((PlottingView) dataVisualizer).switchShowingOfAxes();
        }
        repaint();
    }

    //Turns grid lines ON or OFF in the plotting view.
    protected void switchShowingOfGridlines() {
        if (dataVisualizer instanceof PlottingView) {
            ((PlottingView) dataVisualizer).switchShowingOfGridlines();
        }
        repaint();
    }

    //Returns the height of View.
    protected int getWindowHeight() {
        return windowHeight;
    }

    //Returns the width of View.
    protected int getWindowWidth() {
        return windowWidth;
    }

    //Sets the controller.
    public void setController(Controller controller) {
        this.controller = controller;
    }

    //Returns the NMI score computed in Model.
    protected double getNMIScore() {
        return model.getNMIScore();
    }

    //Returns the clustering time computed in Model.
    protected long getClusteringTime() {
        return model.getClusteringTime();
    }

    //Returns the truth value describing if the data set in Model is null.
    protected boolean hasDataset() {
        return model.getDataset() != null;
    }

    //Returns the data set in Model.
    protected Dataset getDataset() {
        return model.getDataset();
    }

    //Returns true if t-SNE is not running; otherwise false.
    public boolean isReady() {
        return dataVisualizer.isReady();
    }
}
