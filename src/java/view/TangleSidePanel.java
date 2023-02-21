package view;

import datasets.FeatureBasedDataset;
import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class TangleSidePanel extends SidePanel {

    private ValueAdjuster aValueAdjuster; //Slider for the "a" parameter.
    private JCheckBox showCutsCheckBox;

    public TangleSidePanel(View view) {
        super(view);

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        aValueAdjuster = new ValueAdjuster(1, 80);
        aValueAdjuster.setEnabled(false);
        aValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                repaint();
            }
        });
        add(aValueAdjuster);
        if (view.getDataset() instanceof FeatureBasedDataset && ((FeatureBasedDataset) view.getDataset()).dataPoints[0].length <= 2) {
            showCutsCheckBox = new JCheckBox("Show Cuts");
            showCutsCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    view.plottingView.repaint();
                }
            });
            add(showCutsCheckBox);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawString("a", 30, 300);
    }

    protected void updateAValue(int a) {
        aValueAdjuster.setValue(a);
    }

    protected void valueChanged() {
        if (aValueAdjuster.hasValue()) {
            view.showClustering(aValueAdjuster.getValue(), -1);
        }
    }

    protected void update(int n) {
        aValueAdjuster.setMaximumValue(n);
        aValueAdjuster.setEnabled(view.plottingView.getNumberOfPoints() > 0);
        repaint();
    }

    protected void setBounds() {
        super.setBounds();
        aValueAdjuster.setBounds(30, 300, 100, 130);
        if (showCutsCheckBox != null) {
            showCutsCheckBox.setBounds(30, 400, 100, 50);
        }
    }

    protected boolean showCuts() {
        return showCutsCheckBox == null ? false : showCutsCheckBox.isSelected();
    }
}
