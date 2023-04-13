package view;

import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SpectralSidePanel extends SidePanel {

    //Sliders for the "k" and "sigma" parameter in spectral clustering.
    private ValueAdjuster kValueAdjuster;
    private ValueAdjuster sigmaValueAdjuster;

    //Constructor receiving view.
    public SpectralSidePanel(View view) {
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

        JLabel sigmaLabel = new JLabel("sigma");
        sigmaLabel.setFont(super.font);
        sigmaLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(sigmaLabel);

        sigmaValueAdjuster = new ValueAdjuster(1, 100);
        sigmaValueAdjuster.setMaximumSize(new Dimension(view.sidePanelWidth - view.sidePanelWidth / 2, view.windowHeight / 6));
        sigmaValueAdjuster.setEnabled(false);
        sigmaValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                repaint();
            }
        });
        add(sigmaValueAdjuster);
    }

    //Clusters the data set when the sliders in spectral side panel has been changed.
    protected void valueChanged() {
        if (kValueAdjuster.hasValue() && sigmaValueAdjuster.hasValue()) {
            view.controller.generateClusteringSpectral(kValueAdjuster.getValue(), sigmaValueAdjuster.getValue());
        }
    }

    //Updates kValueAdjuster and sigmaValueAdjuster based on n and determines if the user should be able to interact with them.
    protected void update(int n) {
        kValueAdjuster.setMaximumValue(100);
        kValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        sigmaValueAdjuster.setMaximumValue(n/2);
        sigmaValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        repaint();
    }

    //Sets bounds of spectral side panel.
    protected void setBounds() {
        super.setBounds();
        kValueAdjuster.setBounds(30, 240, 100, 130);
        sigmaValueAdjuster.setBounds(30, 350, 100, 130);
    }
}
