package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopPanel extends JPanel {

    private View view;

    private JToolBar toolBar;

    private JPopupMenu testPopup;
    private JButton testButton;
    private JButton newButton;
    private JButton exportButton;
    private JButton algorithmButton;
    private JButton datasetButton;

    public TopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        toolBar = new JToolBar();
        toolBar.setLayout(null);

        createNewButton();
        createExportButton();
        createAlgorithmButton();
        createDatasetButton();

        add(toolBar);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void createNewButton() {
        newButton = new JButton("New");
        newButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
        });
        toolBar.add(newButton);
    }

    private void createExportButton() {
        exportButton = new JButton("Export");
        exportButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
        });
        toolBar.add(exportButton);
    }

    private void createAlgorithmButton() {
        algorithmButton = new JButton("Algorithm");
        algorithmButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
        });
        toolBar.add(algorithmButton);
    }

    private void createDatasetButton() {
        datasetButton = new JButton("Create dataset");
        datasetButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
        });
        toolBar.add(datasetButton);
    }

    private void createTestButton() {
        //Create the popup menu.
        testPopup = new JPopupMenu();
        testPopup.add(new JMenuItem(new AbstractAction("Test 1") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view, "Test 1 selected");
            }
        }));
        testPopup.add(new JMenuItem(new AbstractAction("Test 2") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view, "Test 2 selected");
            }
        }));

        testButton = new JButton("Tests");
        testButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                testPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        toolBar.add(testButton);
    }

    protected void setBounds() {
        int buttonWidth = 100;
        int buttonHeight = 30;

        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        newButton.setBounds(buttonHeight, view.topPanelHeight / 2 - buttonHeight / 2, buttonWidth, buttonHeight);
        exportButton.setBounds(buttonHeight + buttonWidth, view.topPanelHeight / 2 - buttonHeight / 2, buttonWidth, buttonHeight);
        datasetButton.setBounds(buttonHeight + buttonWidth * 2, view.topPanelHeight / 2 - buttonHeight / 2, buttonWidth, buttonHeight);
        algorithmButton.setBounds(buttonHeight + buttonWidth * 3, view.topPanelHeight / 2 - buttonHeight / 2, buttonWidth, buttonHeight);
    }

}
