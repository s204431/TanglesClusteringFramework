package view;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {
    protected View view;
    protected double[][] softClustering;
    protected int[] hardClustering;
    private double NMIScore = -1;
    private long clusteringTime = -1;

    private JLabel pointsText = new JLabel("Total points: ");
    private JLabel pointsLabel = new JLabel();
    private JLabel showingLabel = new JLabel();
    private JLabel NMIText = new JLabel("NMI score: ");
    private JLabel NMILabel = new JLabel();
    private JLabel timeText = new JLabel("Clustering time: ");
    private JLabel timeLabel = new JLabel();

    protected Font font;

    public SidePanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        font = new Font("TimesRoman", Font.BOLD, view.sidePanelWidth / 13);

        add(Box.createRigidArea(new Dimension(0, font.getSize())));

        JLabel[] allLabels = { pointsText, pointsLabel, showingLabel, new JLabel(), NMIText, NMILabel, timeText, timeLabel };

        int c = 0;
        for (JLabel label : allLabels) {
            label.setFont(font);
            label.setAlignmentX(CENTER_ALIGNMENT);
            add(label);
            if (c++ % 2 == 1) {
                add(Box.createRigidArea(new Dimension(0, font.getSize())));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (view.getDataset() == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        pointsLabel.setText(""+view.dataVisualizer.getOriginalNumberOfPoints());

        if (view.dataVisualizer.getOriginalNumberOfPoints() != view.dataVisualizer.getNumberOfPoints()) {
            showingLabel.setText("Showing "+view.dataVisualizer.getNumberOfPoints());
        } else {
            showingLabel.setText(" ");
        }

        if (NMIScore >= 0) {
            NMILabel.setText(""+((int)(NMIScore*100000)/100000.0));
        } else {
            NMILabel.setText("None");
        }

        if (clusteringTime >= 0) {
            timeLabel.setText(clusteringTime + " ms");
        } else {
            timeLabel.setText("None");
        }
    }

    //Called when a value in the panel has changed.
    protected void valueChanged() {

    }

    //Updates the total number of data points.
    protected void update(int n) {

    }

    protected void setBounds() {
        setBounds(view.windowWidth - view.sidePanelWidth, view.topPanelHeight, view.sidePanelWidth, view.windowHeight - view.topPanelHeight);
    }

    //Sets different values to be stored by the panel.
    public void setValues(double NMIScore, long clusteringTime) {
        this.NMIScore = NMIScore;
        this.clusteringTime = clusteringTime;
    }

    protected void setClustering(int[] hardClustering, double[][] softClustering) {
        this.hardClustering = hardClustering;
        this.softClustering = softClustering;
    }

}