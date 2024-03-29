package view;

import util.ValueAdjuster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SidePanel extends JPanel {

    //Responsible: Michael

    //This class represents a panel on the side of a View.

    protected View view;
    protected double[][] softClustering;
    protected int[] hardClustering;
    private double NMIScore = -1;
    private long clusteringTime = -1;

    //Components shown on every side panel.
    private JLabel pointsText = new JLabel("Total points: ");
    private JLabel pointsLabel = new JLabel();
    private JLabel showingLabel = new JLabel();
    private JLabel NMIText = new JLabel("NMI score: ");
    private JLabel NMILabel = new JLabel();
    private JLabel timeText = new JLabel("Clustering time: ");
    private JLabel timeLabel = new JLabel();
    private JCheckBox groundTruthCheckBox = new JCheckBox("Show ground truth");

    protected Font font;    //Font also used by child classes.

    //Constructor receiving a View.
    public SidePanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        font = new Font("TimesRoman", Font.BOLD, view.sidePanelWidth / 13);

        add(Box.createRigidArea(new Dimension(0, font.getSize())));

        JLabel[] allLabels = { pointsText, pointsLabel, showingLabel, new JLabel(), NMIText, NMILabel, timeText, timeLabel };

        //Add labels to side panel with spaces between.
        int c = 0;
        for (JLabel label : allLabels) {
            label.setFont(font);
            label.setAlignmentX(CENTER_ALIGNMENT);
            add(label);
            if (c++ % 2 == 1) {
                add(Box.createRigidArea(new Dimension(0, font.getSize())));
            }
        }
        groundTruthCheckBox.setAlignmentX(CENTER_ALIGNMENT);
        if (view.hasDataset() && view.getDataset().getGroundTruth() != null) {
            groundTruthCheckBox.addActionListener(e -> {
                view.repaint();
                view.dataVisualizer.showGroundTruth(groundTruthCheckBox.isSelected());
            });
            add(groundTruthCheckBox);

        }
        else {
            groundTruthCheckBox = null;
        }
        add(Box.createRigidArea(new Dimension(0, 20)));
    }

    //Draws the side panel on the screen.
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

    //Updates components in the side panel based on the total number of data points.
    protected void update(int n) {

    }

    //Adds cluster button to side panel.
    protected void addClusterButton(ValueAdjuster[] valueAdjusters) {
        add(Box.createRigidArea(new Dimension(0, 20)));
        JButton clusterButton = new JButton("Cluster");
        clusterButton.setAlignmentX(CENTER_ALIGNMENT);
        clusterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ValueAdjuster adjuster : valueAdjusters) {
                    adjuster.updateValue();
                }
            }
        });
        add(clusterButton);
    }

    //States the size and placement of SidePanel.
    protected void setBounds() {
        setBounds(view.windowWidth - view.sidePanelWidth, view.topPanelHeight, view.sidePanelWidth, view.windowHeight - view.topPanelHeight);
    }

    //Sets different values to be stored by the panel.
    public void setValues(double NMIScore, long clusteringTime) {
        this.NMIScore = NMIScore;
        this.clusteringTime = clusteringTime;
    }

    //Saves the received hard and soft clustering.
    protected void setClustering(int[] hardClustering, double[][] softClustering) {
        this.hardClustering = hardClustering;
        this.softClustering = softClustering;
    }

    //Returns whether the ground truth should be shown.
    protected boolean showGroundTruth() {
        return groundTruthCheckBox != null && groundTruthCheckBox.isSelected();
    }

}