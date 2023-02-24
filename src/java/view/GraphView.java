package view;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.Node;
import guru.nidi.graphviz.parse.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.node;

public class GraphView extends JPanel {
    private View view;

    private Image image;

    private MutableGraph graph;

    private java.awt.Color[] colors = { java.awt.Color.RED, java.awt.Color.BLUE, java.awt.Color.YELLOW, java.awt.Color.GREEN, java.awt.Color.ORANGE, java.awt.Color.PINK, java.awt.Color.GRAY };;

    public GraphView(View view) {
        this.view = view;
        setPreferredSize(new Dimension(view.getWindowWidth(), view.getWindowHeight()));

        String exampleDot =
                "graph {\n" +
                "    white -- cyan -- blue\n" +
                "    white -- yellow -- green\n" +
                "    white -- pink -- red\n" +
                "\n" +
                "    cyan -- green -- black\n" +
                "    yellow -- red -- black\n" +
                "    pink -- blue -- black\n" +
                "}";
        loadGraphFromDotString(exampleDot);
        drawClusters(
                new int[] { 0,0,0,0,1,1,1,1 },
                new double[][] {
                        {0.9, 0.1},
                        {0.9, 0.1},
                        {0.9, 0.1},
                        {0.9, 0.1},
                        {0.9, 0.1},
                        {0.1, 0.9},
                        {0.9, 0.1},
                        {0.1, 0.9} });
        image = convertGraphToImage();


        //Test frame
        JFrame frame = new JFrame();
        frame.setTitle("GraphView test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    //Draws graphView on screen
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillRect(0, 0, 100, 100);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    protected Image convertGraphToImage() {
        BufferedImage image = Graphviz.fromGraph(graph).render(Format.PNG).toImage();
        if (image.getWidth() > image.getHeight()) {
            return Graphviz.fromGraph(graph).width(view.windowWidth - view.sidePanelWidth).render(Format.PNG).toImage();
        } else {
            return Graphviz.fromGraph(graph).height(view.windowHeight - view.topPanelHeight).render(Format.PNG).toImage();
        }
    }

    protected void loadGraphFromDotString(String dot) {
        try {
            InputStream stream = new ByteArrayInputStream(dot.getBytes(StandardCharsets.UTF_8));
            graph = new Parser().read(stream);
            graph.graphAttrs()
                    .add(Color.rgb(this.getBackground().getRGB()).background())
                    .nodeAttrs().add(Color.WHITE.fill())
                    .nodes().forEach(node ->
                            node.add(
                                    Color.BLACK,
                                    Style.FILLED,
                                    Style.lineWidth(1)
                            ));
        } catch (Exception e) {}
    }

    protected void drawClusters(int[] hardClustering) {
        final int[] i = {0};
        graph.nodes().forEach(node ->
                        node.add(
                                Color.rgba(colors[hardClustering[i[0]++]].getRGB()).fill()
                        ));
    }

    protected void drawClusters(int[] hardClustering, double[][] softClustering) {
        final int[] i = {0};
        graph.nodes().forEach(node ->
                node.add(
                        Color.rgba(colors[hardClustering[i[0]]].getRGB() + ((int)(softClustering[i[0]][hardClustering[i[0]++]] * 200) << 24)).fill()
                ));
    }
}
