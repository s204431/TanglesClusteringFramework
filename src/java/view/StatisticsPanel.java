package view;

import datasets.Dataset;
import datasets.FeatureBasedDataset;
import smile.plot.swing.ScatterPlot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StatisticsPanel extends JPanel {
    private View view;

    protected StatisticsPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(10, 10, 100, 100);

        BufferedImage image = null;

        Dataset dataset = view.getDataset();
        if (dataset instanceof FeatureBasedDataset) {
            ScatterPlot plot = ScatterPlot.of(((FeatureBasedDataset) dataset).dataPoints);
            image = plot.canvas().toBufferedImage(view.windowWidth, view.windowHeight - view.topPanelHeight);
        }

        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    protected void setBounds() {
        setBounds(0, view.topPanelHeight, view.windowWidth, view.windowHeight - view.topPanelHeight);
    }

}
