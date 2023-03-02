package view;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.NumberEditorExt;
import smile.swing.Table;
import smile.swing.table.ButtonCellRenderer;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
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


                Font titleFont = new Font("TimesRoman", Font.BOLD, 25);

                //Create panel with scroll pane of test cases
                JPanel testSetPane = new JPanel();
                testSetPane.setLayout(new BoxLayout(testSetPane, BoxLayout.PAGE_AXIS));
                //Title
                JLabel testCaseLabel = new JLabel("Test cases");
                testCaseLabel.setFont(titleFont);
                testCaseLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                testSetPane.add(testCaseLabel);
                //Table of test cases
                DefaultTableModel tableModel = new CustomTableModel();
                tableModel.setColumnIdentifiers(new String[] {"Points", "Dimensions", "Clusters", "Runs", "", ""});
                for (int i = 0; i < 20; i++) {
                    tableModel.addRow(new Object[] {i, i, i, i, "+", "-"});
                }

                JTable table = new JTable(tableModel);
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
                String[] options = new String[] { "Run", "Cancel" };
                int response = JOptionPane.showOptionDialog(view, runPane, "Choose test set",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, options[0]);
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

    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        runButton.setBounds(BUTTON_HEIGHT, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        plottingButton.setBounds(view.windowWidth - view.sidePanelWidth - BUTTON_WIDTH, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
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
