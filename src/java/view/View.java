package view;

import model.Model;
import util.BitSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class View extends JFrame {
    private Model model;

    protected int windowWidth, windowHeight;

    protected JPanel mainComponent;
    protected PlottingView plottingView;
    private JTabbedPane pane;
    private List<SidePanel> sidePanels = new ArrayList<>();
    private SidePanel selectedSidePanel;
    private TopPanel topPanel;

    protected int topPanelHeight;
    protected int sidePanelWidth;

    public View() {
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

        plottingView = new PlottingView(this);
        pane = new JTabbedPane();
        pane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ((JComponent)((JTabbedPane) e.getSource()).getSelectedComponent()).add(plottingView);
                changeSidePanel(((JTabbedPane) e.getSource()).getSelectedIndex());
            }
        });
        //Add initial side tab.
        SidePanel sidePanel = new TangleSidePanel(this);
        sidePanels.add(sidePanel);
        selectedSidePanel = sidePanel;
        pane.addTab("Tangle", new JPanel(null));
        ((JComponent)pane.getSelectedComponent()).add(plottingView);

        addSidePanel(new KMeansSidePanel(this), "K-Means");

        topPanel = new TopPanel(this);

        setBounds();

        mainComponent.add(sidePanel);
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
        selectedSidePanel.update(plottingView.originalNumberOfPoints);
        loadClusters(selectedSidePanel.hardClustering, selectedSidePanel.softClustering);
        plottingView.repaint();
    }

    private void setBounds() {
        topPanelHeight = windowHeight / 20;
        sidePanelWidth = windowWidth / 8;

        topPanelHeight = Math.max(topPanelHeight, 30);
        sidePanelWidth = Math.max(sidePanelWidth, 200);

        mainComponent.setPreferredSize(new Dimension(windowWidth, windowHeight));
        mainComponent.setBounds(0, 0, windowWidth, windowHeight);
        plottingView.setBounds(0, 0, windowWidth - sidePanelWidth, windowHeight);
        pane.setBounds(0, topPanelHeight, windowWidth - sidePanelWidth, windowHeight);
        selectedSidePanel.setBounds();
        topPanel.setBounds();
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters, double[][] softClustering) {
        plottingView.loadPoints(points);
        plottingView.loadClusters(clusters, softClustering);
        selectedSidePanel.update(points.length);
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters) {
        plottingView.loadPoints(points);
        plottingView.loadClusters(clusters);
        selectedSidePanel.update(points.length);
    }

    public void loadPointsWithClustering(BitSet[] questionnaireAnswers, int[] clusters, double[][] softClustering) {
        plottingView.loadPoints(questionnaireAnswers);
        plottingView.loadClusters(clusters, softClustering);
        selectedSidePanel.update(questionnaireAnswers.length);
    }

    public void loadPointsWithClustering(BitSet[] questionnaireAnswers, int[] clusters) {
        plottingView.loadPoints(questionnaireAnswers);
        plottingView.loadClusters(clusters);
        selectedSidePanel.update(questionnaireAnswers.length);
    }

    public void loadPoints(double[][] points) {
        plottingView.loadPoints(points);
        selectedSidePanel.update(points.length);
    }

    public void loadPoints(BitSet[] points) {
        plottingView.loadPoints(points);
        selectedSidePanel.update(points.length);
    }

    public void loadClusters(int[] clusters, double[][] softClustering) {
        selectedSidePanel.hardClustering = clusters;
        selectedSidePanel.softClustering = softClustering;
        plottingView.loadClusters(clusters, softClustering);
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

    protected int getWindowHeight() {
        return windowHeight;
    }

    protected int getWindowWidth() {
        return windowWidth;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    protected double getNMIScore() {
        return model.getNMIScore();
    }

    protected long getClusteringTime() {
        return model.getClusteringTime();
    }
}
