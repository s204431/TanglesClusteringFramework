package view;

import util.ValueAdjuster;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {
    private View view;
    private ValueAdjuster aValueAdjuster;

    public SidePanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        aValueAdjuster = new ValueAdjuster();
        aValueAdjuster.setBounds(50, 200, 100, 130);
        aValueAdjuster.performOnChange(() -> {
            int value = aValueAdjuster.getValue();
            if (value >= 0) {
                view.changeAValue(value);
                repaint();
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
        g2d.drawString("NMI score:", 50, 30);
        g2d.drawString(""+((int)(view.getNMIScore()*100000)/100000.0), 50, 50);
        g2d.drawString("Clustering time:", 50, 80);
        g2d.drawString(view.getClusteringTime() + " ms", 50, 100);
        g2d.drawString("a", 50, 200);
    }

    protected void update() {
        repaint();
    }

    protected void update(int n) {
        aValueAdjuster.setMaximumValue(n);
        update();
    }

    protected void updateAValue(int a) {
        aValueAdjuster.setValue(a);
    }

}