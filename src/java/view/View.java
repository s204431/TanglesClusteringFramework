package view;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;
import util.BitSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
        plottingView.loadPoints(points);
        plottingView.loadClusters(clusters, softClustering);
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters) {
        plottingView.loadPoints(points);
        plottingView.loadClusters(clusters);
    }

    public void loadPointsWithClustering(BitSet[] questionnaireAnswers, int[] clusters, double[][] softClustering) {
        plottingView.loadPoints(questionnaireAnswers);
        plottingView.loadClusters(clusters, softClustering);
    }

    public void loadPointsWithClustering(BitSet[] questionnaireAnswers, int[] clusters) {
        plottingView.loadPoints(questionnaireAnswers);
        plottingView.loadClusters(clusters);
    }

    public void loadPoints(double[][] points) {
        plottingView.loadPoints(points);
    }

    protected int getWindowHeight() {
        return windowHeight;
    }

    protected int getWindowWidth() {
        return windowWidth;
    }
}
