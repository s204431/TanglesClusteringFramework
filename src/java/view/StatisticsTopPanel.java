package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static view.TopPanel.BUTTON_HEIGHT;
import static view.TopPanel.BUTTON_WIDTH;

public class StatisticsTopPanel extends JPanel {

    private View view;

    JToolBar toolBar;

    JButton plottingButton;

    protected StatisticsTopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        toolBar = new JToolBar();
        toolBar.setLayout(null);

        addPlottingButton();

        add(toolBar);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void addPlottingButton() {
        plottingButton = new JButton("Plotting");
        plottingButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                view.switchToPlotting();
            }
        });
        toolBar.add(plottingButton);
    }

    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        plottingButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH * 2, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
}
