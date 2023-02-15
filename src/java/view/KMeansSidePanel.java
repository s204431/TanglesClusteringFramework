package view;

import util.ValueAdjuster;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class KMeansSidePanel extends SidePanel {

    private static final int MAX_K = 20;

    private ValueAdjuster kValueAdjuster; //Slider for the "k" parameter.

    public KMeansSidePanel(View view) {
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
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawString("k", 30, 300);
    }

    protected void updateKValue(int k) {
        kValueAdjuster.setValue(k);
    }

    protected void valueChanged() {
        if (kValueAdjuster.hasValue()) {
            view.showClusteringKMeans(kValueAdjuster.getValue());
        }
    }

    protected void update(int n) {
        kValueAdjuster.setMaximumValue(100);
        kValueAdjuster.setEnabled(view.plottingView.getNumberOfPoints() > 0);
        repaint();
    }

    protected void setBounds() {
        super.setBounds();
        kValueAdjuster.setBounds(30, 300, 100, 130);
    }
}
