package util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

public class ValueAdjuster extends JComponent {

    //Responsible: Michael

    //This class represents a JSlider with a corresponding JTextField that follow each other.

    private final JSlider slider;
    private final JTextField textField;
    private boolean valueEntered = false;
    private int maximumValue;
    private ChangeListener changeListener;
    private boolean hasValue = false;
    private int lastValue = Integer.MIN_VALUE;

    //Creates a default ValueAdjuster.
    public ValueAdjuster() {
        this(0, 100);
    }

    //Creates ValueAdjuster from a minimum and maximum slider value (as a percentage of the maximum value).
    public ValueAdjuster(int minSliderValue, int maxSliderValue) {
        ValueAdjuster thisObject = this;

        //Create the slider.
        slider = new JSlider(JSlider.HORIZONTAL, minSliderValue, maxSliderValue, (maxSliderValue-minSliderValue)/2);
        slider.setMinorTickSpacing(100);
        slider.setMajorTickSpacing(100);
        slider.addChangeListener(new ChangeListener() {
            //Update the text field when the slider is used.
            @Override
            public void stateChanged(ChangeEvent e) {
                //new ChangeEvent(this);
                int value = (int)(slider.getValue() * maximumValue / 100.0);
                if (!valueEntered) {
                    textField.setText("" + value);
                    if (changeListener != null && value != lastValue) {
                        changeListener.stateChanged(new ChangeEvent(thisObject));
                    }
                }
                lastValue = value;
                valueEntered = false;
                repaint();
            }
        });
        add(slider);

        //Create the text field with number formatter.
        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        ExtendedNumberFormatter numberFormatter = new ExtendedNumberFormatter(format);
        numberFormatter.setMinimum(minSliderValue);
        numberFormatter.setMaximum(Integer.MAX_VALUE);
        numberFormatter.setAllowsInvalid(false);
        textField = new JFormattedTextField(numberFormatter);
        textField.setHorizontalAlignment(JTextField.CENTER);
        add(textField);

        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            //Update the value adjuster when the Enter key is pressed.
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        int value = Integer.parseInt(textField.getText());
                        valueEntered = true;
                        hasValue = true;
                        slider.setValue((int)(value * 100.0 / maximumValue));
                        slider.setFocusable(true);
                        slider.setEnabled(true);
                        if (changeListener != null) {
                            changeListener.stateChanged(new ChangeEvent(thisObject));
                        }
                        textField.setFocusable(false);
                        textField.setFocusable(true);
                        repaint();
                    } catch (Exception ignored) { }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    //Sets the value of the value adjuster.
    public void setValue(int value) {
        slider.setValue((int)(value * 100.0 / maximumValue));
        textField.setText(""+value);
        valueEntered = true;
        hasValue = true;
        slider.setFocusable(true);
        slider.setEnabled(true);
    }

    //Removes the value from the value adjuster.
    public void removeValue() {
        hasValue = false;
        slider.setFocusable(false);
        slider.setEnabled(false);
        slider.setValue((slider.getMaximum()-slider.getMinimum())/2);
        textField.setText("");
    }

    //Returns whether the value adjuster has a value.
    public boolean hasValue() {
        return hasValue;
    }

    //Updates the current value of the ValueAdjuster to the value in the text field.
    public void updateValue() {
        try {
            setValue(Integer.parseInt(textField.getText()));
            if (changeListener != null) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
            repaint();
        }
        catch (Exception e) {}
    }

    //Returns the value of the value adjuster. Returns Integer.MIN_VALUE if there is no value.
    public int getValue() {
        if (!hasValue) {
            return Integer.MIN_VALUE;
        }
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    //Sets the maximum value that the value adjuster can contain.
    public void setMaximumValue(int maxValue) {
        maximumValue = maxValue;
        repaint();
    }

    //Adds a change listener that is used when the value of the value adjuster changes.
    public void addChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    //Sets whether the value adjuster should be enabled to be used by the user.
    @Override
    public void setEnabled(boolean enabled) {
        slider.setFocusable(hasValue && enabled);
        slider.setEnabled(hasValue && enabled);
        textField.setFocusable(enabled);
        textField.setEnabled(enabled);
        textField.setText(enabled && hasValue ? "" + getValue() : "");
    }

    //Sets the bounds of the value adjuster.
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        int sliderHeight = getHeight() * 40 / 130;
        slider.setBounds(0, 0, getWidth(), sliderHeight);
        textField.setBounds(0, sliderHeight, getWidth(), getWidth() * 30 / 130);
    }
}
