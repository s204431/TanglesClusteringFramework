package view;

import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class LinkageSidePanel extends SidePanel {

    private static final int MAX_K = 20;

    private ValueAdjuster kValueAdjuster; //Slider for the "k" parameter.

    public LinkageSidePanel(View view) {
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
    }

    protected void updateKValue(int k) {
        kValueAdjuster.setValue(k);
    }

    protected void valueChanged() {
        if (kValueAdjuster.hasValue()) {
            view.controller.generateClusteringLinkage(kValueAdjuster.getValue());
        }
    }

    protected void update(int n) {
        kValueAdjuster.setMaximumValue(100);
        kValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        repaint();
    }

    protected void setBounds() {
        super.setBounds();
        kValueAdjuster.setBounds(30, 300, 100, 130);
    }
}
