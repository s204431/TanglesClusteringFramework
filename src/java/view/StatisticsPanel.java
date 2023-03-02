package view;

import datasets.Dataset;
import smile.plot.swing.ScatterPlot;

import javax.swing.*;
import java.awt.*;

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
    }

    protected void setBounds() {
        setBounds(0, view.topPanelHeight, view.windowWidth, view.windowHeight - view.topPanelHeight);
    }

}
