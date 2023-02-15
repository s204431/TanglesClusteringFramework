package view;

import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SidePanel extends JPanel {
    private View view;
    private ValueAdjuster aValueAdjuster;

    public SidePanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        aValueAdjuster = new ValueAdjuster(1, 80);
        aValueAdjuster.setEnabled(false);
        aValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                update();
            }
        });
        add(aValueAdjuster);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("TimesRoman", Font.BOLD, 18));
        g2d.drawString("Total points: ", 30, 30);
        g2d.drawString(""+view.plottingView.originalNumberOfPoints, 30, 50);
        if (view.plottingView.originalNumberOfPoints != view.plottingView.getNumberOfPoints()) {
            g2d.drawString("Showing "+view.plottingView.getNumberOfPoints(), 30, 70);
        }
        g2d.drawString("NMI score:", 30, 130);
        g2d.drawString(""+((int)(view.getNMIScore()*100000)/100000.0), 30, 150);
        g2d.drawString("Clustering time:", 30, 180);
        g2d.drawString(view.getClusteringTime() + " ms", 30, 200);
        g2d.drawString("a", 30, 300);
    }

    protected void valueChanged() {
        if (aValueAdjuster.hasValue()) {
            view.showClustering(aValueAdjuster.getValue());
        }
    }

    protected void update() {
        repaint();
    }

    protected void update(int n) {
        aValueAdjuster.setMaximumValue(n);
        aValueAdjuster.setEnabled(true);
        update();
    }

    protected void updateAValue(int a) {
        aValueAdjuster.setValue(a);
    }

    protected void setBounds() {
        setBounds(view.windowWidth - view.sidePanelWidth, view.topPanelHeight, view.windowWidth, view.windowHeight);
        aValueAdjuster.setBounds(30, 300, 100, 130);
    }

}