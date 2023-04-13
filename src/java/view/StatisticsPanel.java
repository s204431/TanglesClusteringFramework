package view;

import smile.plot.swing.*;
import smile.plot.swing.Canvas;
import testsets.ClusteringTester;
import testsets.TestSet;

import javax.swing.*;
import java.awt.*;
import java.awt.Graphics;

public class StatisticsPanel extends JPanel {

    //This class visualizes the results from running a test set.

    private View view;

    protected JLabel label;

    private JPanel leftPicturePanel;
    private JPanel rightPicturePanel;

    private JLabel preRunLabel, runLabel;

    //Constructor receiving a View.
    public StatisticsPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        leftPicturePanel = new JPanel();
        rightPicturePanel = new JPanel();
        leftPicturePanel.setLayout(new BoxLayout(leftPicturePanel, BoxLayout.PAGE_AXIS));
        rightPicturePanel.setLayout(new BoxLayout(rightPicturePanel, BoxLayout.PAGE_AXIS));

        add(leftPicturePanel);
        add(Box.createRigidArea(new Dimension(view.windowWidth / 100, 0)));
        add(rightPicturePanel);

        Font font = new Font("TimesRoman", Font.BOLD, 25);
        preRunLabel = new JLabel("Please run a test set");
        runLabel = new JLabel("Running test set...");
        runLabel.setAlignmentX(CENTER_ALIGNMENT);
        preRunLabel.setAlignmentX(CENTER_ALIGNMENT);
        runLabel.setAlignmentY(TOP_ALIGNMENT);
        preRunLabel.setAlignmentY(TOP_ALIGNMENT);
        runLabel.setFont(font);
        preRunLabel.setFont(font);
        runLabel.setVisible(false);

        JPanel labelPane = new JPanel();
        labelPane.add(preRunLabel);
        labelPane.add(runLabel);
        add(labelPane);
    }

    //Creates graphs displaying the results from running a test set and adds them to the left and right picture panels.
    protected void plotTestResults(double[][][] testResults, TestSet testSet, String[] algorithmNames) {
        Color[] colors = new Color[] {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        Line[][] linesTime = new Line[3][algorithmNames.length];
        Line[][] linesNMI = new Line[3][algorithmNames.length];
        Legend[] legends = new Legend[algorithmNames.length];

        //Organize the test results.
        for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
            double[][] timePoints = new double[testResults[algorithm].length][2];
            double[][] timeDimensions = new double[testResults[algorithm].length][2];
            double[][] timeClusters = new double[testResults[algorithm].length][2];
            double[][] nmiPoints = new double[testResults[algorithm].length][2];
            double[][] nmiDimensions = new double[testResults[algorithm].length][2];
            double[][] nmiClusters = new double[testResults[algorithm].length][2];
            for (int test = 0; test < testResults[algorithm].length; test++) {
                timePoints[test][0] = testSet.get(test).nPoints;
                timePoints[test][1] = testResults[algorithm][test][0];

                timeDimensions[test][0] = testSet.get(test).nDimensions;
                timeDimensions[test][1] = testResults[algorithm][test][0];

                timeClusters[test][0] = testSet.get(test).nClusters;
                timeClusters[test][1] = testResults[algorithm][test][0];

                nmiPoints[test][0] = testSet.get(test).nPoints;
                nmiPoints[test][1] = testResults[algorithm][test][1];

                nmiDimensions[test][0] = testSet.get(test).nDimensions;
                nmiDimensions[test][1] = testResults[algorithm][test][1];

                nmiClusters[test][0] = testSet.get(test).nClusters;
                nmiClusters[test][1] = testResults[algorithm][test][1];
            }
            linesTime[0][algorithm] = Line.of(timePoints, colors[algorithm]);
            linesTime[1][algorithm] = Line.of(timeDimensions, colors[algorithm]);
            linesTime[2][algorithm] = Line.of(timeClusters, colors[algorithm]);
            linesNMI[0][algorithm] = Line.of(nmiPoints, colors[algorithm]);
            linesNMI[1][algorithm] = Line.of(nmiDimensions, colors[algorithm]);
            linesNMI[2][algorithm] = Line.of(nmiClusters, colors[algorithm]);
            legends[algorithm] = new Legend(algorithmNames[algorithm], colors[algorithm]);
        }

        //Create the line plots.
        LinePlot[] timePlots = new LinePlot[3];
        LinePlot[] nmiPlots = new LinePlot[3];

        for (int i = 0; i < 3; i++) {
            timePlots[i] = new LinePlot(linesTime[i], legends);
            nmiPlots[i] = new LinePlot(linesNMI[i], legends);
        }

        //Add axes labels, rescale the images and add the images to left and right picture panels.
        String[] xLabels = { "Points", "Dimensions", "Clusters" };
        String[] yLabels = { "Time (ms)", "NMI score" };
        int imageWidth = view.windowWidth * 16 / 33;
        int imageHeight = (view.windowHeight - view.topPanelHeight) * 2 / 7;
        Canvas[] canvases = new Canvas[6];
        Image[] images = new Image[6];
        for (int i = 0; i < 3; i++) {
            canvases[i] = timePlots[i].canvas();
            canvases[i+3] = nmiPlots[i].canvas();

            canvases[i].setAxisLabels(xLabels[i], yLabels[0]);
            canvases[i+3].setAxisLabels(xLabels[i], yLabels[1]);

            images[i] = canvases[i].toBufferedImage(imageWidth * 8 / 7, imageHeight * 8 / 7);
            images[i+3] = canvases[i+3].toBufferedImage(imageWidth * 8 / 7, imageHeight * 8 / 7);

            images[i] = images[i].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
            images[i+3] = images[i+3].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);

            leftPicturePanel.add(new JLabel(new ImageIcon(images[i])));
            rightPicturePanel.add(new JLabel(new ImageIcon(images[i+3])));

            leftPicturePanel.add(Box.createRigidArea(new Dimension(0, view.windowHeight / 100)));
            rightPicturePanel.add(Box.createRigidArea(new Dimension(0, view.windowHeight / 100)));
        }

        setVisible(false);
        setVisible(true);
    }

    //Shows text indicating that a test set is running.
    protected void startRunPhase() {
        leftPicturePanel.removeAll();
        rightPicturePanel.removeAll();
        preRunLabel.setVisible(false);
        runLabel.setVisible(true);
        runLabel.setText("Running test set... 0%");
    }

    //Paint method used to update text displaying the progress of the running test set.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (runLabel.isVisible()) {
            runLabel.setText("Running test set... " + (int)(ClusteringTester.testProgress*100)+"%");
        }
    }

    //Removes the text indicating that a test set is running.
    protected void endRunPhase() {
        runLabel.setVisible(false);
    }

    //Sets the bounds of the StatisticsPanel.
    protected void setBounds() {
        setBounds(0, view.topPanelHeight, view.windowWidth, view.windowHeight - view.topPanelHeight);
    }

}
