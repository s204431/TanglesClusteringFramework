package view;

import util.ValueAdjuster;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SpectralSidePanel extends SidePanel {

    private ValueAdjuster kValueAdjuster; //Slider for the "k" parameter.
    private ValueAdjuster sigmaValueAdjuster;

    public SpectralSidePanel(View view) {
        super(view);

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        kValueAdjuster = new ValueAdjuster(1, 20);
        kValueAdjuster.setEnabled(false);
        kValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                repaint();
            }
        });
        add(kValueAdjuster);
        sigmaValueAdjuster = new ValueAdjuster(1, 100);
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

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawString("k", 30, 235);
        g2d.drawString("sigma", 30, 345);
    }

    protected void updateKValue(int k) {
        kValueAdjuster.setValue(k);
    }

    protected void valueChanged() {
        if (kValueAdjuster.hasValue() && sigmaValueAdjuster.hasValue()) {
            view.showClusteringSpectral(kValueAdjuster.getValue(), sigmaValueAdjuster.getValue());
        }
    }

    protected void update(int n) {
        kValueAdjuster.setMaximumValue(100);
        kValueAdjuster.setEnabled(view.plottingView.getNumberOfPoints() > 0);
        sigmaValueAdjuster.setMaximumValue(n/2);
        sigmaValueAdjuster.setEnabled(view.plottingView.getNumberOfPoints() > 0);
        repaint();
    }

    protected void setBounds() {
        super.setBounds();
        kValueAdjuster.setBounds(30, 240, 100, 130);
        sigmaValueAdjuster.setBounds(30, 350, 100, 130);
    }
}
