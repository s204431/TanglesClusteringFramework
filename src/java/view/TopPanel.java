package view;

import javax.swing.*;
import java.awt.*;

public class TopPanel extends JPanel {
    View view;

    public TopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);
    }

    //Draws everything on screen.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 100, 100);
    }

}
