package view;

import datasets.FeatureBasedDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImageView extends JPanel implements DataVisualizer {

    //Responsible: Michael

    // This class visualizes an image and the clustering of that image.

    public static final String name = "Image View";

    private View view;

    private Color[] colors;

    private int numberOfPoints = 0;

    private BufferedImage originalImage;
    private BufferedImage clusteredImage;
    private double originalImageX;
    private double originalImageY;
    private double clusteredImageX;
    private double clusteredImageY;
    private double imageWidth;
    private double imageHeight;

    //Constructor receiving view.
    public ImageView(View view) {
        this.view = view;
        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
    }

    //Draws ImageView on screen.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Draw original image
        if (originalImage != null) {
            g.drawImage(originalImage, (int)originalImageX, (int)originalImageY, (int)imageWidth, (int)imageHeight, null);
        }

        //Draw clustered image
        if (clusteredImage != null) {
            g.drawImage(clusteredImage, (int)clusteredImageX, (int)clusteredImageY, (int)imageWidth, (int)imageHeight, null);
        }

        //Draw line separating images
        ((Graphics2D)g).setStroke(new BasicStroke(3));
        g.drawLine((view.windowWidth - view.sidePanelWidth) / 2, 0, (view.windowWidth - view.sidePanelWidth) / 2, view.windowHeight);
    }

    //Loads original image and adjusts its size.
    public void loadImage(BufferedImage image) {
        originalImage = image;
        numberOfPoints = image.getWidth() * image.getHeight();

        //Adjust size of image
        imageWidth = (double)(view.windowWidth - view.sidePanelWidth) / 2;
        double scale = imageWidth / originalImage.getWidth();
        imageHeight = originalImage.getHeight() * scale;

        int maxWidth = view.windowWidth - view.sidePanelWidth;
        int maxHeight = view.windowHeight - view.topPanelHeight*3;

        if (imageWidth > maxWidth) {
            scale = maxWidth / imageWidth;
            imageWidth = maxWidth;
            imageHeight *= scale;
        }

        if (imageHeight > maxHeight) {
            scale = maxHeight / imageHeight;
            imageHeight = maxHeight;
            imageWidth *= scale;
        }

        //Set position of images
        originalImageX = 0;
        originalImageY = 0;
        clusteredImageX = (double)(view.windowWidth - view.sidePanelWidth) - imageWidth;
        clusteredImageY = 0;
    }

    //Colors the pixels based on the received hard clustering.
    public void loadClusters(int[] hardClustering, double[][] softClustering) {
        loadClusters(hardClustering);
    }

    //Colors the pixels based on the received hard clustering.
    public void loadClusters(int[] hardClustering) {
        if (hardClustering == null) {
            clusteredImage = null;
            return;
        }

        double[][] dataPoints = ((FeatureBasedDataset)view.getDataset()).dataPoints;
        colors = new Color[Arrays.stream(hardClustering).max().getAsInt()+1];
        for (int i = 0; i < colors.length; i++) {
            int r = 0;
            int g = 0;
            int b = 0;
            int n = 0;
            for (int j = 0; j < hardClustering.length; j++) {
                if (hardClustering[j] == i) {
                    r += dataPoints[j][0];
                    g += dataPoints[j][1];
                    b += dataPoints[j][2];
                    n++;
                }
            }
            n = n == 0 ? 1 : n;
            colors[i] = new Color(r/n, g/n, b/n);
        }

        clusteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int i = 0; i < clusteredImage.getWidth(); i++) {
            for (int j = 0; j < clusteredImage.getHeight(); j++) {
                clusteredImage.setRGB(i, j, colors[hardClustering[index++]].getRGB());
            }
        }

        repaint();
    }

    @Override
    public void showGroundTruth(boolean show) {
        //Do nothing.
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
}
