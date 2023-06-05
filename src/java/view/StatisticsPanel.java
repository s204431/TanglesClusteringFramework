package view;

import smile.plot.swing.*;
import smile.plot.swing.Canvas;
import smile.plot.swing.Point;
import testsets.ClusteringTester;
import testsets.TestSet;

import javax.swing.*;
import java.awt.*;
import java.awt.Graphics;

public class StatisticsPanel extends JPanel {

    //Responsible: Michael

    //This class visualizes the results from running a test set.

    private View view;

    protected JLabel label;

    private JPanel leftPicturePanel;
    private JPanel rightPicturePanel;

    private JLabel preRunLabel, runLabel;

    private double[][][] testResults;
    private TestSet testSet;
    private String[] algorithmNames;

    private boolean logarithmicScale = false;

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

    //Creates graphs displaying the results from an already run test set and adds them to the left and right picture panels.
    protected void plotTestResults() {
        if (this.testResults == null || this.testSet == null || this.algorithmNames == null || runLabel.isVisible()) {
            return;
        }
        startRunPhase();
        plotTestResults(testResults, testSet, algorithmNames);
        endRunPhase();
    }

    //Creates graphs displaying the results from running a test set and adds them to the left and right picture panels.
    protected void plotTestResults(double[][][] testResults, TestSet testSet, String[] algorithmNames) {
        this.testResults = testResults;
        this.testSet = testSet;
        this.algorithmNames = algorithmNames;

        Color[] colors = new Color[] {Color.RED, Color.BLUE, new Color(1, 150, 30) /* Darker green */, new Color(255, 140, 0) /* Darker orange */};
        Line[][] linesTime = new Line[3][algorithmNames.length];
        Line[][] linesNMI = new Line[3][algorithmNames.length];
        Legend[] legends = new Legend[algorithmNames.length];
        Point[][] timeScatterPoints = new Point[3][algorithmNames.length];
        Point[][] nmiScatterPoints = new Point[3][algorithmNames.length];

        //Organize the test results.
        for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
            double[][] timePoints = new double[testResults[algorithm].length][2];
            double[][] timeDimensions = new double[testResults[algorithm].length][2];
            double[][] timeClusters = new double[testResults[algorithm].length][2];

            double[][] nmiPoints = new double[testResults[algorithm].length][2];
            double[][] nmiDimensions = new double[testResults[algorithm].length][2];
            double[][] nmiClusters = new double[testResults[algorithm].length][2];

            for (int test = 0; test < testResults[algorithm].length; test++) {
                //Time values
                timePoints[test][0] = testSet.get(test).nPoints;
                timePoints[test][1] = testResults[algorithm][test][0];
                timeDimensions[test][0] = testSet.get(test).nDimensions;
                timeDimensions[test][1] = testResults[algorithm][test][0];
                timeClusters[test][0] = testSet.get(test).nClusters;
                timeClusters[test][1] = testResults[algorithm][test][0];

                //NMI values
                nmiPoints[test][0] = testSet.get(test).nPoints;
                nmiPoints[test][1] = testResults[algorithm][test][1];
                nmiDimensions[test][0] = testSet.get(test).nDimensions;
                nmiDimensions[test][1] = testResults[algorithm][test][1];
                nmiClusters[test][0] = testSet.get(test).nClusters;
                nmiClusters[test][1] = testResults[algorithm][test][1];
            }

            //Sort test results
            mergeSort(timePoints);
            mergeSort(timeDimensions);
            mergeSort(timeClusters);
            mergeSort(nmiPoints);
            mergeSort(nmiDimensions);
            mergeSort(nmiClusters);

            //Converts the test results into logarithmic scale if logarithmicScale is true.
            if (logarithmicScale) {
                for (int test = 0; test < testResults[algorithm].length; test++) {
                    timePoints[test][0] = timePoints[test][0] == 0 ? 0 : Math.log10(timePoints[test][0]);
                    timePoints[test][1] = timePoints[test][1] == 0 ? 0 : Math.log10(timePoints[test][1]);
                    nmiPoints[test][0] = nmiPoints[test][0] == 0 ? 0 : Math.log10(nmiPoints[test][0]);
                }
            }

            //Create lines
            linesTime[0][algorithm] = Line.of(timePoints, colors[algorithm]);
            linesTime[1][algorithm] = Line.of(timeDimensions, colors[algorithm]);
            linesTime[2][algorithm] = Line.of(timeClusters, colors[algorithm]);
            linesNMI[0][algorithm] = Line.of(nmiPoints, colors[algorithm]);
            linesNMI[1][algorithm] = Line.of(nmiDimensions, colors[algorithm]);
            linesNMI[2][algorithm] = Line.of(nmiClusters, colors[algorithm]);
            legends[algorithm] = new Legend(algorithmNames[algorithm], colors[algorithm]);

            //Create scatter points (used for edge case with only 1 test case).
            timeScatterPoints[0][algorithm] = new Point(timePoints, '#', colors[algorithm]);
            timeScatterPoints[1][algorithm] = new Point(timeDimensions, '#', colors[algorithm]);
            timeScatterPoints[2][algorithm] = new Point(timeClusters, '#', colors[algorithm]);
            nmiScatterPoints[0][algorithm] = new Point(nmiPoints, '#', colors[algorithm]);
            nmiScatterPoints[1][algorithm] = new Point(nmiDimensions, '#', colors[algorithm]);
            nmiScatterPoints[2][algorithm] = new Point(nmiClusters, '#', colors[algorithm]);
        }

