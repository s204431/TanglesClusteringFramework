package view;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;
import datasets.FeatureBasedDataset;
import util.BitSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.List;

public class PlottingView extends JPanel implements DataVisualizer, MouseListener, MouseMotionListener, MouseWheelListener {

    //Responsible: Michael

    //This class visualizes a point-based data set in a two-dimensional coordinate system.

    public static final String name = "Plotting View";

    private static final int POINT_SIZE = 8;
    private static final int MAX_POINTS_TO_DRAW = 10000;
    private static final int MAX_POINTS_TO_DRAW_WHEN_MOVING = 2000;

    private View view;

    private double[][] points;
    private int[] clusters;
    private double[][] softClustering;
    private Color[] colors;

    private int windowMax;

    private boolean close = false;  //Determines if threads in this class needs to be stopped.

    //Constants used when dragging or zooming.
    private int[] mouseOrigVector;
    private boolean dragging = false;
    private int zoomTimer = 0;
    private int xOrig;
    private int yOrig;
    private double factor = 1; //Used to expand x- or y-axis to capture largest datapoint.
    private int zoomFactor = 1;
    private int lineGap;

    protected JLabel coordinates = new JLabel();

    //Often used strokes.
    private final BasicStroke stroke1 = new BasicStroke(1);
    private final BasicStroke stroke2 = new BasicStroke(2);
    private final BasicStroke stroke3 = new BasicStroke(3);

    private int[] indicesToDraw; //Indices of points to draw when there are too many points.
    protected int originalNumberOfPoints; //Number of points before reducing the number of points.
    protected boolean runningTSNE = false;
    protected boolean showAxes = true;
    protected boolean showGridLines = true;

    //Constructor receiving view.
    public PlottingView(View view) {
        super();
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        xOrig = (int)(view.getWindowWidth() * 0.5);
        yOrig = (int)(view.getWindowHeight() * 0.5);

        coordinates.setBounds(5, 5, 150, 30);
        coordinates.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        add(coordinates);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        (new Thread(new BoardDragger())).start();
        (new Thread(new ZoomTimer())).start();
    }

    //Draws plottingView on screen.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        //Return if no data set has been loaded.
        if (!view.hasDataset()) {
            g2d.setFont(getFont().deriveFont(20f));
            g2d.drawString("Please load or generate a dataset", getWidth()/4, getHeight()/3);
            return;
        }

