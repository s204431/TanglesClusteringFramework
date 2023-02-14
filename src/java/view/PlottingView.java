package view;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;
import util.BitSet;
import view.View;

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

public class PlottingView extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final int POINT_SIZE = 8;
    private static final int MAX_POINTS_TO_DRAW = 10000;
    private static final int MAX_POINTS_TO_DRAW_WHEN_MOVING = 2000;

    private View view;

    private int windowMax;

    public JFrame frame;

    private boolean close = false;

    private double[][] points;
    private int[] clusters;
    private double[][] softClustering;
    private Color[] colors;

    private int[] mouseOrigVector;
    private boolean dragging = false;

    private int xOrig;
    private int yOrig;
    private double factor = 1; //Used to expand x- or y-axis to capture largest datapoint
    private int zoomFactor = 1;
    private int lineGap;
    private int lines;

    private JLabel coordinates = new JLabel();

    //Often used strokes
    private final BasicStroke stroke1 = new BasicStroke(1);
    private final BasicStroke stroke2 = new BasicStroke(2);
    private final BasicStroke stroke3 = new BasicStroke(3);
    private final BasicStroke stroke4 = new BasicStroke(4);

    private int[] indicesToDraw; //Indices of points to draw when there are too many points.
    protected int originalNumberOfPoints; //Number of points before reducing the number of points.

    public PlottingView(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        xOrig = (int)(view.getWindowWidth() * 0.5);
        yOrig = (int)(view.getWindowHeight() * 0.5);

        /*
        //Create frame
        frame = new JFrame("PlottingView");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(windowWidth, windowHeight));
        frame.setLayout(null);

        frame.add(this);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                windowWidth = frame.getWidth();
                windowHeight = frame.getHeight();
                repaint();
            }
        });

         */

        coordinates.setBounds(5, 5, 150, 30);
        coordinates.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        add(coordinates);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        (new Thread(new BoardDragger())).start();
    }

    //Draws plottingView on screen.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        //Draw axes
        g2d.setStroke(stroke4);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, yOrig, view.getWindowWidth(), yOrig);
        g2d.drawLine(xOrig, 0, xOrig, view.getWindowHeight());

        //Draw lines, grid lines and numbers on axes
        windowMax = Math.max(view.getWindowHeight(), view.getWindowWidth());
        int lineSize1 = windowMax/150;
        int lineSize2 = windowMax/75;
        int fontSize = windowMax / 70;
        g2d.setFont(new Font("TimesRoman", Font.BOLD, Math.max(fontSize, 12)));
        g2d.setStroke(stroke2);
        if (lineGap == 0) {
            lineGap = windowMax / 30;
        }
        lines = (windowMax + windowMax * zoomFactor * 4) / lineGap;
        int lineSize;
        boolean drawNumber;
        for (int i = 1; i < lines; i++) {
            if (i%5 == 0) {
                g2d.setStroke(stroke3);
                lineSize = lineSize2;
                drawNumber = true;
            } else {
                g2d.setStroke(stroke2);
                lineSize = lineSize1;
                drawNumber = false;
            }

            //Define useful values
            int posX = xOrig + lineGap * i;
            int posY = yOrig + lineGap * i;
            int negX = xOrig - lineGap * i;
            int negY = yOrig - lineGap * i;

            //Draw gridlines
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(stroke1);
            g2d.drawLine(posX, 0, posX, view.getWindowHeight()); //Positive direction on x-axis
            g2d.drawLine(0, posY, view.getWindowWidth(), posY); //Positive direction on y-axis
            g2d.drawLine(negX, 0, negX, view.getWindowHeight()); //Negative direction on x-axis
            g2d.drawLine(0, negY, view.getWindowWidth(), negY); //Negative direction on y-axis

            //Draw lines on axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(posX, yOrig - lineSize, posX, yOrig + lineSize); //Positive direction on x-axis
            g2d.drawLine(xOrig - lineSize, posY, xOrig + lineSize, posY); //Positive direction on y-axis
            g2d.drawLine(negX, yOrig - lineSize, negX, yOrig + lineSize); //Negative direction on x-axis
            g2d.drawLine(xOrig - lineSize, negY, xOrig + lineSize, negY); //Negative direction on y-axis

            //Draw numbers on axes
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
                    posText = "" + (int)(num);
                    negText = "-" + (int)(num);
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

        //Plot points
        if (points != null) {
            int bound = MAX_POINTS_TO_DRAW_WHEN_MOVING < points.length && dragging ? MAX_POINTS_TO_DRAW_WHEN_MOVING : points.length;
            for (int i = 0; i < bound; i++) {
                int[] coor = convertPointToCoordinateOnScreen(points[i]);
                if (clusters != null) {
                    Color c = colors[clusters[i]];
                    if (softClustering != null) {
                        c = changeTranslucencyOfColor(c, softClustering[i][clusters[i]]);
                    }
                    g2d.setColor(c);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.fillOval(coor[0], coor[1], POINT_SIZE, POINT_SIZE);
                //g2d.setColor(Color.BLACK);
                g2d.setColor(new Color(0,0,0,50));
                g2d.drawOval(coor[0], coor[1], POINT_SIZE, POINT_SIZE);
            }
        }

        //Box behind coordinate text
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(5, 5, 150, 30);
        updateCoordinateText();
    }

    private Color changeTranslucencyOfColor(Color color, double percentage) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(percentage * 255));
    }

    public int[] convertPointToCoordinateOnScreen(double[] coor) {
        return new int[] { (int)(xOrig + (coor[0] * (lineGap) / factor) - POINT_SIZE / 2),  (int)(yOrig - (coor[1] * (lineGap) / factor) - POINT_SIZE / 2)};
    }

    private double[] convertScreenPositionToCoordinate(int x, int y) {
        return new double[] { (x - xOrig) * factor / (double)lineGap, (yOrig - y) * factor / (double)lineGap };
    }

    protected void loadPoints(double[][] points) {
        loadPoints(points, true);
    }

    protected void loadPoints(double[][] points, boolean shufflePoints) {
        if (shufflePoints) {
            originalNumberOfPoints = points.length;
            points = convertPoints(points);
        }
        if (points[0].length > 2) {
            points = TSne(points);
        }
        else if (points[0].length == 1) {
            points = convert1DTo2D(points);
        }
        double[] bounds = findBounds(points);
        configureAxes(bounds);
        this.points = points;
        repaint();
    }

    protected void loadPoints(BitSet[] questionnaireAnswers) {
        originalNumberOfPoints = questionnaireAnswers.length;
        double[][] dataPoints = new double[questionnaireAnswers.length][questionnaireAnswers[0].size()];
        for (int i = 0; i < questionnaireAnswers.length; i++) {
            for (int j = 0; j < questionnaireAnswers[i].size(); j++) {
                dataPoints[i][j] = questionnaireAnswers[i].get(j) ? 1 : 0;
            }
        }
        dataPoints = convertPoints(dataPoints);
        dataPoints = TSne(dataPoints);
        loadPoints(dataPoints, false);
    }

    private double[][] convert1DTo2D(double[][] points) {
        double[][] copy = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            copy[i][0] = points[i][0];
            copy[i][1] = 0;
        }
        return copy;
    }

    private double[][] TSne(double[][] dataPoints) {
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
        double [][] points = tsne.tsne(config);

        return points;
    }

    public void loadClusters(int[] clusters) {
        clusters = convertClusters(clusters);
        colors = new Color[] { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.ORANGE, Color.PINK, Color.GRAY };
        this.clusters = clusters;
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
        repaint();
    }

    public void loadClusters(int[] clusters, double[][] softClustering) {
        softClustering = convertSoftClustering(softClustering);
        this.softClustering = softClustering;
        loadClusters(clusters);
    }

    //Reduces the number of points if there are too many points to draw.
    private double[][] convertPoints(double[][] dataPoints) {
        pointsShuffled = true;
        indicesToDraw = getRandomDistinctNumbers(Math.min(MAX_POINTS_TO_DRAW, dataPoints.length), dataPoints.length);
        double[][] newPoints = new double[indicesToDraw.length][dataPoints[0].length];
        for (int i = 0; i < newPoints.length; i++) {
            newPoints[i] = dataPoints[indicesToDraw[i]];
        }
        return newPoints;
    }

    //Matches the clusters to the points if the number of points have been reduced.
    private int[] convertClusters(int[] clusters) {
        int[] newClusters = new int[indicesToDraw.length];
        for (int i = 0; i < indicesToDraw.length; i++) {
            newClusters[i] = clusters[indicesToDraw[i]];
        }
        return newClusters;
    }

    private double[][] convertSoftClustering(double[][] softClustering) {
        double[][] newSoftClustering = new double[indicesToDraw.length][];
        for (int i = 0; i < indicesToDraw.length; i++) {
            newSoftClustering[i] = softClustering[indicesToDraw[i]];
        }
        return newSoftClustering;
    }

    private double[] findBounds(double[][] points) {
        double[] bounds = new double[4];  //Max x, max y, min x, min y
        for (double[] point : points) {
            if (point[0] > bounds[0]) {
                bounds[0] = point[0];
            }
            if (point[1] > bounds[1]) {
                bounds[1] = point[1];
            }
            if (point[0] < bounds[2]) {
                bounds[2] = point[0];
            }
            if (point[1] < bounds[3]) {
                bounds[3] = point[1];
            }
        }
        return bounds;
    }

    private void configureAxes(double[] bounds) {
        double max = 0;
        for (double bound : bounds) {
            max = Math.max(Math.abs(bound), max);
        }
        factor = Math.max((int)(max / 6), 1);
        xOrig = view.getWindowWidth() / 2 + (int)((bounds[0] + bounds[2]) / factor);
        yOrig = view.getWindowHeight() / 2 - (int)((bounds[1] + bounds[3]) / factor);
    }

    private double round(double d, int decimalPlaces) {
        return new BigDecimal(d).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

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

    private String convertNumberToScientificNotation(double n) {
        NumberFormat numberFormat = new DecimalFormat("0.00E0");
        return numberFormat.format(n);
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

    protected int getNumberOfPoints() {
        if (points == null) {
            return 0;
        }
        return points.length;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateCoordinateText();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            mouseOrigVector = new int[]{xOrig - mousePos.x, yOrig - mousePos.y};
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    //Zooms in/out when scrolling.
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int lineGapReset = windowMax / 30;
        double nZoom = lineGap / (double)50;
        double tempFactor = factor;
        double tempLineGap = lineGap;
        double origWidthDist = ((double)(e.getX() - xOrig) / (double)lineGap);
        double origHeightDist = ((double)(e.getY() - yOrig) / (double)lineGap);

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

        xOrig -= (origWidthDist - (e.getX() - xOrig) / (double)lineGap) * (double)lineGap;
        yOrig -= (origHeightDist - (e.getY() - yOrig) / (double)lineGap) * (double)lineGap;

        if (tempFactor > factor) {
            xOrig -= (origWidthDist) * 4 * (double) lineGap;
            yOrig -= (origHeightDist) * 4 * (double) lineGap;
        } else if (tempFactor < factor) {
            xOrig += (origWidthDist) * 4 * (tempLineGap + 0.5);
            yOrig += (origHeightDist) * 4 * (tempLineGap + 0.5);
        }
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
}

