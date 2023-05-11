package view;

import datasets.Dataset;
import datasets.FeatureBasedDataset;
import datasets.GraphDataset;
import util.ExtendedNumberFormatter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

public class TopPanel extends JPanel {

    //Responsible: Michael

    //This class represents a panel in the top part of a View.

    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 30;

    private View view;

    //Toolbar and all the buttons that are added to the toolbar.
    private JToolBar toolBar;
    private JButton newButton;
    private JButton exportButton;
    private JButton showAxesButton;
    private JButton showGridLinesButton;
    private JButton statisticsButton;

    private String axesOnText = "Axes ON";
    private String gridlinesOnText = "Grid lines ON";

    //Constructor receiving a View.
    protected TopPanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        toolBar = new JToolBar();
        toolBar.setLayout(null);

        addNewButton();
        addExportButton();
        addAxesButton();
        addGridLinesButton();
        addStatisticsButton();

        add(toolBar);
    }

    //Adds button to generate and load data sets to the toolbar.
    private void addNewButton() {
        final JPopupMenu newPopup = new JPopupMenu();

        //Create new dataset functionality.
        newPopup.add(new JMenuItem(new AbstractAction("Create new dataset") {
            public void actionPerformed(ActionEvent e) {
                //Formatter restricting user input to numbers.
                ExtendedNumberFormatter formatter = createNumberFormatter();
                JFormattedTextField pointsTextField = new JFormattedTextField(formatter);
                JFormattedTextField dimensionTextField = new JFormattedTextField(formatter);
                JFormattedTextField clusterTextField = new JFormattedTextField(formatter);

                //Dropdown menu for supported data types.
                JComboBox<String> comboBox = new JComboBox<>();
                for (String type : Dataset.supportedDatasetTypes) {
                    comboBox.addItem(type);
                }
                comboBox.setAlignmentX(LEFT_ALIGNMENT);

                //Labels describing which parameter is which.
                JPanel createPopupPanel = new JPanel();
                createPopupPanel.setLayout(new BoxLayout(createPopupPanel, BoxLayout.PAGE_AXIS));
                createPopupPanel.add(new JLabel("Number of points"));
                createPopupPanel.add(pointsTextField);
                createPopupPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                JLabel dimensionLabel = new JLabel("Number of dimensions");
                createPopupPanel.add(dimensionLabel);
                createPopupPanel.add(dimensionTextField);
                createPopupPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                createPopupPanel.add(new JLabel("Number of clusters"));
                createPopupPanel.add(clusterTextField);
                createPopupPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                createPopupPanel.add(comboBox);

                comboBox.addActionListener(e1 -> {
                    dimensionLabel.setEnabled(!comboBox.getSelectedItem().equals(GraphDataset.name));
                    dimensionTextField.setEnabled(!comboBox.getSelectedItem().equals(GraphDataset.name));
                });


                int createResult = JOptionPane.showConfirmDialog(view, createPopupPanel,
                        "Choose parameters", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                //Create new dataset if user pressed OK in OptionPane.
                if (createResult == JOptionPane.OK_OPTION) {
                    int points = Integer.parseInt(getTextFieldValue(pointsTextField, 1000));
                    int dimensions = Integer.parseInt(getTextFieldValue(dimensionTextField, 2));
                    int clusters = Integer.parseInt(getTextFieldValue(clusterTextField, 4));
                    String datasetType = (String) comboBox.getSelectedItem();
                    view.createDataset(datasetType, points, dimensions, clusters);
                }
            }
        }));

        //Load dataset functionality.
        newPopup.add(new JMenuItem(new AbstractAction("Load new dataset") {
            public void actionPerformed(ActionEvent e) {
                //File chooser for .csv and .dot files.
                final File[] file = new File[1];
                JPanel savePopupPanel = new JPanel();
                savePopupPanel.setLayout(new BoxLayout(savePopupPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("No file selected");
                JButton fileButton = new JButton("Select File");
                fileButton.addActionListener((l) -> {
                    final JFileChooser fc = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(".csv, .dot", "csv", "dot");
                    fc.setFileFilter(filter);
                    int returnVal = fc.showDialog(view, "Choose");
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file[0] = fc.getSelectedFile();
                        label.setText(file[0].getName());
                    }
                });

                //Panel that holds file chooser.
                savePopupPanel.add(label);
                savePopupPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                savePopupPanel.add(fileButton);

                //Checkbox for normalizing data set.
                JCheckBox normalizeCheckBox = new JCheckBox("Normalize data set");
                normalizeCheckBox.setAlignmentX(RIGHT_ALIGNMENT);
                JPanel checkBoxPanel = new JPanel();
                checkBoxPanel.add(normalizeCheckBox);

                //Dropdown menu for supported data types.
                JComboBox<String> comboBox = new JComboBox<>();
                for (String type : Dataset.supportedDatasetTypes) {
                    comboBox.addItem(type);
                }
                comboBox.addActionListener(e1 -> {
                    normalizeCheckBox.setEnabled(comboBox.getSelectedItem().equals(FeatureBasedDataset.name));
                });

                //Panel that is shown to the user.
                JPanel loadPopupPanel = new JPanel();
                loadPopupPanel.setLayout(new BoxLayout(loadPopupPanel, BoxLayout.PAGE_AXIS));
                loadPopupPanel.add(savePopupPanel);
                loadPopupPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                loadPopupPanel.add(comboBox);
                loadPopupPanel.add(checkBoxPanel);

                int loadResult = JOptionPane.showConfirmDialog(view, loadPopupPanel,
                        "Load dataset", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                Object selectedFile = file[0];
                String datasetType = (String) comboBox.getSelectedItem();

                //Load the chosen file if the user presses OK.
                if (loadResult == JOptionPane.OK_OPTION && file[0] != null) {
                    String fileName = file[0].getAbsolutePath();
                    if (datasetType.equals(GraphDataset.name)) {
                        try {
                            view.loadDatasetFromFile(datasetType, fileName, 0, 0, 0, 0, false);
                        }
                        catch (RuntimeException e1) {
                            JOptionPane.showMessageDialog(view, "Failed to load data set");
                        }
                        return;
                    }

                    //Formatter restricting user input to numbers.
                    ExtendedNumberFormatter formatter = createNumberFormatter();
                    JFormattedTextField startRowTextField = new JFormattedTextField(formatter);
                    JFormattedTextField endRowTextField = new JFormattedTextField(formatter);
                    JFormattedTextField startColTextField = new JFormattedTextField(formatter);
                    JFormattedTextField endColTextField = new JFormattedTextField(formatter);

                    //Horizontal boxes to contain labels and text fields for loading parameters.
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

                    // Panel that is shown to the user.
                    JPanel parameterPopupPanel = new JPanel();
                    parameterPopupPanel.setLayout(new BoxLayout(parameterPopupPanel, BoxLayout.PAGE_AXIS));
                    parameterPopupPanel.add(box1);
                    parameterPopupPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    parameterPopupPanel.add(box2);

                    int parameterResult = JOptionPane.showConfirmDialog(view, parameterPopupPanel,
                            ("Choose loading parameters for " + selectedFile), JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);

                    // Load data set with chosen parameters if the user presses OK.
                    if (parameterResult == JOptionPane.OK_OPTION) {
                        int startRow = Integer.parseInt(getTextFieldValue(startRowTextField, 0));
                        int endRow = Integer.parseInt(getTextFieldValue(endRowTextField, -1));
                        int startCol = Integer.parseInt(getTextFieldValue(startColTextField, 0));
                        int endCol = Integer.parseInt(getTextFieldValue(endColTextField ,-1));
                        boolean normalize = normalizeCheckBox.isSelected();
                        try {
                            view.loadDatasetFromFile(datasetType, fileName, startRow, endRow, startCol, endCol, normalize);
                        }
                        catch (RuntimeException e1) {
                            JOptionPane.showMessageDialog(view, "Failed to load data set");
                        }
                    }
                }
            }
        }));

        //Create load new image functionality.
        newPopup.add(new JMenuItem(new AbstractAction("Load new image") {
            public void actionPerformed(ActionEvent e) {
                //File chooser for .png and .jpg files.
                final File[] file = new File[1];
                JPanel loadPopupPanel = new JPanel();
                loadPopupPanel.setLayout(new BoxLayout(loadPopupPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("No file selected");
                JButton fileButton = new JButton("Select File");
                fileButton.addActionListener((l) -> {
                    final JFileChooser fc = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(".png, .jpg", "png", "jpg");
                    fc.setFileFilter(filter);
                    int returnVal = fc.showDialog(view, "Choose");
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file[0] = fc.getSelectedFile();
                        label.setText(file[0].getName());
                    }
                });

                //Panel that holds file chooser.
                loadPopupPanel.add(label);
                loadPopupPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                loadPopupPanel.add(fileButton);

                int loadResult = JOptionPane.showConfirmDialog(view, loadPopupPanel,
                        "Load image", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                //Load the chosen image file if the user presses OK.
                if (loadResult == JOptionPane.OK_OPTION && file[0] != null) {
                    String fileName = file[0].getAbsolutePath();
                    view.loadImageFromFile(fileName);
                }
            }
        }));

        //Create button on toolbar.
        newButton = new JButton("New");
        newButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                newPopup.show(e.getComponent(), 0, newButton.getY() + BUTTON_HEIGHT);
            }
        });
        toolBar.add(newButton);
    }

    //Adds button for exporting to the toolbar.
    private void addExportButton() {
        final JPopupMenu exportPopup = new JPopupMenu();
        exportPopup.add(new JMenuItem(new AbstractAction("Save dataset to file") {
            public void actionPerformed(ActionEvent e) {
                //File chooser for .dot and .csv files.
                final File[] file = new File[1];
                JPanel savePopupPanel = new JPanel();
                savePopupPanel.setLayout(new BoxLayout(savePopupPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("No file selected");
                JButton button = new JButton("Select File");
                button.addActionListener((l) -> {
                    final JFileChooser fc = new JFileChooser();
                    String extension = view.getDataset() instanceof GraphDataset ? "dot" : "csv";
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

                //Panel that holds file chooser.
                savePopupPanel.add(label);
                savePopupPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                savePopupPanel.add(button);

                int saveResult = JOptionPane.showConfirmDialog(view, savePopupPanel,
                        "Save dataset to file", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                //Save data set to the destination path if the user presses OK.
                if (saveResult == JOptionPane.OK_OPTION && file[0] != null) {
                    view.getDataset().saveToFile(file[0]);
                }
            }
        }));
        exportPopup.add(new JMenuItem(new AbstractAction("As PNG") {
            public void actionPerformed(ActionEvent e) {
                //File chooser for .png files.
                final File[] file = new File[1];
                JPanel savePopupPanel = new JPanel();
                savePopupPanel.setLayout(new BoxLayout(savePopupPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("No file selected");
                JButton button = new JButton("Select File");
                button.addActionListener((l) -> {
                    final JFileChooser fc = new JFileChooser();
                    String extension = "png";
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

                //Panel that holds file chooser.
                savePopupPanel.add(label);
                savePopupPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                savePopupPanel.add(button);

                int saveResult = JOptionPane.showConfirmDialog(view, savePopupPanel,
                        "Export as PNG", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                //Save png of dataVisualizer to the destination path if the user presses OK.
                if (saveResult == JOptionPane.OK_OPTION && file[0] != null) {
                    try {
                        if (view.dataVisualizer instanceof PlottingView) {
                            ((PlottingView)view.dataVisualizer).coordinates.setVisible(false);
                        }
                        JPanel panel = ((JPanel)view.dataVisualizer);
                        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = image.createGraphics();
                        panel.paint(g);
                        g.dispose();
                        ImageIO.write(image, "png", file[0]);
                        if (view.dataVisualizer instanceof PlottingView) {
                            ((PlottingView)view.dataVisualizer).coordinates.setVisible(true);
                        }
                    } catch (IOException e1) {}
                }
            }
        }));


        exportButton = new JButton("Export");
        exportButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                exportPopup.show(e.getComponent(), 0, exportButton.getY() + BUTTON_HEIGHT);
            }
        });
        toolBar.add(exportButton);
    }

    //Adds button to turn axes on and off to the toolbar.
    private void addAxesButton() {
        showAxesButton = new JButton(axesOnText);
        showAxesButton.setBackground(new JButton().getBackground());
        showAxesButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (showAxesButton.getText().equals(axesOnText)) {
                    showAxesButton.setText(axesOnText.substring(0, axesOnText.length() - 2) + "OFF");
                    showAxesButton.setBackground(Color.GRAY);
                } else {
                    showAxesButton.setText(axesOnText);
                    showAxesButton.setBackground(new JButton().getBackground());
                }
                view.switchShowingOfAxes();
            }
        });
        toolBar.add(showAxesButton);
    }

    //Adds button to turn grid lines on and off to the toolbar.
    private void addGridLinesButton() {
        showGridLinesButton = new JButton(gridlinesOnText);
        showGridLinesButton.setBackground(new JButton().getBackground());
        showGridLinesButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (showGridLinesButton.getText().equals(gridlinesOnText)) {
                    showGridLinesButton.setText(gridlinesOnText.substring(0, gridlinesOnText.length() - 2) + "OFF");
                    showGridLinesButton.setBackground(Color.GRAY);
                } else {
                    showGridLinesButton.setText(gridlinesOnText);
                    showGridLinesButton.setBackground(new JButton().getBackground());
                }
                view.switchShowingOfGridlines();
            }
        });
        toolBar.add(showGridLinesButton);
    }

    //Adds button that switches to statistics view to the toolbar.
    private void addStatisticsButton() {
        statisticsButton = new JButton("Statistics");
        statisticsButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                view.switchToStatistics();
            }
        });
        toolBar.add(statisticsButton);
    }

    //Returns the truth value stating if the axes button is on or off.
    protected boolean showAxesButtonIsOn() {
        return showAxesButton.getText().equals(axesOnText);
    }

    //Returns the truth value stating if the grid lines button is on or off.
    protected boolean showGridlinesButtonIsOn() {
        return showGridLinesButton.getText().equals(gridlinesOnText);
    }

    //Makes axes and grid lines buttons visible.
    protected void makeOnOffButtonsVisible(boolean visible) {
        showAxesButton.setVisible(visible);
        showGridLinesButton.setVisible(visible);
    }

    //Sets bounds of the visible components in the top panel.
    protected void setBounds() {
        setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        toolBar.setBounds(0, 0, view.windowWidth, view.topPanelHeight);
        newButton.setBounds(BUTTON_HEIGHT, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        exportButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);

        showAxesButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH * 3, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        showGridLinesButton.setBounds(BUTTON_HEIGHT + BUTTON_WIDTH * 4, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);

        statisticsButton.setBounds(view.windowWidth - view.sidePanelWidth - BUTTON_WIDTH, view.topPanelHeight / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    //Returns the value of a text field. If the text field is null it returns the default value.
    private String getTextFieldValue(JFormattedTextField textfield, int defaultValue) {
        if (textfield.getValue() == null) {
            return ""+defaultValue;
        }
        return textfield.getValue().toString();
    }

    //Custom ExtendedNumberFormatter that only allows number between 1 and max integer value as input.
    private ExtendedNumberFormatter createNumberFormatter() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        ExtendedNumberFormatter formatter = new ExtendedNumberFormatter(format);
        formatter.setMinimum(1);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        return formatter;
    }

}
