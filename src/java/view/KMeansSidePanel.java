package view;

import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class KMeansSidePanel extends SidePanel {

    //Responsible: Michael

    //This class is the side panel associated with the k-means clustering algorithm.

    private ValueAdjuster kValueAdjuster; //Slider for the "k" parameter.

    //Constructor receiving view.
    public KMeansSidePanel(View view) {
        super(view);

        JLabel kLabel = new JLabel("k");
        kLabel.setFont(super.font);
        kLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(kLabel);

        kValueAdjuster = new ValueAdjuster(2, 20);
        kValueAdjuster.setMaximumSize(new Dimension(view.sidePanelWidth - view.sidePanelWidth / 2, view.windowHeight / 6));
        kValueAdjuster.setEnabled(false);
        kValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                repaint();
            }
        });
        add(kValueAdjuster);

    }

    //Clusters the data set when the slider in k-means side panel has been changed.
    protected void valueChanged() {
        if (kValueAdjuster.hasValue()) {
            view.controller.generateClusteringKMeans(kValueAdjuster.getValue());
        }
    }

    //Updates kValueAdjuster based on n and determines if the user should be able to interact with it.
    protected void update(int n) {
        kValueAdjuster.setMaximumValue(100);
        kValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        repaint();
    }

    //Sets the bounds of the k-means side panel.
    protected void setBounds() {
        super.setBounds();
    }
}