        //Draws string if TSNE is running.
        if (runningTSNE) {
            g2d.setColor(Color.BLACK);
            int textHeight = 35;
            g2d.setFont(getFont().deriveFont((float)textHeight));
            String text = "Converting data points to 2D...";
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, (view.windowWidth - view.sidePanelWidth) / 2 - textWidth / 2, (view.windowHeight - view.topPanelHeight) / 2 - textHeight);
            return;
        }

        //Choose whether to show ground truth or generated clusters.
        int[] clusters;// = view.selectedSidePanel.showGroundTruth() ? view.getDataset().getGroundTruth() : this.clusters;
        double[][] softClustering;

        if (view.selectedSidePanel.showGroundTruth()) {
            clusters = view.getDataset().getGroundTruth();
            clusters = convertClusters(clusters);
            softClustering = null;
        }
        else {
            clusters = this.clusters;
            softClustering = this.softClustering;
        }

        //Update necessary values.
        windowMax = Math.max(view.getWindowHeight(), view.getWindowWidth());
        if (lineGap == 0) {
            lineGap = windowMax / 30;
        }
        int lines = (windowMax + windowMax * zoomFactor * 4) / lineGap;

        //Draw axes.
        if (showAxes) {
            g2d.setStroke(stroke3);
            g2d.setColor(Color.BLACK);
            g2d.drawLine(0, yOrig, view.windowWidth, yOrig);
            g2d.drawLine(xOrig, 0, xOrig, view.windowHeight);
        }

        //Draw lines, grid lines and numbers on axes.
        int lineSize1 = windowMax / 150;
        int lineSize2 = windowMax / 75;
        int fontSize = windowMax / 70;
        g2d.setFont(new Font("TimesRoman", Font.BOLD, Math.max(fontSize, 12)));
        g2d.setStroke(stroke2);
        int lineSize;
        boolean drawNumber;
        for (int i = showAxes ? 1 : 0; i < lines; i++) {
            if (i % 5 == 0) {
                g2d.setStroke(stroke3);
                lineSize = lineSize2;
                drawNumber = true;
            } else {
                g2d.setStroke(stroke2);
                lineSize = lineSize1;
                drawNumber = false;
            }

            int posX = xOrig + lineGap * i;
            int posY = yOrig + lineGap * i;
            int negX = xOrig - lineGap * i;
            int negY = yOrig - lineGap * i;

            //Draw gridlines.
            g2d.setStroke(stroke1);
            if (showGridLines) {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(posX, 0, posX, view.windowHeight); //Positive direction on x-axis
                g2d.drawLine(0, posY, view.windowWidth, posY); //Positive direction on y-axis
                g2d.drawLine(negX, 0, negX, view.windowHeight); //Negative direction on x-axis
                g2d.drawLine(0, negY, view.windowWidth, negY); //Negative direction on y-axis
            }

            //Draw lines on axes.
            if (showAxes) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(posX, yOrig - lineSize, posX, yOrig + lineSize); //Positive direction on x-axis
                g2d.drawLine(xOrig - lineSize, posY, xOrig + lineSize, posY); //Positive direction on y-axis
                g2d.drawLine(negX, yOrig - lineSize, negX, yOrig + lineSize); //Negative direction on x-axis
                g2d.drawLine(xOrig - lineSize, negY, xOrig + lineSize, negY); //Negative direction on y-axis

                //Draw numbers on axes.
                if (drawNumber) {
                    double num = i * factor;
                    String posText;
                    String negText;
                    if (num < 10) {
                        posText = "" + round(num, 2);
                        negText = "-" + round(num, 2);
                    } else if (num > 9999) {
                        posText = "" + convertNumberToScientificNotation(num);
                        negText = "-" + convertNumberToScientificNotation(num);
                    } else {
                        posText = "" + (int) (num);
                        negText = "-" + (int) (num);
                    }
                    int fontHeight = g2d.getFontMetrics().getHeight();
                    int fontWidth = g2d.getFontMetrics().stringWidth(posText);
                    g2d.drawString(posText, posX - fontWidth / 2, yOrig + lineSize + fontHeight); //Positive direction on x-axis
                    g2d.drawString(posText, xOrig - lineSize - fontWidth * 3 / 2, negY + fontHeight / 4); //Positive direction on y-axis

                    fontWidth = g2d.getFontMetrics().stringWidth(negText);
                    g2d.drawString(negText, negX - fontWidth / 2, yOrig + lineSize + fontHeight); //Negative direction on x-axis
                    g2d.drawString(negText, xOrig - lineSize - fontWidth * 3 / 2, posY + fontHeight / 4); //Negative direction on y-axis
                }
            }
        }

        //Plot points.
        if (points != null) {
            int bound = MAX_POINTS_TO_DRAW_WHEN_MOVING < points.length && (dragging || zoomTimer > 0) ? MAX_POINTS_TO_DRAW_WHEN_MOVING : points.length;
            for (int i = 0; i < bound; i++) {
                int[] coor = convertPointToCoordinateOnScreen(points[i]);
                if (clusters != null && clusters[i] < colors.length && colors[clusters[i]] != null) {
                    Color c = colors[clusters[i]];
                    if (softClustering != null) {
                        c = changeTranslucencyOfColor(c, softClustering[i][clusters[i]]);
                    }
                    g2d.setColor(c);
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.fillOval(coor[0], coor[1], POINT_SIZE, POINT_SIZE);
                g2d.setColor(new Color(0,0,0,50));
                g2d.drawOval(coor[0], coor[1], POINT_SIZE, POINT_SIZE);
            }
        }

        //Box behind coordinate text.
        if (coordinates.isVisible()) {
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRect(5, 5, 150, 30);
            updateCoordinateText();
        }

        //Show cuts.
        if (points != null && view.selectedSidePanel instanceof TangleSidePanel && (((TangleSidePanel) view.selectedSidePanel).showHorizontalCuts() || ((TangleSidePanel) view.selectedSidePanel).showVerticalCuts())) {
            FeatureBasedDataset dataset = (FeatureBasedDataset) view.getDataset();
            if (dataset.axisParallelCuts != null && dataset.cutCosts != null) {
                double lowestCost = Double.MAX_VALUE;
                double highestCost = Double.MIN_VALUE;
                for (int i = 0; i < dataset.cutCosts.length; i++) {
                    if (dataset.cutCosts[i] < lowestCost) {
                        lowestCost = dataset.cutCosts[i];
                    }
                    if (dataset.cutCosts[i] > highestCost) {
                        highestCost = dataset.cutCosts[i];
                    }
                }

                //Draw cuts.
                int nSegments = 20;
                int costIndex = 0;
                for (int i = 0; i < dataset.axisParallelCuts.length; i++) {
                    if ((i == 0 && !((TangleSidePanel) view.selectedSidePanel).showVerticalCuts()) || (i == 1 && !((TangleSidePanel) view.selectedSidePanel).showHorizontalCuts())) {
                        costIndex += dataset.axisParallelCuts[i].length;
                        continue;
                    }
                    int otherDimension = i == 0 ? 1 : 0;
                    double[] minPoint = new double[2];
                    double[] maxPoint = new double[2];
                    minPoint[otherDimension] = Integer.MAX_VALUE;
                    maxPoint[otherDimension] = Integer.MIN_VALUE;
                    for (int j = 0; j < points.length; j++) {
                        if (points[j][otherDimension] < minPoint[otherDimension]) {
                            minPoint = points[j];
                        }
                        if (points[j][otherDimension] > maxPoint[otherDimension]) {
                            maxPoint = points[j];
                        }
                    }
                    for (int j = 0; j < dataset.axisParallelCuts[i].length; j++) {
                        g2d.setStroke(stroke2);
                        int rank = 0;
                        for (int k = 0; k < dataset.cutCosts.length; k++) {
                            if (costIndex != k && dataset.cutCosts[costIndex] > dataset.cutCosts[k]) {
                                rank++;
                            }
                        }
                        double p = (double)rank/(dataset.cutCosts.length-1);
                        g2d.setColor(p <= 0.5 ? new Color((int)(p*2.0*255), 0, 0) : new Color(255, (int)((p-0.5)*2.0*200), (int)((p-0.5)*2.0*200)));
                        if (((FeatureBasedDataset) view.getDataset()).cutsAreAxisParallel) {
                            if (i == 0) {
                                double pos = convertPointToCoordinateOnScreen(new double[] {dataset.axisParallelCuts[i][j], 0})[0];
                                g2d.drawLine((int) pos+POINT_SIZE/2, 0, (int) pos+POINT_SIZE/2, view.getWindowHeight());
                            }
                            else {
                                double pos = convertPointToCoordinateOnScreen(new double[] {0, dataset.axisParallelCuts[i][j]})[1];
                                g2d.drawLine(0, (int) pos+POINT_SIZE/2, view.getWindowWidth(), (int) pos+POINT_SIZE/2);
                            }
                        }
                        else {
                            double[][] segmentMax = new double[nSegments][];
                            double[][] segmentMin = new double[nSegments][];
                            for (int k = 0; k < segmentMax.length; k++) {
                                segmentMax[k] = null;
                                segmentMin[k] = null;
                            }
                            for (int k = 0; k < points.length; k++) {
                                int segment = (int)((points[k][otherDimension] - minPoint[otherDimension])/((maxPoint[otherDimension] - minPoint[otherDimension] + 1)/nSegments));
                                if (!dataset.initialCuts[costIndex].get(indicesToDraw[k]) && (segmentMax[segment] == null || points[k][i] > segmentMax[segment][i])) {
                                    segmentMax[segment] = points[k];
                                }
                                if (dataset.initialCuts[costIndex].get(indicesToDraw[k]) && (segmentMin[segment] == null || points[k][i] < segmentMin[segment][i])) {
                                    segmentMin[segment] = points[k];
                                }
                            }
                            List<double[]> segmentPoints = new ArrayList<>();
                            segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : minPoint[0]-(maxPoint[0]-minPoint[0]), i == 1 ? dataset.axisParallelCuts[1][j] : minPoint[1]-(maxPoint[1]-minPoint[1])});
                            segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : minPoint[0], i == 1 ? dataset.axisParallelCuts[1][j] : minPoint[1]});
                            for (int k = 0; k < segmentMax.length; k++) {
                                if (segmentMin[k] == null && segmentMax[k] != null) {
                                    if (dataset.axisParallelCuts[i][j] < segmentMax[k][i]) {
                                        segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : segmentMax[k][0], i == 1 ? dataset.axisParallelCuts[1][j] : segmentMax[k][1]});
                                    }
                                    else {
                                        segmentPoints.add(segmentMax[k]);
                                    }
                                }
                                else if (segmentMax[k] == null && segmentMin[k] != null) {
                                    if (dataset.axisParallelCuts[i][j] > segmentMin[k][i]) {
                                        segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : segmentMin[k][0], i == 1 ? dataset.axisParallelCuts[1][j] : segmentMin[k][1]});
                                    }
                                    else {
                                        segmentPoints.add(segmentMin[k]);
                                    }
                                }
                                else if (segmentMin[k] != null && segmentMax[k] != null) {
                                    if (dataset.axisParallelCuts[i][j] > segmentMin[k][i] && dataset.axisParallelCuts[i][j] < segmentMax[k][i]) {
                                        segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : segmentMin[k][0], i == 1 ? dataset.axisParallelCuts[1][j] : segmentMin[k][1]});
                                    }
                                    else {
                                        segmentPoints.add(new double[] {(segmentMax[k][0]+segmentMin[k][0])/2, (segmentMax[k][1]+segmentMin[k][1])/2});
                                    }
                                }
                            }
                            segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : maxPoint[0], i == 1 ? dataset.axisParallelCuts[1][j] : maxPoint[1]});
                            segmentPoints.add(new double[] {i == 0 ? dataset.axisParallelCuts[0][j] : maxPoint[0]+(maxPoint[0]-minPoint[0]), i == 1 ? dataset.axisParallelCuts[1][j] : maxPoint[1]+(maxPoint[1]-minPoint[1])});
                            for (int k = 0; k < segmentPoints.size()-1; k++) {
                                int[] pos1 = convertPointToCoordinateOnScreen(segmentPoints.get(k));
                                int[] pos2 = convertPointToCoordinateOnScreen(segmentPoints.get(k+1));
                                g2d.drawLine(pos1[0]+POINT_SIZE/2, pos1[1]+POINT_SIZE/2, pos2[0]+POINT_SIZE/2, pos2[1]+POINT_SIZE/2);
                            }
                        }
                        costIndex++;
                    }
                }
            }
        }
    }

    //Returns an identical color to the received one but with a translucency based on percentage.
    private Color changeTranslucencyOfColor(Color color, double percentage) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(percentage * 255));
    }

    //Computes where a point should be placed on the screen given it's coordinates.
    private int[] convertPointToCoordinateOnScreen(double[] coor) {
        return new int[] { (int)(xOrig + (coor[0] * (lineGap) / factor) - POINT_SIZE / 2),  (int)(yOrig - (coor[1] * (lineGap) / factor) - POINT_SIZE / 2)};
    }

    //Computes the coordinates for a point based on it's position on the screen.
    private double[] convertScreenPositionToCoordinate(int x, int y) {
        return new double[] { (x - xOrig) * factor / (double)lineGap, (yOrig - y) * factor / (double)lineGap };
    }

    //Loads the points of a feature based data set in a variable and finds the min/max values necessary to configure the axes.
    protected void loadPoints(double[][] points) {
        if (runningTSNE) {
            return;
        }
        originalNumberOfPoints = points.length;
        points = convertPoints(points);
        if (points[0].length > 2) {
            TSne(points);
        }
        else if (points[0].length == 1) {
            this.points = convert1DTo2D(points);
        }
        else {
            this.points = points;
        }
        double[] bounds = findBounds(points);
        configureAxes(bounds);
        repaint();
    }

    //Load the points of a binary data set and saves them in a variable.
    protected void loadPoints(BitSet[] questionnaireAnswers) {
        if (runningTSNE) {
            return;
        }
        originalNumberOfPoints = questionnaireAnswers.length;
        double[][] dataPoints = new double[questionnaireAnswers.length][questionnaireAnswers[0].size()];
        for (int i = 0; i < questionnaireAnswers.length; i++) {
            for (int j = 0; j < questionnaireAnswers[i].size(); j++) {
                dataPoints[i][j] = questionnaireAnswers[i].get(j) ? 1 : 0;
            }
        }
        dataPoints = convertPoints(dataPoints);
        TSne(dataPoints);
        repaint();
    }

    //Converts a one-dimensional data set to a two-dimensional data set.
    private double[][] convert1DTo2D(double[][] points) {
        double[][] copy = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            copy[i][0] = points[i][0];
            copy[i][1] = 0;
        }
        return copy;
    }

    //Converts a higher-dimensional data set to a two-dimensional data set using t-SNE. Runs in a different thread.
    private void TSne(double[][] dataPoints) {
        runningTSNE = true;
        points = null;
        int initialDims = dataPoints[0].length;
        double perplexity = 20.0;
        int maxIterations = 500;
        boolean parallel = true;
        BarnesHutTSne tsne;
        if(parallel) {
            tsne = new ParallelBHTsne();
        } else {
            tsne = new BHTSne();
        }
        TSneConfiguration config = TSneUtils.buildConfig(dataPoints, 2, initialDims, perplexity, maxIterations);
        new Thread(new Runnable() {
            @Override
            public void run() {
                points = tsne.tsne(config);
                if (points[0].length == 1) {
                    points = convert1DTo2D(points);
                }
                double[] bounds = findBounds(points);
                configureAxes(bounds);
                view.selectedSidePanel.update(points.length);
                repaint();
                runningTSNE = false;
            }
        }).start();
    }

    //Sets the colors array. Uses hard coded colors if there are no more than 7 clusters.
    //Otherwise, it uses random colors.
    private void setColors(int[] clusters) {
        colors = new Color[] { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.ORANGE, Color.PINK, Color.GRAY };
        int amountOfColors = 0;
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i] > amountOfColors) {
                amountOfColors = clusters[i];
            }
        }
        amountOfColors++;
        if (amountOfColors > colors.length) {
            Random random = new Random();
            this.colors = new Color[amountOfColors];
            for (int i = 0; i < amountOfColors; i++) {
                float r = random.nextFloat();
                float g = random.nextFloat();
                float b = random.nextFloat();
                Color randomColor = new Color(r, g, b);
                this.colors[i] = randomColor;
            }
        }
    }

    //Colors the data points in a data set based on the received hard clustering.
    public void loadClusters(int[] clusters) {
        if (clusters == null) {
            this.clusters = null;
            return;
        }
        clusters = convertClusters(clusters);
        this.clusters = clusters;
        if (!view.selectedSidePanel.showGroundTruth()) {
            setColors(clusters);
        }
        repaint();
    }

    //Colors the data points in a data set based on the received hard and soft clustering.
    //Soft clustering is illustrated by the translucency of the color.
    public void loadClusters(int[] clusters, double[][] softClustering) {
        if (softClustering != null) {
            softClustering = convertSoftClustering(softClustering);
        }
        this.softClustering = softClustering;
        loadClusters(clusters);
    }

    //Reduces the number of points if there are too many points to draw.
    private double[][] convertPoints(double[][] dataPoints) {
        indicesToDraw = getRandomDistinctNumbers(Math.min(MAX_POINTS_TO_DRAW, dataPoints.length), dataPoints.length);
        double[][] newPoints = new double[indicesToDraw.length][dataPoints[0].length];
        for (int i = 0; i < newPoints.length; i++) {
            newPoints[i] = dataPoints[indicesToDraw[i]];
        }
        return newPoints;
    }

    //Switches between showing and not showing ground truth.
    public void showGroundTruth(boolean show) {
        if (show) {
            int[] groundTruth = view.getDataset().getGroundTruth();
            groundTruth = convertClusters(groundTruth);
            setColors(groundTruth);
        }
        else if (clusters != null) {
            setColors(clusters);
        }
    }

    //Matches the clusters to the points if the number of points have been reduced.
    private int[] convertClusters(int[] clusters) {
        int[] newClusters = new int[indicesToDraw.length];
        for (int i = 0; i < indicesToDraw.length; i++) {
            newClusters[i] = clusters[indicesToDraw[i]];
        }
        return newClusters;
    }

    //Convert the soft clustering of the whole data set to a soft clustering only for the drawn points.
    private double[][] convertSoftClustering(double[][] softClustering) {
        double[][] newSoftClustering = new double[indicesToDraw.length][];
        for (int i = 0; i < indicesToDraw.length; i++) {
            newSoftClustering[i] = softClustering[indicesToDraw[i]];
        }
        return newSoftClustering;
    }

    //Finds the min and max values of the x and y values of the data points.
    private double[] findBounds(double[][] points) {
        double[] bounds = new double[4];  //Max x, max y, min x, min y
        for (double[] point : points) {
            if (point[0] > bounds[0]) {
                bounds[0] = point[0];
            }
            if (point[0] <= bounds[2]) {
                bounds[2] = point[0];
            }
            if (point.length > 1) {
                if (point[1] > bounds[1]) {
                    bounds[1] = point[1];
                }
                if (point[1] <= bounds[3]) {
                    bounds[3] = point[1];
                }
            }
        }
        return bounds;
    }

    //Configures the axes based of the received min and max values of the data points.
    private void configureAxes(double[] bounds) {
        double max = 0;
        for (double bound : bounds) {
            max = Math.max(Math.abs(bound), max);
        }
        factor = Math.max((int)(max / 6), 1);
        xOrig = (view.windowWidth - view.sidePanelWidth) / 2 + (int)((bounds[0] + bounds[2]) / factor);
        yOrig = (view.windowHeight - view.topPanelHeight) / 2 - (int)((bounds[1] + bounds[3]) / factor);
    }

    //Rounds a double d to decimalPlaces decimal places.
    private double round(double d, int decimalPlaces) {
        return new BigDecimal(d).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    //Updates the coordinate text and converts it to scientific notation if necessary.
    private void updateCoordinateText() {
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            double[] coor = convertScreenPositionToCoordinate(mousePos.x, mousePos.y);
            String text = " ";
            if (coor[0] > 9999 || coor[0] < -9999) {
                text += convertNumberToScientificNotation(coor[0]) + ", ";
            } else {
                text += round(coor[0], 2) + ", ";
            }

            if (coor[1] > 9999 || coor[1] < -9999) {
                text += convertNumberToScientificNotation(coor[1]);
            } else {
                text += round(coor[1], 2);
            }

            coordinates.setText(text);
        }
    }

    //Converts a double n to scientific notation.
    private String convertNumberToScientificNotation(double n) {
        NumberFormat numberFormat = new DecimalFormat("0.00E0");
        return numberFormat.format(n);
    }

    //Changes the boolean value determining if axes should be shown.
    protected void switchShowingOfAxes() {
        showAxes = !showAxes;
    }

    //Changes the boolean value determining if grid lines should be shown.
    protected void switchShowingOfGridlines() {
        showGridLines = !showGridLines;
    }

    //Returns an array of "amount" random distinct numbers between 0 (inclusive) and "maxNumber" (exclusive).
    private int[] getRandomDistinctNumbers(int amount, int maxNumber) {
        ArrayList<Integer> list = new ArrayList<>(maxNumber);
        for (int i = 0; i < maxNumber; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        int[] result = new int[amount];
        for (int i = 0; i < amount; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    //Returns the number of points in the currently loaded data set.
    public int getNumberOfPoints() {
        if (points == null) {
            return 0;
        }
        return points.length;
    }

    //Return the original number of points of the data set.
    public int getOriginalNumberOfPoints() {
        return originalNumberOfPoints;
    }

    //Inherited method from DataVisualizer determining returning true if t-SNE is not running.
    public boolean isReady() {
        return !runningTSNE;
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    //Updates the coordinate text when the mouse moves.
    @Override
    public void mouseMoved(MouseEvent e) {
        if (view.hasDataset()) {
            updateCoordinateText();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    //Saves the mouse position and starts dragging when the mouse is pressed.
    @Override
    public void mousePressed(MouseEvent e) {
        Point mousePos = getMousePosition();
        if (mousePos != null && SwingUtilities.isLeftMouseButton(e)) {
            mouseOrigVector = new int[]{xOrig - mousePos.x, yOrig - mousePos.y};
            dragging = true;
        }
    }

    //Stops dragging when the mouse is released.
    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    //Zooms in/out when scrolling.
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int lineGapReset = windowMax / 30;
        double nZoom = lineGap / (double) 50;
        double tempFactor = factor;
        double tempLineGap = lineGap;
        double origWidthDist = ((double) (e.getX() - xOrig) / (double) lineGap);
        double origHeightDist = ((double) (e.getY() - yOrig) / (double) lineGap);

        if (e.getPreciseWheelRotation() > 0.0 && lineGap > lineGapReset / 6) {
            lineGap -= nZoom > 0 ? nZoom : 1;
            if (lineGap < lineGapReset / 5 && zoomFactor >= 1) {
                lineGap = lineGapReset;
                factor *= 5;
                zoomFactor--;
            }
        } else if (e.getPreciseWheelRotation() < 0.0) {
            if (zoomFactor >= 0) {
                lineGap += nZoom > 1 ? nZoom : 1;
                if (lineGap > lineGapReset * 5) {
                    lineGap = lineGapReset;
                    factor /= 5;
                    zoomFactor++;
                }
            }
        }

        xOrig -= (origWidthDist - (e.getX() - xOrig) / (double) lineGap) * (double) lineGap;
        yOrig -= (origHeightDist - (e.getY() - yOrig) / (double) lineGap) * (double) lineGap;

        if (tempFactor > factor) {
            xOrig -= (origWidthDist) * 4 * (double) lineGap;
            yOrig -= (origHeightDist) * 4 * (double) lineGap;
        } else if (tempFactor < factor) {
            xOrig += (origWidthDist) * 4 * (tempLineGap + 0.5);
            yOrig += (origHeightDist) * 4 * (tempLineGap + 0.5);
        }
        zoomTimer = 200;
        repaint();
    }

    //Concurrent thread that moves the graph when dragging.
    private class BoardDragger implements Runnable {
        @Override
        public void run() {
            while (!close) {
                if (dragging) {
                    Point mousePos = getMousePosition();
                    if (mousePos != null) {
                        xOrig = mousePos.x + mouseOrigVector[0];
                        yOrig = mousePos.y + mouseOrigVector[1];
                        repaint();
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Reduces the zoom timer periodically.
    private class ZoomTimer implements Runnable {
        @Override
        public void run() {
            while(!close) {
                if (zoomTimer > 0) {
                    zoomTimer -= 100;
                    if (zoomTimer <= 0) {
                        repaint();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            }
        }
    }
}

