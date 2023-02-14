package util;

import view.View;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ValueAdjuster extends JComponent {
    //This class represents a JSlider with a corresponding JTextField that follow each other.
    private JSlider slider;
    private JTextField textField;
    private boolean valueEntered = false;
    private int maximumValue;
    private Runnable onChangeRunnable;

    public ValueAdjuster() {
        this(1, 80);
    }

    public ValueAdjuster(int minSliderValue, int maxSliderValue) {
        slider = new JSlider(JSlider.HORIZONTAL, minSliderValue, maxSliderValue, (maxSliderValue-minSliderValue)/2);
        slider.setMinorTickSpacing(100);
        slider.setMajorTickSpacing(100);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int)(slider.getValue() * maximumValue / 100.0);
                if (!valueEntered) {
                    textField.setText(maximumValue == 0 ? "-" : "" + value);
                    if (onChangeRunnable != null) {
                        onChangeRunnable.run();
                    }
                }
                valueEntered = false;
                repaint();
            }
        });
        add(slider);

        textField = new JTextField("-");
        textField.setHorizontalAlignment(JTextField.CENTER);
        add(textField);

        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        int value = Integer.parseInt(textField.getText());
                        valueEntered = true;
                        slider.setValue((int)(value * 100.0 / maximumValue));
                        if (onChangeRunnable != null) {
                            onChangeRunnable.run();
                        }
                        textField.setFocusable(false);
                        textField.setFocusable(true);
                        repaint();
                    } catch (Exception exception) { }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        setFocusable(maximumValue > 0);
    }

    public void setValue(int value) {
        slider.setValue((int)(value * 100.0 / maximumValue));
        textField.setText(""+value);
        valueEntered = true;
    }

    public int getValue() {
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    public void setMaximumValue(int maxValue) {
        maximumValue = maxValue;
        setFocusable(maximumValue > 0);
        repaint();
    }

    public void performOnChange(Runnable runnable) {
        this.onChangeRunnable = runnable;
    }

    @Override
    public void setFocusable(boolean focusable) {
        slider.setFocusable(focusable);
        slider.setEnabled(focusable);
        textField.setFocusable(focusable);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        slider.setBounds(0, 0, getWidth(), (int)((50.0/130.0)*getHeight()));
        textField.setBounds(0, (int)((50.0/130.0)*getHeight()), getWidth(), (int)((30.0/130.0)*getHeight()));
    }
}
