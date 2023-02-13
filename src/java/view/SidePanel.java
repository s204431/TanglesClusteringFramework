package view;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SidePanel extends JPanel {
    private View view;

    private JSlider aSlider;
    private JTextField aValue;

    protected int numberOfDataPoints;

    private boolean valueEntered = false;

    public SidePanel(View view) {
        this.view = view;

        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));
        setLayout(null);

        aSlider = new JSlider(JSlider.HORIZONTAL, 0, 80, 50);
        aSlider.setBounds(50, 200, 100, 50);
        aSlider.setMinorTickSpacing(100);
        aSlider.setMajorTickSpacing(100);
        aSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int)(aSlider.getValue() * numberOfDataPoints / 100.0);
                if (!valueEntered) {
                    aValue.setText(numberOfDataPoints == 0 ? "No data points" : "" + value);
                }
                view.changeAValue(value);
                repaint();
            }
        });
        add(aSlider);

        aValue = new JTextField("No data points");
        aValue.setBounds(50, 250, 100, 30);
        aValue.setHorizontalAlignment(JTextField.CENTER);
        add(aValue);

        aValue.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        int a = Integer.parseInt(aValue.getText());
                        valueEntered = true;
                        aSlider.setValue((int)(a * 100.0 / numberOfDataPoints));
                        aValue.setFocusable(false);
                        aValue.setFocusable(true);
                    } catch (Exception exception) { }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("TimesRoman", Font.BOLD, 18));
        g2d.drawString("NMI score:", 50, 30);
        g2d.drawString(""+((int)(view.getNMIScore()*100000)/100000.0), 50, 50);
        g2d.drawString("Clustering time:", 50, 80);
        g2d.drawString(view.getClusteringTime() + " ms", 50, 100);
        g2d.drawString("a", 50, 200);
    }

    protected void update() {
        repaint();
    }

    protected void update(int n) {
        this.numberOfDataPoints = n;
        update();
    }

    protected void updateAValue(int a) {
        if (valueEntered) {
            valueEntered = false;
        }
        else {
            aSlider.setValue((int)(a * 100.0 / numberOfDataPoints));
            aValue.setText(""+a);
        }
    }

}