        //Create the plots.
        LinePlot[] timePlots = new LinePlot[3];
        LinePlot[] nmiPlots = new LinePlot[3];
        ScatterPlot[] timeScatterPlots = new ScatterPlot[3];
        ScatterPlot[] nmiScatterPlots = new ScatterPlot[3];
        for (int i = 0; i < 3; i++) {
            timePlots[i] = new LinePlot(linesTime[i], legends);
            nmiPlots[i] = new LinePlot(linesNMI[i], legends);
            timeScatterPlots[i] = new ScatterPlot(timeScatterPoints[i], legends);
            nmiScatterPlots[i] = new ScatterPlot(nmiScatterPoints[i], legends);
        }

        //Find min and max value of test results.
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        for (int i = 0; i < algorithmNames.length; i++) {
            for (int j = 0; j < testResults[i].length; j++) {
                double temp = testResults[i][j][0] == 0 ? 0 : Math.log10(testResults[i][j][0]);
                min = Math.min(temp, min);
                max = Math.max(temp, max);
            }
        }

        //Find rounded gap between the slices.
        int slices = timePlots[0].canvas().getAxis(1).slices();
        double gap = (max - min) / slices;
        int digits;
        if (gap >= 1) {
            digits = String.valueOf((int)gap).length() - 1;
        } else {
            digits = -(int)Math.ceil(Math.abs(Math.log10(gap)));
        }
        double x = gap / (Math.pow(10, digits));
        double roundedGap = Math.ceil(x) * (Math.pow(10, digits));
        double startValue = (int)(min / roundedGap) * roundedGap;

        //Prepare tick labels and their locations on the graph.
        String[][] xTicks = new String[3][testSet.size()];
        double[][] xLocations = new double[3][testSet.size()];
        for (int i = 0; i < testSet.size(); i++) {
            xTicks[0][i] = ""+testSet.get(i).nPoints;
            xTicks[1][i] = ""+testSet.get(i).nDimensions;
            xTicks[2][i] = ""+testSet.get(i).nClusters;
            xLocations[0][i] = logarithmicScale ? Math.log10(testSet.get(i).nPoints) : testSet.get(i).nPoints;
            xLocations[1][i] = testSet.get(i).nDimensions;
            xLocations[2][i] = testSet.get(i).nClusters;
        }

        String[] yTicks = new String[slices];
        double[] yLocations = new double[slices];
        if (min == max) {
            yTicks = new String[] { ""+Math.pow(10, min), ""+Math.pow(10, max) };
            yLocations = new double[] { min, max };
        } else {
            for (int i = 0; i < slices; i++) {
                double tempValue = startValue + i * roundedGap;
                yTicks[i] = "" + (int)(Math.pow(10, tempValue));
                yLocations[i] = tempValue;
            }
        }

