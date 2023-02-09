package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class PlottingView extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    public static final int POINT_SIZE = 8;

    public JFrame frame;

    public int windowWidth, windowHeight, windowMax;

    private boolean close = false;

    private double[][] points;
    private int[] clusters;
    private double[][] softClustering;
    private Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.ORANGE, Color.PINK, Color.GRAY };  //Default colors

    private int[] mouseOrigVector;
    private boolean dragging = false;

    private int xOrig = (int)(windowWidth * 0.5);
    private int yOrig = (int)(windowHeight * 0.5);
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

    public PlottingView() {
        //Initialize
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(screenSize.width, screenSize.height));
        setBounds(0, 0, getPreferredSize().width, getPreferredSize().height);
        setLayout(null);

        windowWidth = screenSize.width - screenSize.width / 4;
        windowHeight = screenSize.height - screenSize.height / 4;

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

        //Components
        coordinates.setBounds(5, 5, 150, 30);
        coordinates.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        frame.add(coordinates);


        frame.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                windowWidth = frame.getWidth();
                windowHeight = frame.getHeight();
                repaint();
            }
        });

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        (new Thread(new BoardDragger())).start();
    }

    //Draws everything on screen.
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        //Draw axes
        g2d.setStroke(stroke4);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, yOrig, windowWidth, yOrig);
        g2d.drawLine(xOrig, 0, xOrig, windowHeight);

        //Draw lines, grid lines and numbers on axes
        windowMax = Math.max(windowHeight, windowWidth);
        int lineSize1 = windowMax/150;
        int lineSize2 = windowMax/75;
        int fontSize = windowMax / 70;
        g.setFont(new Font("TimesRoman", Font.BOLD, Math.max(fontSize, 12)));
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
            g2d.drawLine(posX, 0, posX, windowHeight); //Positive direction on x-axis
            g2d.drawLine(0, posY, windowWidth, posY); //Positive direction on y-axis
            g2d.drawLine(negX, 0, negX, windowHeight); //Negative direction on x-axis
            g2d.drawLine(0, negY, windowWidth, negY); //Negative direction on y-axis

            //Draw lines on axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(posX, yOrig - lineSize, posX, yOrig + lineSize); //Positive direction on x-axis
            g2d.drawLine(xOrig - lineSize, posY, xOrig + lineSize, posY); //Positive direction on y-axis
            g2d.drawLine(negX, yOrig - lineSize, negX, yOrig + lineSize); //Negative direction on x-axis
            g2d.drawLine(xOrig - lineSize, negY, xOrig + lineSize, negY); //Negative direction on y-axis

            //Draw number on axes
            if (drawNumber) {
                String posText = "" + (int)(i * factor);
                String negText = "-" + (int)(i * factor);
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
            for (int i = 0; i < points.length; i++) {
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

    public void loadPointsWithClustering(double[][] points, int[] clusters) {
        loadPoints(points);
        loadClusters(clusters);
    }

    public void loadPointsWithClustering(double[][] points, int[] clusters, double[][] softClustering) {
        loadPoints(points);
        loadClusters(clusters, softClustering);
    }

    public void loadPoints(double[][] points) {
        double[] bounds = findBounds(points);
        configureAxes(bounds);
        this.points = points;
        repaint();
    }

    public void loadClusters(int[] clusters) {
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
        this.softClustering = softClustering;
        loadClusters(clusters);
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
        xOrig = windowWidth / 2 + (int)((bounds[0] + bounds[2]) / factor);
        yOrig = windowHeight / 2 - (int)((bounds[1] + bounds[3]) / factor);
    }

    private double round(double d, int decimalPlaces) {
        return new BigDecimal(d).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            double[] coor = convertScreenPositionToCoordinate(mousePos.x, mousePos.y);
            String text = round(coor[0], 2) + ", " + round(coor[1], 2);
            coordinates.setText(text);
        }
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
        int nZoom = lineGap / 70;
        double tempFactor = factor;
        boolean factorChanged = false;
        double origWidthDist = ((e.getX() - xOrig) / (double)lineGap);
        double origHeightDist = ((e.getY() - yOrig) / (double)lineGap);
        if (e.getPreciseWheelRotation() > 0.0 && lineGap > lineGapReset / 6) {
            lineGap -= nZoom > 0 ? nZoom : 1;
            if (lineGap < lineGapReset / 5 && zoomFactor >= 1) {
                lineGap = lineGapReset;
                factor *= 5;
                zoomFactor--;
                factorChanged = true;
            }
        } else if (e.getPreciseWheelRotation() < 0.0) {
            if (zoomFactor >= 0) {
                lineGap += nZoom > 0 ? nZoom : 1;
                if (lineGap > lineGapReset * 5) {
                    lineGap = lineGapReset;
                    factor /= 5;
                    zoomFactor++;
                    factorChanged = true;
                }
            }
        }
        xOrig -= (origWidthDist - (e.getX() - xOrig) / (double)lineGap) * (double)lineGap;
        yOrig -= (origHeightDist - (e.getY() - yOrig) / (double)lineGap) * (double)lineGap;
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

