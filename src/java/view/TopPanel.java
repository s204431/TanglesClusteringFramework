package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;

public class TopPanel extends JPanel {
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 30;

    private View view;

    private JToolBar toolBar;

    private JButton newButton;
    private JPopupMenu newPopup;
    private JButton exportButton;
    private JButton algorithmButton;

    public TopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        toolBar = new JToolBar();
        toolBar.setLayout(null);

        createNewButton();
        createExportButton();
        createAlgorithmButton();

        add(toolBar);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void createNewButton() {
        //Create the popup menu
        newPopup = new JPopupMenu();
        newPopup.add(new JMenuItem(new AbstractAction("Load new dataset") {
            public void actionPerformed(ActionEvent e) {
                File folder = new File("datasets");
                File[] matchingFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".csv");
                    }
                });

                String[] loadableDatasets = new String[matchingFiles.length];
                for (int i = 0; i < matchingFiles.length; i++) {
                    loadableDatasets[i] = matchingFiles[i].getName().substring(0, matchingFiles[i].getName().length() - 4);
                }

                //Open loading frame
                JFrame loadPopupFrame = new JFrame();
                loadPopupFrame.setPreferredSize(new Dimension(300, 400));
                loadPopupFrame.setTitle("Load");

                JPanel loadPopupPanel = new JPanel();

                JList loadList = new JList(loadableDatasets);
                loadList.addMouseListener(new MouseAdapter() {
                      @Override
                      public void mouseClicked(MouseEvent e) {
                          if (e.getClickCount() == 2) {
                              //Load selected dataset and close loading frame
                              System.out.println("Clicked " + loadList.getSelectedValue());
                              loadPopupFrame.dispatchEvent(new WindowEvent(loadPopupFrame, WindowEvent.WINDOW_CLOSING));
                          }
                      }
                });
                loadPopupPanel.add(loadList);

                loadPopupFrame.add(loadPopupPanel);
                loadPopupFrame.pack();
                loadPopupFrame.setLocationByPlatform(true);
                loadPopupFrame.setLocationRelativeTo(null);
                loadPopupFrame.setVisible(true);
            }
        }));
        newPopup.add(new JMenuItem(new AbstractAction("Create new dataset") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view, "Create new dataset here...");
            }
        }));

        //Create button
        newButton = new JButton("New");
        newButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                newPopup.show(e.getComponent(), 0, newButton.getY() + BUTTON_HEIGHT);
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

    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        newButton.setBounds(BUTTON_HEIGHT, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        exportButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        algorithmButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH * 2, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

}
