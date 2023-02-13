package view;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;
import model.Model;
import util.BitSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class View extends JFrame {
    private Model model;

    private int windowWidth, windowHeight;

    private JPanel mainComponent;
    private PlottingView plottingView;
    private SidePanel sidePanel;
    private TopPanel topPanel;

    private int topPanelHeight;
    private int sidePanelWidth;

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
        mainComponent.setLayout(null);

        plottingView = new PlottingView(this);
        sidePanel = new SidePanel(this);
        topPanel = new TopPanel(this);

        setBounds();

        mainComponent.add(plottingView);
        mainComponent.add(sidePanel);
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

    private void setBounds() {
        topPanelHeight = windowHeight / 15;
        sidePanelWidth = windowWidth / 8;

        topPanelHeight = topPanelHeight < 50 ? 50 : topPanelHeight;
        sidePanelWidth = sidePanelWidth < 200 ? 200 : sidePanelWidth;

        mainComponent.setPreferredSize(new Dimension(windowWidth, windowHeight));
        mainComponent.setBounds(0, 0, windowWidth, windowHeight);
        plottingView.setBounds(0, topPanelHeight, windowWidth - sidePanelWidth, windowHeight);
        sidePanel.setBounds(windowWidth - sidePanelWidth, topPanelHeight, windowWidth, windowHeight);
        topPanel.setBounds(0, 0, windowWidth, topPanelHeight);
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters, double[][] softClustering) {
        plottingView.loadPoints(points);
        plottingView.loadClusters(clusters, softClustering);
        sidePanel.update(points.length);
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters) {
        plottingView.loadPoints(points);
        plottingView.loadClusters(clusters);
        sidePanel.update(points.length);
    }

    public void loadPointsWithClustering(BitSet[] questionnaireAnswers, int[] clusters, double[][] softClustering) {
        plottingView.loadPoints(questionnaireAnswers);
        plottingView.loadClusters(clusters, softClustering);
        sidePanel.update(questionnaireAnswers.length);
    }

    public void loadPointsWithClustering(BitSet[] questionnaireAnswers, int[] clusters) {
        plottingView.loadPoints(questionnaireAnswers);
        plottingView.loadClusters(clusters);
        sidePanel.update(questionnaireAnswers.length);
    }

    public void loadPoints(double[][] points) {
        plottingView.loadPoints(points);
        sidePanel.update(points.length);
    }

    public void loadClusters(int[] clusters, double[][] softClustering) {
        plottingView.loadClusters(clusters, softClustering);
    }

    public void updateAValue(int a) {
        sidePanel.updateAValue(a);
    }

    protected void changeAValue(int a) {
        model.regenerateClusters(a);
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
}