        //Edge case with only one test case.
        if (testSet.size() == 1) {
            for (int i = 0; i < 3; i++) {
                xTicks[i] = new String[] { xTicks[i][0], xTicks[i][0] };
                xLocations[i] = new double[] { xLocations[i][0], xLocations[i][0] };
            }
            yTicks = new String[] { yTicks[0], yTicks[0] };
            yLocations = new double[] { yLocations[0], yLocations[0] };
        }

        //Add axes labels, tick labels, rescale the images and add the images to left and right picture panels.
        String[] xLabels = { "Points", "Dimensions", "Clusters" };
        String[] yLabels = { "Time (ms)", "NMI score" };
        int imageWidth = view.windowWidth * 16 / 33;
        int imageHeight = (view.windowHeight - view.topPanelHeight) * 2 / 7;
        Canvas[] canvases = new Canvas[6];
        Image[] images = new Image[6];


        for (int i = 0; i < 3; i++) {
            if (testSet.size() == 1) {  //Edge case with only 1 test case
                canvases[i] = timeScatterPlots[i].canvas();
                canvases[i + 3] = nmiScatterPlots[i].canvas();
            } else {
                canvases[i] = timePlots[i].canvas();
                canvases[i + 3] = nmiPlots[i].canvas();
            }

            //Set axis labels
            canvases[i].setAxisLabels(xLabels[i], yLabels[0]);
            canvases[i+3].setAxisLabels(xLabels[i], yLabels[1]);

            //Set tick labels
            canvases[i].getAxis(0).setTicks(xTicks[i], xLocations[i]);
            canvases[i + 3].getAxis(0).setTicks(xTicks[i], xLocations[i]);
            if (logarithmicScale && i == 0) {
                canvases[i].getAxis(1).setTicks(yTicks, yLocations);
            }

            //Convert to images
            images[i] = canvases[i].toBufferedImage(imageWidth * 8 / 7, imageHeight * 8 / 7);
            images[i+3] = canvases[i+3].toBufferedImage(imageWidth * 8 / 7, imageHeight * 8 / 7);

            //Scale images
            images[i] = images[i].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
            images[i+3] = images[i+3].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);

            //Add images to left and right picture panel
            leftPicturePanel.add(new JLabel(new ImageIcon(images[i])));
            rightPicturePanel.add(new JLabel(new ImageIcon(images[i+3])));

            leftPicturePanel.add(Box.createRigidArea(new Dimension(0, view.windowHeight / 100)));
            rightPicturePanel.add(Box.createRigidArea(new Dimension(0, view.windowHeight / 100)));
        }

        setVisible(false);
        setVisible(true);
    }

    //Sorts a two-dimensional double array based on the first column.
    public static void mergeSort(double[][] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }

        //Split array.
        int mid = arr.length / 2;
        double[][] left = new double[mid][];
        double[][] right = new double[arr.length - mid][];
        System.arraycopy(arr, 0, left, 0, mid);
        System.arraycopy(arr, mid, right, 0, arr.length - mid);

        //Recursively sort left and right array and merge them.
        mergeSort(left);
        mergeSort(right);
        merge(left, right, arr);
    }

    //Merges left and right array into arr.
    public static void merge(double[][] left, double[][] right, double[][] arr) {
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < left.length && j < right.length) {
            if (left[i][0] < right[j][0]) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
            }
        }
        while (i < left.length) {
            arr[k++] = left[i++];
        }
        while (j < right.length) {
            arr[k++] = right[j++];
        }
    }

    //Switches between logarithmic scale and non-logarithmic scale in graphs.
    protected void switchLogarithmicScale() {
        logarithmicScale = !logarithmicScale;
        startRunPhase();
        plotTestResults(testResults, testSet, algorithmNames);
        endRunPhase();
    }

    //Rounds double to precision decimal places.
    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
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
