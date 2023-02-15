package view;

import util.ValueAdjuster;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class TangleSidePanel extends SidePanel {

    private ValueAdjuster aValueAdjuster; //Slider for the "a" parameter.

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
            view.showClustering(aValueAdjuster.getValue());
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
    }
}
