package view;

import datasets.Dataset;
import smile.data.type.DataType;
import smile.swing.Table;
import smile.swing.table.ButtonCellRenderer;
import test.ClusteringTester;
import test.TestCase;
import test.TestSet;

import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static view.TopPanel.BUTTON_HEIGHT;
import static view.TopPanel.BUTTON_WIDTH;

public class StatisticsTopPanel extends JPanel {

    private View view;

    JToolBar toolBar;

    JButton runButton;
    JButton plottingButton;

    TestSet testSet = null;
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
                //TODO: ADD DROP DOWN MENU FOR TYPE OF DATASET
                JComboBox<String> comboBox = new JComboBox<>();
                for (String type : Dataset.supportedDatasetTypes) {
                    comboBox.addItem(type);
                }

                JPanel dataTypePanel = new JPanel();
                dataTypePanel.add(comboBox);

                String[] options = new String[] { "Run", "Cancel" };
                int dataTypeResponse = JOptionPane.showOptionDialog(view, dataTypePanel, "Choose type of test set",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, options[0]);

                if (dataTypeResponse != 1) {
                    String dataType = (String) comboBox.getSelectedItem();
                }

                Font titleFont = new Font("TimesRoman", Font.BOLD, 25);

                //Create panel with scroll pane of test cases
                JPanel testSetPane = new JPanel();

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
                testSetPane.setLayout(new BoxLayout(testSetPane, BoxLayout.PAGE_AXIS));
                //Title
                JLabel testCaseLabel = new JLabel("Test cases");
                testCaseLabel.setFont(titleFont);
                testCaseLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                testSetPane.add(testCaseLabel);
                //Table of test cases
                generateTable(testSet);
                testSetPane.add(tablePane);

                //Create panel with checkmarks for algorithms to run
                JPanel checkBoxPane = new JPanel();
                checkBoxPane.setLayout(new BoxLayout(checkBoxPane, BoxLayout.PAGE_AXIS));
                //Title
                JLabel algorithmsLabel = new JLabel("Algorithms");
                algorithmsLabel.setFont(titleFont);
                algorithmsLabel.setAlignmentX(LEFT_ALIGNMENT);
                checkBoxPane.add(algorithmsLabel);
                //Checkboxes for available algorithms to run on test set
                JCheckBox tangleCheckBox = new JCheckBox();
                JCheckBox kMeansCheckBox = new JCheckBox();
                JCheckBox spectralCheckBox = new JCheckBox();
                JCheckBox linkageCheckBox = new JCheckBox();
                checkBoxPane.add(createCheckBoxPanel(tangleCheckBox, "Tangle"));
                checkBoxPane.add(createCheckBoxPanel(kMeansCheckBox, "K-Means"));
                checkBoxPane.add(createCheckBoxPanel(spectralCheckBox, "Spectral Clustering"));
                checkBoxPane.add(createCheckBoxPanel(linkageCheckBox, "Linkage"));



                //Collect testSetPane and checkBoxPane side to side in a single panel
                JPanel runPane = new JPanel();
                runPane.setLayout(new BoxLayout(runPane, BoxLayout.LINE_AXIS));
                runPane.add(testSetPane);
                runPane.add(Box.createRigidArea(new Dimension(20, 0)));
                runPane.add(checkBoxPane);


                //JOption pane with options for running, resetting, loading and saving a test set.
                String[] options1 = new String[] { "Run", "Cancel" };
                int response = JOptionPane.showOptionDialog(view, runPane, "Choose test set",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options1, options1[0]);
            }
        });
        toolBar.add(runButton);
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

    private JPanel createCheckBoxPanel(JCheckBox checkBox, String text) {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setAlignmentX(LEFT_ALIGNMENT);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        checkBoxPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });
        checkBoxPanel.add(checkBox, BorderLayout.WEST);
        checkBoxPanel.add(new JLabel(text), BorderLayout.EAST);
        //checkBoxPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return checkBoxPanel;
    }

    private void generateTable(TestSet testSet) {
        CustomTableModel model  = (CustomTableModel)table.getModel();
        for (int i = 0; i < table.getRowCount(); i++) {
            model.removeRow(i);
        }
        if (testSet == null) {
            model.addRow(new Object[] {"", "", "", "", "+", "-"});
        }
        else {
            for (int i = 0; i < testSet.size(); i++) {
                TestCase testCase = testSet.get(i);
                model.addRow(new Object[] {testCase.nPoints, testCase.nDimensions, testCase.nClusters, testCase.nRuns, "+", "-"});
            }
        }
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
            int nPoints = (int)table.getValueAt(i, 0);
            int nDimensions = (int)table.getValueAt(i, 1);
            int nClusters = (int)table.getValueAt(i, 2);
            int nRuns = (int)table.getValueAt(i, 3);
            testSet.add(new TestCase(nPoints, nDimensions, nClusters, nRuns));
        }
        return testSet;
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
