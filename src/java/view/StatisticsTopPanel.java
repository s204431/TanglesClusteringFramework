package view;

import datasets.Dataset;
import datasets.GraphDataset;
import model.Model;
import smile.data.type.DataType;
import smile.swing.Table;
import smile.swing.table.ButtonCellRenderer;
import test.ClusteringTester;
import test.TestCase;
import test.TestSet;

import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import static view.TopPanel.BUTTON_HEIGHT;
import static view.TopPanel.BUTTON_WIDTH;

public class StatisticsTopPanel extends JPanel {

    private View view;

    JToolBar toolBar;

    JButton runButton;
    JButton plottingButton;

    TestSet testSet = null;
    JComboBox<String> comboBox;
    JTable table = new JTable();

    protected StatisticsTopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        toolBar = new JToolBar();
        toolBar.setLayout(null);

        addRunButton();
        addPlottingButton();

        add(toolBar);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void addRunButton() {
        runButton = new JButton("Run");
        runButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Font titleFont = new Font("TimesRoman", Font.BOLD, 25);
                Font subTitleFont = new Font("TimesRoman", Font.BOLD, 18);

                //Create panel with scroll pane of test cases
                JPanel testSetPane = new JPanel();
                testSetPane.setLayout(new BoxLayout(testSetPane, BoxLayout.PAGE_AXIS));
                //Tests title
                JLabel testCaseLabel = new JLabel("Test cases");
                testCaseLabel.setFont(titleFont);
                testCaseLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                testSetPane.add(testCaseLabel);
                testSetPane.add(Box.createRigidArea(new Dimension(0, 20)));
                //Table of test cases
                table = new JTable();
                DefaultTableModel tableModel = new CustomTableModel();
                tableModel.setColumnIdentifiers(new String[] {"Points", "Dimensions", "Clusters", "Runs", "", ""});

                table.setModel(tableModel);
                TableCellRenderer tableCellRenderer1 = new ButtonCellRenderer(table, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tableModel.addRow(new Object[] {"", "", "", "", "+", "-"});
                        for (int i = table.getRowCount()-1; i >= table.getSelectedRow()+1; i--) {
                            for (int j = 0; j < 4; j++) {
                                table.setValueAt(table.getValueAt(i-1, j), i, j);
                            }
                        }
                        for (int i = 0; i < 4; i++) {
                            table.setValueAt("", table.getSelectedRow()+1, i);
                        }
                    }
                }, 4);
                TableCellRenderer tableCellRenderer2 = new ButtonCellRenderer(table, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (table.getRowCount() > 1) {
                            tableModel.removeRow(table.getSelectedRow());
                        }
                    }
                }, 5);
                table.getColumnModel().getColumn(4).setCellRenderer(tableCellRenderer1);
                table.getColumnModel().getColumn(5).setCellRenderer(tableCellRenderer2);
                table.getColumnModel().getColumn(4).setPreferredWidth(40);
                table.getColumnModel().getColumn(5).setPreferredWidth(40);
                table.setFillsViewportHeight(true);
                JScrollPane tablePane = new JScrollPane(table);
                testSetPane.add(tablePane);
                //Drop down menu for choosing type of dataset
                comboBox = new JComboBox<>();
                for (String type : Dataset.supportedDatasetTypes) {
                    comboBox.addItem(type);
                }
                testSetPane.add(Box.createRigidArea(new Dimension(0, 10)));
                testSetPane.add(comboBox);

                //Create panel with checkmarks for algorithms to run
                JPanel checkBoxPane = new JPanel();
                checkBoxPane.setLayout(new BoxLayout(checkBoxPane, BoxLayout.PAGE_AXIS));
                //Algorithms subtitle
                JLabel algorithmsLabel = new JLabel("Algorithms");
                algorithmsLabel.setFont(subTitleFont);
                JPanel algorithmTitlePane = new JPanel();
                algorithmTitlePane.add(algorithmsLabel);
                checkBoxPane.add(Box.createRigidArea(new Dimension(0, 20 + titleFont.getSize())));
                checkBoxPane.add(algorithmTitlePane);
                //Checkboxes for available algorithms to run on test set
                JCheckBox tangleCheckBox = new JCheckBox();
                JCheckBox kMeansCheckBox = new JCheckBox();
                JCheckBox spectralCheckBox = new JCheckBox();
                JCheckBox linkageCheckBox = new JCheckBox();
                checkBoxPane.add(createCheckBoxPanel(tangleCheckBox, "Tangle"));
                checkBoxPane.add(createCheckBoxPanel(kMeansCheckBox, "K-Means"));
                checkBoxPane.add(createCheckBoxPanel(spectralCheckBox, "Spectral Clustering"));
                checkBoxPane.add(createCheckBoxPanel(linkageCheckBox, "Linkage"));
                checkBoxPane.add(Box.createRigidArea(new Dimension(0, 100)));
                //Buttons for resetting, saving and loading test set
                JButton resetButton = new JButton("Reset");
                resetButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (int i = 0; i < table.getRowCount(); i++) {
                            generateTable(null);
                        }
                    }
                });
                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final File[] file = new File[1];
                        JPanel savePopupPanel = new JPanel();
                        savePopupPanel.setLayout(new BoxLayout(savePopupPanel, BoxLayout.LINE_AXIS));
                        JLabel label = new JLabel("No file selected");
                        JButton fileButton = new JButton("Select File");
                        fileButton.addActionListener((l) -> {
                            final JFileChooser fc = new JFileChooser();
                            String extension = "test";
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("."+extension, extension);
                            fc.setFileFilter(filter);
                            int returnVal = fc.showDialog(view, "Choose");
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                file[0] = fc.getSelectedFile();
                                if (!file[0].getName().endsWith("."+extension)) {
                                    file[0] = new File(file[0].getPath()+"."+extension);
                                }
                                label.setText(file[0].getName());
                            }
                        });
                        savePopupPanel.add(label);
                        savePopupPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                        savePopupPanel.add(fileButton);

                        int saveResult = JOptionPane.showConfirmDialog(view, savePopupPanel,
                                "Save test set", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);

                        if (saveResult == JOptionPane.OK_OPTION && file[0] != null) {
                            //Save test set
                            String datatype = comboBox.getSelectedItem().toString();
                            testSet = convertToTestSet(datatype, table);
                            TestSet.saveTestSet(testSet, file[0]);
                        }
                    }
                });
                JButton loadButton = new JButton("Load");
                loadButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final File[] file = new File[1];
                        JPanel loadPopupPanel = new JPanel();
                        loadPopupPanel.setLayout(new BoxLayout(loadPopupPanel, BoxLayout.LINE_AXIS));
                        JLabel label = new JLabel("No file selected");
                        JButton fileButton = new JButton("Select File");
                        fileButton.addActionListener((l) -> {
                            final JFileChooser fc = new JFileChooser();
                            String extension = "test";
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("."+extension, extension);
                            fc.setFileFilter(filter);
                            int returnVal = fc.showDialog(view, "Choose");
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                file[0] = fc.getSelectedFile();
                                label.setText(file[0].getName());
                            }
                        });
                        loadPopupPanel.add(label);
                        loadPopupPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                        loadPopupPanel.add(fileButton);

                        int loadResult = JOptionPane.showConfirmDialog(view, loadPopupPanel,
                                "Load test set", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);

                        if (loadResult == JOptionPane.OK_OPTION && file[0] != null) {
                            //Load test set
                            testSet = TestSet.loadTestSet(file[0]);
                            generateTable(testSet);
                        }
                    }
                });
                JPanel resetPane = new JPanel();
                resetPane.add(resetButton);
                checkBoxPane.add(Box.createRigidArea(new Dimension(0, 10)));
                checkBoxPane.add(resetPane);
                checkBoxPane.add(Box.createRigidArea(new Dimension(0, 10)));
                JPanel saveLoadPane = new JPanel();
                saveLoadPane.setLayout(new BoxLayout(saveLoadPane, BoxLayout.LINE_AXIS));
                saveLoadPane.add(Box.createHorizontalGlue());
                saveLoadPane.add(saveButton);
                saveLoadPane.add(Box.createRigidArea(new Dimension(10,0)));
                saveLoadPane.add(loadButton);
                saveLoadPane.add(Box.createHorizontalGlue());
                checkBoxPane.add(saveLoadPane);


                //Collect testSetPane and checkBoxPane side to side in a single panel
                JPanel runPane = new JPanel();
                runPane.setLayout(new BoxLayout(runPane, BoxLayout.LINE_AXIS));
                runPane.add(testSetPane);
                runPane.add(Box.createRigidArea(new Dimension(20, 0)));
                runPane.add(checkBoxPane);

                generateTable(testSet);

                //JOption pane with options for running, resetting, loading and saving a test set.
                String[] options = new String[] { "Run", "Cancel" };
                int response = JOptionPane.showOptionDialog(view, runPane, "Choose test set",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, options[0]);

                //Save test set
                String datatype = comboBox.getSelectedItem().toString();
                testSet = convertToTestSet(datatype, table);

                if (response == 0) {
                    String[] algorithmsNames = getAlgorithmNames(new boolean[] { tangleCheckBox.isSelected(), kMeansCheckBox.isSelected(), spectralCheckBox.isSelected(), linkageCheckBox.isSelected() });
                    double[][][] testResults = ClusteringTester.runTest(testSet, algorithmsNames);
                    view.plotTestResults(testResults, testSet, algorithmsNames);
                }
            }
        });
        toolBar.add(runButton);
    }

    private String[] getAlgorithmNames(boolean[] algorithmsToRun) {
        String[] algorithms = { Model.tangleName, Model.kMeansName, Model.spectralClusteringName, Model.linkageName };

        int length = 0;
        for (boolean b : algorithmsToRun) {
            if (b) {
                length++;
            }
        }

        String[] result = new String[length];
        length = 0;
        for (int i = 0; i < algorithmsToRun.length; i++) {
            if (algorithmsToRun[i]) {
                result[length] = algorithms[i];
                length++;
            }
        }
        return result;
    }

    private void addPlottingButton() {
        plottingButton = new JButton("Scatter plot");
        plottingButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                view.switchToPlotting();
            }
        });
        toolBar.add(plottingButton);
    }

    private JPanel createCheckBoxPanel(JCheckBox checkBox, String text) {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BorderLayout());
        checkBoxPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });
        checkBoxPanel.add(checkBox, BorderLayout.WEST);
        checkBoxPanel.add(new JLabel(text), BorderLayout.CENTER);
        //checkBoxPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return checkBoxPanel;
    }

    private void generateTable(TestSet testSet) {
        CustomTableModel model = (CustomTableModel) table.getModel();
        for (int i = table.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        if (testSet == null) {
            model.addRow(new Object[]{"", "", "", "", "+", "-"});
            comboBox.setSelectedItem("Feature Based");
        } else {
            for (int i = 0; i < testSet.size(); i++) {
                TestCase testCase = testSet.get(i);
                model.addRow(new Object[]{convertInteger(testCase.nPoints), convertInteger(testCase.nDimensions), convertInteger(testCase.nClusters), convertInteger(testCase.nRuns), "+", "-"});
            }
        }
        if (testSet != null) {
            comboBox.setSelectedItem(testSet.dataTypeName);
        }
    }

    private Object convertInteger(int integer) {
        return integer == 0 ? "" : integer;
    }

    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        runButton.setBounds(BUTTON_HEIGHT, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        plottingButton.setBounds(view.windowWidth - view.sidePanelWidth - BUTTON_WIDTH, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public TestSet convertToTestSet(String dataTypeName, JTable table) {
        TestSet testSet = new TestSet(dataTypeName);
        for (int i = 0; i < table.getRowCount(); i++) {
            int nPoints = parseInt(table.getValueAt(i, 0).toString());
            int nDimensions = parseInt(table.getValueAt(i, 1).toString());
            int nClusters = parseInt(table.getValueAt(i, 2).toString());
            int nRuns = parseInt(table.getValueAt(i, 3).toString());
            testSet.add(new TestCase(nPoints, nDimensions, nClusters, nRuns));
        }
        return testSet;
    }

    private int parseInt(String string) {
        return string.equals("") ? 0 : Integer.parseInt(string);
    }

    private static class CustomTableModel extends DefaultTableModel {
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 4) {
                return JButton.class;
            }
            return Integer.class;
        }
    }
}
