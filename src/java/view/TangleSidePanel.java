package view;

import datasets.FeatureBasedDataset;
import util.ValueAdjuster;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TangleSidePanel extends SidePanel {

    private ValueAdjuster aValueAdjuster; //Slider for the "a" parameter.
    private JCheckBox showCutsCheckBox;
    private JComboBox<String> cutGeneratorDropdown;
    private JComboBox<String> costFunctionDropdown;

    public TangleSidePanel(View view) {
        super(view);

        add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel aLabel = new JLabel("a");
        aLabel.setFont(new Font("TimesRoman", Font.BOLD, 18));
        aLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(aLabel);

        aValueAdjuster = new ValueAdjuster(1, 80);
        aValueAdjuster.setMaximumSize(new Dimension(view.sidePanelWidth - view.sidePanelWidth / 2, view.windowHeight / 6));
        aValueAdjuster.setEnabled(false);
        aValueAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueChanged();
                repaint();
            }
        });
        add(aValueAdjuster);

        if (view.getDataset() instanceof FeatureBasedDataset && ((FeatureBasedDataset) view.getDataset()).dataPoints[0].length <= 2) {
            showCutsCheckBox = new JCheckBox("Show Cuts");
            showCutsCheckBox.setAlignmentX(CENTER_ALIGNMENT);
            showCutsCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    ((JPanel)view.dataVisualizer).repaint();
                }
            });
            add(showCutsCheckBox);
        }
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
    }

    protected void updateAValue(int a) {
        aValueAdjuster.setValue(a);
    }

    String removedCostFunction = null;
    protected void valueChanged() {
        if (removedCostFunction != null) {
            costFunctionDropdown.addItem(removedCostFunction);
        }
        removedCostFunction = null;
        for (int i = 0; i < cutGeneratorDropdown.getItemCount(); i++) {
            for (int j = 0; j < costFunctionDropdown.getItemCount(); j++) {
                if (cutGeneratorDropdown.getSelectedIndex() != i && cutGeneratorDropdown.getItemAt(i).equals(costFunctionDropdown.getItemAt(j))) {
                    removedCostFunction = costFunctionDropdown.getItemAt(j);
                    costFunctionDropdown.removeItemAt(j);
                    break;
                }
            }
        }
        if (aValueAdjuster.hasValue()) {
            view.controller.generateClusteringTangles(aValueAdjuster.getValue(), -1, (String) cutGeneratorDropdown.getSelectedItem(), (String) costFunctionDropdown.getSelectedItem());
        }
        repaint();
    }

    protected void update(int n) {
        aValueAdjuster.setMaximumValue(n);
        aValueAdjuster.setEnabled(view.dataVisualizer.getNumberOfPoints() > 0);
        repaint();
    }

    /*protected void setBounds() {
        super.setBounds();
        aValueAdjuster.setBounds(30, 300, 100, 130);
        if (showCutsCheckBox != null) {
            showCutsCheckBox.setBounds(30, 400, 100, 50);
        }
    }*/

    protected boolean showCuts() {
        return showCutsCheckBox == null ? false : showCutsCheckBox.isSelected();
    }
}
