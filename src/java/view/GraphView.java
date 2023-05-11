package view;

import datasets.GraphDataset;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;
import guru.nidi.graphviz.parse.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static guru.nidi.graphviz.model.Factory.*;

public class GraphView extends JPanel implements DataVisualizer, MouseListener, MouseWheelListener {

    //Responsible: Michael

    //This class visualizes a graph data set in GraphViz format.

    public static final String name = "Graph View";

    private View view;
    private Image image;
    private MutableGraph graph;
    private java.awt.Color[] colors;
    private int[] hardClustering;
    private double[][] softClustering;

    private int numberOfPoints = 0;

    private int[] mouseOrigVector;
    private boolean dragging = false;
    private double xOrig;
    private double yOrig;
    private double imageWidth;
    private double imageHeight;
    private double previousImageWidth;
    private double previousImageHeight;

    private boolean close = false;

    //Constructor receiving view.
    public GraphView(View view) {
        this.view = view;
        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));

        addMouseListener(this);
        addMouseWheelListener(this);
        (new Thread(new BoardDragger())).start();
    }

    //Draws graphView on screen.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, (int)xOrig, (int)yOrig, (int)imageWidth, (int)imageHeight, null);
        }
    }

    //Converts a mutable graph to and image in GraphViz format.
    protected Image convertGraphToImage() {
        Image image = Graphviz.fromGraph(graph).render(Format.PNG).toImage();
        if (imageWidth == 0.0 || imageHeight == 0.0) {
            imageWidth = image.getWidth(null);
            imageHeight = image.getHeight(null);

            int maxWidth = view.windowWidth - view.sidePanelWidth;
            int maxHeight = view.windowHeight - view.topPanelHeight*3;

            if (imageWidth > maxWidth) {
                double scale = maxWidth / imageWidth;
                imageWidth = maxWidth;
                imageHeight *= scale;
            }

            if (imageHeight > maxHeight) {
                double scale = maxHeight / imageHeight;
                imageHeight = maxHeight;
                imageWidth *= scale;
            }
        }
        previousImageWidth = imageWidth;
        previousImageHeight = imageHeight;
        return Graphviz.fromGraph(graph).width((int)imageWidth).height((int)imageHeight).render(Format.PNG).toImage();
    }

    //Constructs a mutable graph from a dot string.
    protected void loadGraphFromDotString(final String dot) {
        try {
            InputStream stream = new ByteArrayInputStream(dot.getBytes(StandardCharsets.UTF_8));
            graph = new Parser().read(stream);
            numberOfPoints = graph.nodes().size();
            graph.graphAttrs()
                    .add(Color.rgb(this.getBackground().getRGB()).background())
                    .nodeAttrs().add(Color.WHITE.fill())
                    .nodes().forEach(node ->
                            node.add(
                                    Color.BLACK,
                                    Style.FILLED,
                                    Style.lineWidth(1)
                            ));
            image = convertGraphToImage();
            repaint();
        } catch (Exception e) {}
    }

    //Colors the nodes in a mutable graph based on the received hard clustering.
    public void loadClusters(final int[] hardClustering) {
        if (hardClustering == null) {
            return;
        }
        if (!view.selectedSidePanel.showGroundTruth()) {
            this.hardClustering = hardClustering;
        }
        if (hardClustering != view.getDataset().getGroundTruth() && view.selectedSidePanel.showGroundTruth()) {
            return;
        }
        addColors(hardClustering);
        loadGraphFromDotString(((GraphDataset)view.getDataset()).asDot());
        for (int i = 0; i < hardClustering.length; i++) {
            graph.nodes().remove(mutNode("" + i));
            graph.add(mutNode("" + i).add(
                    Color.rgb(colors[hardClustering[i]].getRGB()).fill()
            ));
        }
        image = convertGraphToImage();
        repaint();
    }

    //Shows the ground truth.
    @Override
    public void showGroundTruth(boolean show) {
        if (show) {
            loadClusters(view.getDataset().getGroundTruth());
        } else if (hardClustering != null) {
            loadClusters(hardClustering, softClustering);
        }
        else {
            loadGraphFromDotString(((GraphDataset)view.getDataset()).asDot());
        }
    }

    //Colors the nodes in a mutable graph based on the received hard and soft clustering.
    //Soft clustering is illustrated by the translucency of the color.
    public void loadClusters(final int[] hardClustering, final double[][] softClustering) {
        if (hardClustering == null || softClustering == null) {
            return;
        }
        if (!view.selectedSidePanel.showGroundTruth()) {
            this.hardClustering = hardClustering;
            this.softClustering = softClustering;
        }
        if (hardClustering != view.getDataset().getGroundTruth() && view.selectedSidePanel.showGroundTruth()) {
            return;
        }
        addColors(hardClustering);
        loadGraphFromDotString(((GraphDataset)view.getDataset()).asDot());
        for (int i = 0; i < hardClustering.length; i++) {
            graph.nodes().remove(mutNode("" + i));
            graph.add(mutNode("" + i).add(
                    Color.rgba(colors[hardClustering[i]].getRGB()
                                    + ((int) (softClustering[i][hardClustering[i]] * 200) << 24))
                            .fill()
            ));
        }
        image = convertGraphToImage();
        repaint();
    }

    //Adds the necessary amount of colors to represent every cluster in the received hard clustering.
    private void addColors(final int[] hardClustering) {
        colors = new java.awt.Color[] { java.awt.Color.RED, java.awt.Color.BLUE, java.awt.Color.YELLOW, java.awt.Color.GREEN, java.awt.Color.ORANGE, java.awt.Color.PINK, java.awt.Color.GRAY };

        int amountOfColors = 0;
        for (int i = 0; i < hardClustering.length; i++) {
            if (hardClustering[i] > amountOfColors) {
                amountOfColors = hardClustering[i];
            }
        }
        amountOfColors++;
        if (amountOfColors > colors.length) {
            Random random = new Random();
            this.colors = new java.awt.Color[amountOfColors];
            for (int i = 0; i < amountOfColors; i++) {
                float r = random.nextFloat();
                float g = random.nextFloat();
                float b = random.nextFloat();
                java.awt.Color randomColor = new java.awt.Color(r, g, b);
                this.colors[i] = randomColor;
            }
        }
    }

    //Returns number of points in the mutable graph.
    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    //Returns the original number of points in the mutable graph.
    public int getOriginalNumberOfPoints() {
        return getNumberOfPoints();
    }

    //Inherited method from DataVisualizer (only relevant when t-SNE is running).
    public boolean isReady() {
        return true;
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    //Saves the mouse position and starts dragging when the mouse is pressed.
    @Override
    public void mousePressed(MouseEvent e) {
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            mouseOrigVector = new int[]{(int)xOrig - mousePos.x, (int)yOrig - mousePos.y};
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
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    //Zooms in and out when the mouse wheel is moved.
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getPreciseWheelRotation() < 0.0 || (imageWidth > 100 && imageHeight > 100)) {
            double fieldWidthDist = (e.getX() - xOrig)/imageWidth;
            double fieldHeightDist = (e.getY() - yOrig)/imageHeight;
            double w = e.getPreciseWheelRotation() * imageWidth / 25.0;
            double h = e.getPreciseWheelRotation() * imageHeight / 25.0;
            imageWidth -= w == 0.0 ? e.getPreciseWheelRotation() : w;
            imageHeight -= h == 0.0 ? e.getPreciseWheelRotation() : h;
            xOrig -= (fieldWidthDist - (e.getX() - xOrig) / imageWidth) * imageWidth;
            yOrig -= (fieldHeightDist - (e.getY() - yOrig) / imageHeight) * imageHeight;
        }

        if (previousImageWidth / imageWidth >= 2 || imageWidth / previousImageWidth >= 2) {
            previousImageWidth = imageWidth;
            previousImageHeight = imageHeight;
            image = Graphviz.fromGraph(graph).width((int)imageWidth).height((int)imageHeight).render(Format.PNG).toImage();
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
