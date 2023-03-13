package view;

import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SpectralSidePanel extends SidePanel {

    private ValueAdjuster kValueAdjuster; //Slider for the "k" parameter.
    private ValueAdjuster sigmaValueAdjuster;

    public SpectralSidePanel(View view) {
        super(view);

        add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel kLabel = new JLabel("k");
        kLabel.setFont(new Font("TimesRoman", Font.BOLD, 18));
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
        sigmaLabel.setFont(new Font("TimesRoman", Font.BOLD, 18));
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

    protected void updateKValue(int k) {
        kValueAdjuster.setValue(k);
    }

    protected void valueChanged() {
        if (kValueAdjuster.hasValue() && sigmaValueAdjuster.hasValue()) {
            view.controller.generateClusteringSpectral(kValueAdjuster.getValue(), sigmaValueAdjuster.getValue());
        }
    }

    protected void update(int n) {
        kValueAdjuster.setMaximumValue(100);
        kValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        sigmaValueAdjuster.setMaximumValue(n/2);
        sigmaValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        repaint();
    }

    protected void setBounds() {
        super.setBounds();
        kValueAdjuster.setBounds(30, 240, 100, 130);
        sigmaValueAdjuster.setBounds(30, 350, 100, 130);
    }
}
