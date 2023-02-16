package view;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.text.NumberFormat;

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

        addNewButton();
        addExportButton();
        addAlgorithmButton();

        add(toolBar);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void addNewButton() {
        newPopup = new JPopupMenu();

        //Create new dataset
        newPopup.add(new JMenuItem(new AbstractAction("Create new dataset") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view, "Create new dataset here...");
            }
        }));

        //Load dataset
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

                JList loadList = new JList(loadableDatasets);
                JScrollPane scrollPane = new JScrollPane(loadList);
                JPanel loadPopupPanel = new JPanel();
                loadPopupPanel.add(scrollPane);

                if (loadableDatasets.length > 0) {
                    loadList.setSelectedIndex(0);
                }

                int loadResult = JOptionPane.showConfirmDialog(view, loadPopupPanel,
                        "Load dataset", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                Object selectedFile = loadList.getSelectedValue();

                if (loadResult == JOptionPane.OK_OPTION && selectedFile != null) {
                    String fileName = selectedFile.toString();

                    NumberFormat format = NumberFormat.getInstance();
                    format.setGroupingUsed(false);
                    NumberFormatter formatter = new NumberFormatter(format);
                    formatter.setValueClass(Integer.class);
                    formatter.setMinimum(-1);
                    formatter.setMaximum(Integer.MAX_VALUE);
                    formatter.setAllowsInvalid(false);
                    // If you want the value to be committed on each keystroke instead of focus lost
                    formatter.setCommitsOnValidEdit(true);

                    JFormattedTextField startRowTextField = new JFormattedTextField(formatter);
                    JFormattedTextField endRowTextField = new JFormattedTextField(formatter);
                    JFormattedTextField startColTextField = new JFormattedTextField(formatter);
                    JFormattedTextField endColTextField = new JFormattedTextField(formatter);
                    startRowTextField.setValue(0);
                    endRowTextField.setValue(-1);
                    startColTextField.setValue(0);
                    endColTextField.setValue(-1);

                    JPanel textPane1 = new JPanel();
                    textPane1.setLayout(new BoxLayout(textPane1, BoxLayout.PAGE_AXIS));
                    textPane1.add(new JLabel("Start row: "));
                    textPane1.add(new JLabel("Start column: "));

                    JPanel textPane2 = new JPanel();
                    textPane2.setLayout(new BoxLayout(textPane2, BoxLayout.PAGE_AXIS));
                    textPane2.add(new JLabel("End row: "));
                    textPane2.add(new JLabel("End column: "));

                    JPanel valuePane1 = new JPanel();
                    valuePane1.setLayout(new BoxLayout(valuePane1, BoxLayout.PAGE_AXIS));
                    valuePane1.add(startRowTextField);
                    valuePane1.add(startColTextField);

                    JPanel valuePane2 = new JPanel();
                    valuePane2.setLayout(new BoxLayout(valuePane2, BoxLayout.PAGE_AXIS));
                    valuePane2.add(endRowTextField);
                    valuePane2.add(endColTextField);

                    JPanel parameterPopupPanel = new JPanel();
                    parameterPopupPanel.setLayout(new BoxLayout(parameterPopupPanel, BoxLayout.LINE_AXIS));
                    parameterPopupPanel.add(textPane1);
                    parameterPopupPanel.add(valuePane1);
                    parameterPopupPanel.add(Box.createRigidArea(new Dimension(20, 0)));
                    parameterPopupPanel.add(textPane2);
                    parameterPopupPanel.add(valuePane2);

                    int parameterResult = JOptionPane.showConfirmDialog(view, parameterPopupPanel,
                            ("Choose loading parameters for " + selectedFile), JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);

                    if (parameterResult == JOptionPane.OK_OPTION) {
                        int startRow = Integer.parseInt(startRowTextField.getValue().toString());
                        int endRow = Integer.parseInt(endRowTextField.getValue().toString());
                        int startCol = Integer.parseInt(startColTextField.getValue().toString());
                        int endCol = Integer.parseInt(endColTextField.getValue().toString());
                        loadDataset(fileName, startRow, endRow, startCol, endCol);
                    }
                }
            }
        }));

        //Create button on toolbar
        newButton = new JButton("New");
        newButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                newPopup.show(e.getComponent(), 0, newButton.getY() + BUTTON_HEIGHT);
            }
        });
        toolBar.add(newButton);
    }

    private void addExportButton() {
        exportButton = new JButton("Export");
        exportButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
        });
        toolBar.add(exportButton);
    }

    private void addAlgorithmButton() {
        algorithmButton = new JButton("Algorithm");
        algorithmButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
        });
        toolBar.add(algorithmButton);
    }

    private JButton createOkayButton(JPanel panel) {
        JButton okayButton = new JButton("Okay");
        okayButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        return okayButton;
    }

    private JButton createCancelButton(JPanel panel) {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        return cancelButton;
    }

    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        newButton.setBounds(BUTTON_HEIGHT, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        exportButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        algorithmButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH * 2, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void loadDataset(String file, int startRow, int endRow, int startCol, int endCol) {
        System.out.println("Load " + file + " here...");
    }

}
