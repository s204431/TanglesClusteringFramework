package view;

import datasets.Dataset;
import datasets.FeatureBasedDataset;
import model.Model;

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
import java.text.ParseException;

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
                JComboBox<String> comboBox = new JComboBox<>();
                for (String type : Dataset.supportedDatasetTypes) {
                    comboBox.addItem(type);
                }
                JPanel loadPopupPanel = new JPanel();
                loadPopupPanel.setLayout(new BoxLayout(loadPopupPanel, BoxLayout.PAGE_AXIS));
                loadPopupPanel.add(scrollPane);
                loadPopupPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                loadPopupPanel.add(comboBox);

                if (loadableDatasets.length > 0) {
                    loadList.setSelectedIndex(0);
                }

                int loadResult = JOptionPane.showConfirmDialog(view, loadPopupPanel,
                        "Load dataset", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                Object selectedFile = loadList.getSelectedValue();
                String datasetType = (String) comboBox.getSelectedItem();

                if (loadResult == JOptionPane.OK_OPTION && selectedFile != null) {
                    String fileName = selectedFile + ".csv";

                    NumberFormat format = NumberFormat.getIntegerInstance();
                    format.setGroupingUsed(false);
                    ExtendedNumberFormatter formatter = new ExtendedNumberFormatter(format);
                    formatter.setMinimum(-1);
                    formatter.setMaximum(Integer.MAX_VALUE);
                    formatter.setAllowsInvalid(false);

                    JFormattedTextField startRowTextField = new JFormattedTextField(formatter);
                    JFormattedTextField endRowTextField = new JFormattedTextField(formatter);
                    JFormattedTextField startColTextField = new JFormattedTextField(formatter);
                    JFormattedTextField endColTextField = new JFormattedTextField(formatter);
                    startRowTextField.setValue(0);
                    endRowTextField.setValue(-1);
                    startColTextField.setValue(0);
                    endColTextField.setValue(-1);

                    JPanel box1 = new JPanel();
                    box1.setLayout(new BoxLayout(box1, BoxLayout.LINE_AXIS));
                    JPanel box2 = new JPanel();
                    box2.setLayout(new BoxLayout(box2, BoxLayout.LINE_AXIS));

                    JPanel startRowPane = new JPanel();
                    startRowPane.setLayout(new BoxLayout(startRowPane, BoxLayout.PAGE_AXIS));
                    startRowPane.add(new JLabel("Start row:"));
                    startRowPane.add(startRowTextField);

                    JPanel endRowPane = new JPanel();
                    endRowPane.setLayout(new BoxLayout(endRowPane, BoxLayout.PAGE_AXIS));
                    endRowPane.add(new JLabel("End row:"));
                    endRowPane.add(endRowTextField);

                    JPanel startColumnPane = new JPanel();
                    startColumnPane.setLayout(new BoxLayout(startColumnPane, BoxLayout.PAGE_AXIS));
                    startColumnPane.add(new JLabel("Start column:"));
                    startColumnPane.add(startColTextField);

                    JPanel endColumnPane = new JPanel();
                    endColumnPane.setLayout(new BoxLayout(endColumnPane, BoxLayout.PAGE_AXIS));
                    endColumnPane.add(new JLabel("End column:"));
                    endColumnPane.add(endColTextField);

                    box1.add(startRowPane);
                    box1.add(Box.createRigidArea(new Dimension(5, 0)));
                    box1.add(endRowPane);
                    box2.add(startColumnPane);
                    box2.add(Box.createRigidArea(new Dimension(5, 0)));
                    box2.add(endColumnPane);

                    JPanel parameterPopupPanel = new JPanel();
                    parameterPopupPanel.setLayout(new BoxLayout(parameterPopupPanel, BoxLayout.PAGE_AXIS));
                    parameterPopupPanel.add(box1);
                    parameterPopupPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    parameterPopupPanel.add(box2);

                    int parameterResult = JOptionPane.showConfirmDialog(view, parameterPopupPanel,
                            ("Choose loading parameters for " + selectedFile), JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);

                    if (parameterResult == JOptionPane.OK_OPTION) {
                        int startRow = Integer.parseInt(startRowTextField.getValue().toString());
                        int endRow = Integer.parseInt(endRowTextField.getValue().toString());
                        int startCol = Integer.parseInt(startColTextField.getValue().toString());
                        int endCol = Integer.parseInt(endColTextField.getValue().toString());
                        view.loadDatasetFromFile(datasetType, fileName, startRow, endRow, startCol, endCol);
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

    private class ExtendedNumberFormatter extends NumberFormatter {
        public ExtendedNumberFormatter(NumberFormat f) {
            super(f);
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            if (text == "") {
                return null;
            }
            return super.stringToValue(text);
        }
    }

}
