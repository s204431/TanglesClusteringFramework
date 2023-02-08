package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Random;

public class PlottingView extends JPanel implements MouseMotionListener {
    public static final int POINT_SIZE = 10;

    public JFrame frame;

    public int windowWidth, windowHeight;

    private double[][] points;
    private int[] clusters;
    private double[][] softClustering;
    private Color[] colors;

    private int xOrig = (int)(windowWidth * 0.5);
    private int yOrig = (int)(windowHeight * 0.5);
    private int factor = 1; //Used to expand x- or y-axis to capture largest datapoint
    private int lineGap;
    private int lines;

    private int mouseX, mouseY;

    private JTextField coordinates = new JTextField();

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
        coordinates.setBounds(5, 5, 100, 30);
        coordinates.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        add(coordinates);


        frame.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                windowWidth = frame.getWidth();
                windowHeight = frame.getHeight();
                repaint();
            }
        });

        addMouseMotionListener(this);
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
        int windowMax = Math.max(windowHeight, windowWidth);
        int lineSize1 = windowMax/150;
        int lineSize2 = windowMax/75;
        g2d.setStroke(stroke2);
        int fontSize = windowMax / 70;
        g.setFont(new Font("TimesRoman", Font.BOLD, Math.max(fontSize, 12)));
        lineGap = windowMax/30;
        lines = windowMax/lineGap;
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
                String posText = "" + i * factor;
                String negText = "-" + i * factor;
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
                }
                g2d.fillOval(coor[0], coor[1], POINT_SIZE, POINT_SIZE);
                g2d.setColor(Color.GRAY);
                g2d.drawOval(coor[0], coor[1], POINT_SIZE, POINT_SIZE);
            }
        }
    }

    private Color changeTranslucencyOfColor(Color color, double percentage) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(percentage * 255));
    }

    public int[] convertPointToCoordinateOnScreen(double[] coor) {
        return new int[] { (int)(xOrig + (coor[0] * lineGap / factor) - POINT_SIZE / 2),  (int)(yOrig - (coor[1] * lineGap / factor) - POINT_SIZE / 2)};
    }

    private int[] convertScreenPositionToCoordinate(int x, int y) {
        return new int[] { (x - xOrig) * factor / lineGap, (yOrig - y) * factor / lineGap };
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
        this.colors = new Color[clusters.length];
        Random random = new Random();
        for (int i = 0; i < clusters.length; i++) {
            float r = random.nextFloat();
            float g = random.nextFloat();
            float b = random.nextFloat();
            Color randomColor = new Color(r,g,b);
            this.colors[i] = randomColor;
        }
        repaint();
    }

    public void loadClusters(int[] clusters, double[][] softClustering) {
        this.clusters = clusters;
        this.softClustering = softClustering;
        this.colors = new Color[clusters.length];
        Random random = new Random();
        for (int i = 0; i < clusters.length; i++) {
            float r = random.nextFloat();
            float g = random.nextFloat();
            float b = random.nextFloat();
            Color randomColor = new Color(r,g,b);
            this.colors[i] = randomColor;
        }
        repaint();
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
        xOrig = windowWidth / 2 + (int)(bounds[0] + bounds[2]) / factor;
        yOrig = windowHeight / 2 - (int)(bounds[1] + bounds[3]) / factor;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        int[] coor = convertScreenPositionToCoordinate(mouseX, mouseY);
        coordinates.setText(coor[0] + ", " + coor[1]);
    }
}

