package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PlottingView extends JPanel {
    JFrame frame;

    public int windowWidth, windowHeight;

    private int xOrig = (int)(windowWidth * 0.3);
    private int yOrig = (int)(windowHeight * 0.6);
    private int factor = 3; // Used to expand x- or y-axis to fit largest datapoint

    //Often used strokes
    private final BasicStroke stroke1 = new BasicStroke(1);
    private final BasicStroke stroke2 = new BasicStroke(2);
    private final BasicStroke stroke3 = new BasicStroke(3);

    public PlottingView() {
        //Initialize
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(screenSize.width, screenSize.height));
        setBounds(0, 0, getPreferredSize().width, getPreferredSize().height);

        windowWidth = screenSize.width - screenSize.width / 4;
        windowHeight = screenSize.height - screenSize.height / 4;

        //Create frame
        frame = new JFrame("PlottingView");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(windowWidth, windowHeight));
        frame.setLayout(null);

        frame.add(this);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                windowWidth = frame.getWidth();
                windowHeight = frame.getHeight();
                repaint();
            }
        });
    }

    //Draws everything on screen.
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        xOrig = (int)(windowWidth * 0.3);
        yOrig = (int)(windowHeight * 0.6);

        //Draw axes
        g2d.setStroke(stroke3);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, yOrig, windowWidth, yOrig);
        g2d.drawLine(xOrig, 0, xOrig, windowHeight);

        //Draw lines and numbers on axes
        int windowMax = Math.max(windowHeight, windowWidth);
        int lineSize1 = windowMax/150;
        int lineSize2 = windowMax/75;
        g2d.setStroke(stroke1);
        int fontSize = windowMax / 70;
        g.setFont(new Font("TimesRoman", Font.BOLD, Math.max(fontSize, 12)));
        int lineSize = lineSize1;
        int lineGap = windowMax/30;
        int lines = windowMax/lineGap;
        boolean drawNumber = false;
        for (int i = 1; i < lines; i++) {
            if (i%5 == 0) {
                g2d.setStroke(stroke2);
                lineSize = lineSize2;
                drawNumber = true;
            } else {
                g2d.setStroke(stroke1);
                lineSize = lineSize1;
                drawNumber = false;
            }
            int posX = xOrig + lineGap * i;
            int posY = yOrig + lineGap * i;
            int negX = xOrig - lineGap * i;
            int negY = yOrig - lineGap * i;
            g2d.drawLine(posX, yOrig - lineSize, posX, yOrig + lineSize); //Positive direction on x-axis
            g2d.drawLine(xOrig - lineSize, posY, xOrig + lineSize, posY); //Positive direction on y-axis
            g2d.drawLine(negX, yOrig - lineSize, negX, yOrig + lineSize); //Negative direction on x-axis
            g2d.drawLine(xOrig - lineSize, negY, xOrig + lineSize, negY); //Negative direction on y-axis

            if (drawNumber) {
                String posText = "" + i * factor;
                String negText = "-" + i * factor;
                int fontHeight = g2d.getFontMetrics().getHeight();
                int fontWidth = g2d.getFontMetrics().stringWidth(posText);
                g2d.drawString(posText, posX - fontWidth / 2, yOrig + lineSize + fontHeight); //Positive direction on x-axis
                g2d.drawString(posText, xOrig - lineSize - fontWidth * 3 / 2, negY + fontHeight / 4); //Positive direction on y-axis

                fontWidth = g2d.getFontMetrics().stringWidth(negText);
                g2d.drawString(negText, negX - fontWidth / 2, yOrig + lineSize + fontHeight); //Negative direction on x-axis
                g2d.drawString(negText, xOrig - lineSize - fontWidth * 3 / 2, posY + fontHeight / 4); //Negative direction on y-axis
            }
        }
    }
}

