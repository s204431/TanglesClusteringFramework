package view;

import javax.swing.*;
import java.awt.*;

public class StatisticsPanel extends JPanel {
    private View view;

    public StatisticsPanel(View view) {
        this.view = view;
        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);


    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(10, 10, 100, 100);
    }


}
