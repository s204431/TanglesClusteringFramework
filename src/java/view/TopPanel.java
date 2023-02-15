package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopPanel extends JPanel {
    private View view;

    private JToolBar toolBar;
    private JPopupMenu popup;
    private JButton button;

    public TopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);


        toolBar = new JToolBar();
        toolBar.setLayout(null);

        //Create the popup menu.
        popup = new JPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Test 1") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view, "Test 1 selected");
            }
        }));
        popup.add(new JMenuItem(new AbstractAction("Test 2") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view, "Test 2 selected");
            }
        }));

        button = new JButton("Tests");
        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        toolBar.add(button);
        add(toolBar);

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        button.setBounds(30, view.topPanelHeight / 2 - 25, 100, 50);
    }

}
