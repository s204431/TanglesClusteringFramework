package view;

import datasets.FeatureBasedDataset;
import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TangleSidePanel extends SidePanel {

    //Responsible: Michael

    //This class is the side panel associated with the tangle clustering algorithm.

    private ValueAdjuster aValueAdjuster; //Slider for the "a" parameter.
    private JCheckBox showHorizontalCutsCheckBox;
    private JCheckBox showVerticalCutsCheckBox;
    private JComboBox<String> cutGeneratorDropdown;
    private JComboBox<String> costFunctionDropdown;

    private List<String> removedCostFunctions = new ArrayList<>();
    private boolean valueChangedDone = true;

    //Constructor receiving view.
    public TangleSidePanel(View view) {
        super(view);

        JLabel aLabel = new JLabel("a");
        aLabel.setFont(super.font);
        aLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(aLabel);

        aValueAdjuster = new ValueAdjuster(1, 80);
        aValueAdjuster.setMaximumSize(new Dimension(view.sidePanelWidth - view.sidePanelWidth / 2, view.windowHeight / 10));
        aValueAdjuster.setEnabled(false);
        aValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                repaint();
            }
        });
        add(aValueAdjuster);

        //Add checkboxes for visualizing the generated cuts if data set is feature based and has two dimensions.
        if (view.getDataset() instanceof FeatureBasedDataset && ((FeatureBasedDataset) view.getDataset()).dataPoints[0].length <= 2) {
            showHorizontalCutsCheckBox = new JCheckBox("Show Horizontal Cuts");
            showHorizontalCutsCheckBox.setAlignmentX(CENTER_ALIGNMENT);
            showHorizontalCutsCheckBox.addChangeListener(e -> ((JPanel)view.dataVisualizer).repaint());
            showVerticalCutsCheckBox = new JCheckBox("Show Vertical Cuts");
            showVerticalCutsCheckBox.setAlignmentX(CENTER_ALIGNMENT);
            showVerticalCutsCheckBox.addChangeListener(e -> ((JPanel)view.dataVisualizer).repaint());
            add(showHorizontalCutsCheckBox);
            add(showVerticalCutsCheckBox);
        }

        //Add dropdown menus for generation algorithm and cost function.
        cutGeneratorDropdown = new JComboBox<>(view.getDataset().getInitialCutGenerators());
        cutGeneratorDropdown.addActionListener(e -> valueChanged());
        costFunctionDropdown = new JComboBox<>(view.getDataset().getCostFunctions());
        costFunctionDropdown.addActionListener(e -> valueChanged());
        cutGeneratorDropdown.setMaximumSize(new Dimension((int)(view.sidePanelWidth/1.5), 25));
        costFunctionDropdown.setMaximumSize(new Dimension((int)(view.sidePanelWidth/1.5), 25));
        add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel initialCutGeneratorText = new JLabel("Initial cut generator");
        initialCutGeneratorText.setAlignmentX(CENTER_ALIGNMENT);
        add(initialCutGeneratorText);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(cutGeneratorDropdown);
        add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel costFunctionText = new JLabel("Cost function");
        costFunctionText.setAlignmentX(CENTER_ALIGNMENT);
        add(costFunctionText);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(costFunctionDropdown);
        valueChanged();
        addClusterButton(new ValueAdjuster[] { aValueAdjuster });
    }

    //Updates values and generates cluster when a value has been changed in tangle side panel.
    protected void valueChanged() {
        if (!valueChangedDone) {
            return;
        }
        valueChangedDone = false;
        for (String costFunction : removedCostFunctions) {
            costFunctionDropdown.addItem(costFunction);
        }
        removedCostFunctions = new ArrayList<>();
        for (int i = 0; i < cutGeneratorDropdown.getItemCount(); i++) {
            for (int j = 0; j < costFunctionDropdown.getItemCount(); j++) {
                if (cutGeneratorDropdown.getSelectedIndex() != i && cutGeneratorDropdown.getItemAt(i).equals(costFunctionDropdown.getItemAt(j))) {
                    String removedCostFunction = costFunctionDropdown.getItemAt(j);
                    removedCostFunctions.add(removedCostFunction);
                }
            }
        }
        for (String costFunction : removedCostFunctions) {
            costFunctionDropdown.removeItem(costFunction);
        }
        if (aValueAdjuster.hasValue()) {
            view.controller.generateClusteringTangles(aValueAdjuster.getValue(), -1, (String) cutGeneratorDropdown.getSelectedItem(), (String) costFunctionDropdown.getSelectedItem());
        }
        repaint();
        valueChangedDone = true;
    }

    //Updates aValueAdjuster based on n and determines if the user should be able to interact with it.
    protected void update(int n) {
        aValueAdjuster.setMaximumValue(n);
        aValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        repaint();
    }

    //Returns true if the horizontal cuts should be shown.
    protected boolean showHorizontalCuts() {
        return showHorizontalCutsCheckBox == null ? false : showHorizontalCutsCheckBox.isSelected();
    }

    //Returns true if the vertical cuts should be shown.
    protected boolean showVerticalCuts() {
        return showVerticalCutsCheckBox == null ? false : showVerticalCutsCheckBox.isSelected();
    }
}
