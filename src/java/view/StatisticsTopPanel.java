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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
                Font errorFont = new Font("TimesRoman", Font.BOLD, 12);

                //Create panel with scroll pane of test cases
                JPanel testSetPane = new JPanel();
                testSetPane.setLayout(new BoxLayout(testSetPane, BoxLayout.PAGE_AXIS));
                //Tests title
                JLabel testCaseTitle = new JLabel("Test cases");
                testCaseTitle.setFont(titleFont);
                testCaseTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                testSetPane.add(testCaseTitle);
                testSetPane.add(Box.createRigidArea(new Dimension(0, 20)));
                //Error label
                JLabel testSetErrorLabel = new JLabel("Table values cannot be empty or zero.");
                testSetErrorLabel.setAlignmentX(CENTER_ALIGNMENT);
                testSetErrorLabel.setFont(errorFont);
                testSetErrorLabel.setForeground(Color.RED);
                testSetPane.add(testSetErrorLabel);
                testSetErrorLabel.setVisible(false);
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
                //Error label
                JLabel algorithmsErrorLabel = new JLabel("Please select an algorithm");
                algorithmsErrorLabel.setAlignmentX(CENTER_ALIGNMENT);
                algorithmsErrorLabel.setFont(errorFont);
                algorithmsErrorLabel.setForeground(Color.RED);
                checkBoxPane.add(algorithmsErrorLabel);
                algorithmsErrorLabel.setVisible(false);
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
                checkBoxPane.add(Box.createRigidArea(new Dimension(0,30)));

                //Collect testSetPane and checkBoxPane side to side in runPane
                JPanel runPane = new JPanel();
                runPane.setLayout(new BoxLayout(runPane, BoxLayout.LINE_AXIS));
                runPane.add(testSetPane);
                runPane.add(Box.createRigidArea(new Dimension(20, 0)));
                runPane.add(checkBoxPane);

                generateTable(testSet);

                //JOption pane with options for running, resetting, loading and saving a test set.
                final JOptionPane optionPane = new JOptionPane(runPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, new Object[]{}, null);
                final JDialog dialog = new JDialog(view, "Choose test set", true);
                dialog.setContentPane(optionPane);
                dialog.pack();
                dialog.setLocationRelativeTo(null);

                //Run and cancel button
                JButton runButton = new JButton("Run");
                runButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //Prevent running of test set if table has 0 values or if no checkboxes are selected.
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                Object value = table.getValueAt(i,j);
                                if (value == null || value.toString().equals("") || value.toString().equals("0")) {
                                    testSetErrorLabel.setVisible(true);
                                    algorithmsErrorLabel.setVisible(false);
                                    return;
                                }
                            }
                        }
                        testSetErrorLabel.setVisible(false);

                        if (!(tangleCheckBox.isSelected() || kMeansCheckBox.isSelected() || spectralCheckBox.isSelected() || linkageCheckBox.isSelected())) {
                            System.out.println("Please select a checkbox");
                            algorithmsErrorLabel.setVisible(true);
                            return;
                        }
                        testSetErrorLabel.setVisible(false);

                        //Save test set
                        String datatype = comboBox.getSelectedItem().toString();
                        testSet = convertToTestSet(datatype, table);

                        dialog.setVisible(false);

                        //Run test set
                        view.startStatisticsPanelRunPhase();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                setVisible(false);
                                String[] algorithmsNames = getAlgorithmNames(new boolean[]{tangleCheckBox.isSelected(), kMeansCheckBox.isSelected(), spectralCheckBox.isSelected(), linkageCheckBox.isSelected()});
                                double[][][] testResults = ClusteringTester.runTest(testSet, algorithmsNames);
                                view.plotTestResults(testResults,testSet,algorithmsNames);
                                view.endStatisticsPanelRunPhase();
                                setVisible(true);
                            }
                        }).start();
                    }
                });
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //Save test set
                        String datatype = comboBox.getSelectedItem().toString();
                        testSet = convertToTestSet(datatype, table);

                        dialog.setVisible(false);
                    }
                });

                JPanel runCancelPane = new JPanel();
                runCancelPane.setLayout(new BoxLayout(runCancelPane, BoxLayout.LINE_AXIS));
                runCancelPane.add(runButton);
                runCancelPane.add(Box.createRigidArea(new Dimension(20,0)));
                runCancelPane.add(cancelButton);

                testSetPane.add(Box.createRigidArea(new Dimension(0, 10)));
                testSetPane.add(runCancelPane);

                dialog.setVisible(true);
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
        int[] values = new int[4];  //nPoints, nDimensions, nClusters, nRuns
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < 4; j++) {
                Object value = table.getValueAt(i,j);
                values[j] = value != null ? parseInt(value.toString()) : 0;
            }
            testSet.add(new TestCase(values[0], values[1], values[2], values[3]));
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
