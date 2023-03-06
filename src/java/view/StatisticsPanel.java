package view;

import smile.plot.swing.Canvas;
import smile.plot.swing.Histogram;
import smile.plot.swing.Line;
import smile.plot.swing.LinePlot;
import test.TestSet;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StatisticsPanel extends JPanel {
    private View view;

    protected JLabel label;

    public StatisticsPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


    }

    protected void plotTestResults(double[][][] testResults, TestSet testSet, String[] algorithmNames) {
        Color[] colors = new Color[] {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        JPanel picturePanel = new JPanel();
        picturePanel.setLayout(new BoxLayout(picturePanel, BoxLayout.LINE_AXIS));
        Line[] linesTime = new Line[algorithmNames.length];
        Line[] linesNMI = new Line[algorithmNames.length];
        for (int algorithm = 0; algorithm < algorithmNames.length; algorithm++) {
            double[][] timePoints = new double[testResults[algorithm].length][2];
            double[][] nmiPoints = new double[testResults[algorithm].length][2];
            for (int test = 0; test < testResults[algorithm].length; test++) {
                timePoints[test][0] = testSet.get(test).nPoints;
                timePoints[test][1] = testResults[algorithm][test][0];
                nmiPoints[test][0] = testSet.get(test).nPoints;
                nmiPoints[test][1] = testResults[algorithm][test][1];
            }
            linesTime[algorithm] = Line.of(timePoints, colors[algorithm]);
            linesNMI[algorithm] = Line.of(nmiPoints, colors[algorithm]);
        }
        LinePlot plot = new LinePlot(linesTime);
        Image image = plot.canvas().toBufferedImage(300, 300);
        LinePlot plot2 = new LinePlot(linesNMI);
        Image image2 = plot2.canvas().toBufferedImage(300, 300);
        picturePanel.add(new JLabel(new ImageIcon(image)));
        picturePanel.add(new JLabel(new ImageIcon(image2)));
        add(picturePanel);
        setVisible(false);
        setVisible(true);
    }

    protected void setBounds() {
        setBounds(0, view.topPanelHeight, view.windowWidth, view.windowHeight - view.topPanelHeight);
    }

}
