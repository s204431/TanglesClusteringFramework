package view;

import smile.plot.swing.*;
import smile.plot.swing.Canvas;
import test.TestSet;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

public class StatisticsPanel extends JPanel {
    private View view;

    protected JLabel label;

    private JPanel leftPicturePanel;
    private JPanel rightPicturePanel;

    public StatisticsPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        leftPicturePanel = new JPanel();
        leftPicturePanel.setLayout(new BoxLayout(leftPicturePanel, BoxLayout.PAGE_AXIS));

        rightPicturePanel = new JPanel();
        rightPicturePanel.setLayout(new BoxLayout(rightPicturePanel, BoxLayout.PAGE_AXIS));

    }

    protected void plotTestResults(double[][][] testResults, TestSet testSet, String[] algorithmNames) {
        leftPicturePanel.removeAll();
        rightPicturePanel.removeAll();

        Color[] colors = new Color[] {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        Line[][] linesTime = new Line[3][algorithmNames.length];
        Line[][] linesNMI = new Line[3][algorithmNames.length];
        Legend[] legends = new Legend[algorithmNames.length];
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

                timeClusters[test][0] = testSet.get(test).nDimensions;
                timeClusters[test][1] = testResults[algorithm][test][0];

                nmiPoints[test][0] = testSet.get(test).nPoints;
                nmiPoints[test][1] = testResults[algorithm][test][1];

                nmiDimensions[test][0] = testSet.get(test).nPoints;
                nmiDimensions[test][1] = testResults[algorithm][test][1];

                nmiClusters[test][0] = testSet.get(test).nPoints;
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

        LinePlot[] timePlots = new LinePlot[3];
        LinePlot[] nmiPlots = new LinePlot[3];
        String[] xLabels = { "Points", "Dimensions", "Clusters" };
        String[] yLabels = { "Time (ms)", "NMI score" };
        for (int i = 0; i < 3; i++) {
            timePlots[i] = new LinePlot(linesTime[i], legends);
            nmiPlots[i] = new LinePlot(linesNMI[i], legends);
        }

        int imageWidth = view.windowWidth * 16 / 33;
        int imageHeight = (view.windowHeight - view.topPanelHeight) * 2 / 7;
        Canvas[] canvases = new Canvas[6];
        Image[] images = new Image[6];
        for (int i = 0; i < 3; i++) {
            canvases[i] = timePlots[i].canvas();
            canvases[i+3] = nmiPlots[i].canvas();

            canvases[i].setAxisLabels(xLabels[i], yLabels[0]);
            canvases[i+3].setAxisLabels(xLabels[i], yLabels[1]);

            images[i] = canvases[i].toBufferedImage(imageWidth, imageHeight);
            images[i+3] = canvases[i+3].toBufferedImage(imageWidth, imageHeight);

            rightPicturePanel.add(new JLabel(new ImageIcon(images[i])));
            leftPicturePanel.add(new JLabel(new ImageIcon(images[i+3])));
        }

        add(rightPicturePanel);
        add(Box.createRigidArea(new Dimension(view.windowWidth / 100, 0)));
        add(leftPicturePanel);
        setVisible(false);
        setVisible(true);
    }

    protected void setBounds() {
        setBounds(0, view.topPanelHeight, view.windowWidth, view.windowHeight - view.topPanelHeight);
    }

}
