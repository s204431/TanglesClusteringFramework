package datasets;

import model.Model;
import util.BitSet;
import util.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphDataset extends Dataset {

    //Responsible: Jens

    //This class represents a graph dataset.

    public static final String name = "Graph";
    private int[][][] dataPoints;
    private int[][] edges;
    private int a;

    public static final String initialCutsKernighanLin = "Kernighan-Lin";
    public static final String costFunctionKernighanLin = "Kernighan-Lin";

    //Empty constructor.
    public GraphDataset() {

    }

    //Constructor taking an integer 3D array where first dimension is a node, second dimension is an edge for the node and third dimension is the end node of the edge and weight of the edge.
    public GraphDataset(int[][][] dataPoints) {
        this.dataPoints = dataPoints;
        List<int[]> edgesList = new ArrayList<>();
        for (int i = 0; i < dataPoints.length; i++) {
            for (int j = 0; j < dataPoints[i].length; j++) {
                edgesList.add(new int[] {i, dataPoints[i][j][0], dataPoints[i][j][1]});
            }
        }
        edges = new int[edgesList.size()][];
        for (int i = 0; i < edgesList.size(); i++) {
            edges[i] = edgesList.get(i);
        }
    }

    //Constructor taking both the graph and a ground truth.
    public GraphDataset(Tuple<int[][][], int[]> dataPointsWithGroundTruth) {
        this(dataPointsWithGroundTruth.x);
        groundTruth = dataPointsWithGroundTruth.y;
    }

    //Sets the value of a (agreement parameter).
    public void setA(int a) {
        this.a = a;
    }

    //Loads a graph from graphviz format.
    public void loadGraphFromFile(String fileName) {
        try {
            List<List<int[]>> result = new ArrayList<>();
            List<int[]> edgesResult = new ArrayList<>();
            List<String> names = new ArrayList<>();
            File file = new File(fileName);
            Scanner fileScanner = new Scanner(file);
            fileScanner.nextLine(); //Skip first line.
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.stripLeading().stripTrailing().equals("}")) {
                    break;
                }
                line = line.replace(" ", "")
                        .replace("--", " ")
                        .replace("[label=", " ")
                        .replace("];", "");
                Scanner lineScanner = new Scanner(line);
                String name1 = lineScanner.next();
                String name2 = lineScanner.next();
                int index1 = names.indexOf(name1);
                int index2 = names.indexOf(name2);
                int weight = 1;
                if (lineScanner.hasNextInt()) {
                    weight = lineScanner.nextInt();
                }
                if (index1 < 0) {
                    names.add(name1);
                    result.add(new ArrayList<>());
                    index1 = result.size()-1;
                }
                if (index2 < 0) {
                    names.add(name2);
                    result.add(new ArrayList<>());
                    index2 = result.size()-1;
                }
                result.get(index1).add(new int[] {index2, weight});
                edgesResult.add(new int[] {index1, index2, weight});
            }
            dataPoints = new int[result.size()][][];
            for (int i = 0; i < result.size(); i++) {
                dataPoints[i] = new int[result.get(i).size()][2];
                for (int j = 0; j < result.get(i).size(); j++) {
                    dataPoints[i][j] = result.get(i).get(j);
                }
            }
            edges = new int[edgesResult.size()][];
            for (int i = 0; i < edgesResult.size(); i++) {
                edges[i] = edgesResult.get(i);
            }
            fileScanner.close();
            loadGroundTruth(file, 0, dataPoints.length-1);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    //Performs the Kernighan–Lin algorithm to generate initial cuts.
    @Override
    public BitSet[] getInitialCuts(String initialCutGenerator) {
        BitSet[] cuts = getRandomCuts();
        int maxWeight = Integer.MIN_VALUE;
        int minWeight = Integer.MAX_VALUE;
        for (int i = 0; i < edges.length; i++) {
            if (edges[i][2] < minWeight) {
                minWeight = edges[i][2];
            }
            if (edges[i][2] > maxWeight) {
                maxWeight = edges[i][2];
            }
        }
        for (int i = 0; i < cuts.length; i++) {
            BitSet cut = cuts[i];
            int size1 = cut.count();
            int size2 = cut.size() - cut.count();
            for (int j = 0; j < 5; j++) {
                int[] dValues = new int[dataPoints.length];
                for (int k = 0; k < dValues.length; k++) {
                    dValues[k] = getDValue(cut, k, minWeight, maxWeight);
                }
                List<Integer> gv = new ArrayList<>();
                List<Integer> av = new ArrayList<>();
                List<Integer> bv = new ArrayList<>();
                boolean[] inactiveNodes = new boolean[dataPoints.length];
                for (int k = 0; k < Math.min(size1, size2); k++) {
                    int g = Integer.MIN_VALUE;
                    int bestA = -1;
                    int bestB = -1;
                    for (int a = 0; a < dataPoints.length; a++) {
                        for (int b = 0; b < dataPoints.length; b++) {
                            if (!inactiveNodes[a] && !inactiveNodes[b] && cut.get(a) != cut.get(b)) {
                                int newG = dValues[a] + dValues[b] - 2*getCost(a, b, minWeight, maxWeight);
                                if (newG > g) {
                                    g = newG;
                                    bestA = a;
                                    bestB = b;
                                }
                            }
                        }
                    }
                    gv.add(g);
                    av.add(bestA);
                    bv.add(bestB);
                    inactiveNodes[bestA] = true;
                    inactiveNodes[bestB] = true;
                    for (int l = 0; l < dataPoints.length; l++) {
                        if (inactiveNodes[l]) {
                            continue;
                        }
                        int costA = getCost(l, bestA, minWeight, maxWeight);
                        dValues[l] += (cut.get(l) == cut.get(bestA) ? 1 : -1)*costA;
                        int costB = getCost(l, bestB, minWeight, maxWeight);
                        dValues[l] += (cut.get(l) == cut.get(bestB) ? 1 : -1)*costB;
                    }
                }
                int sum = 0;
                int bestSum = 0;
                int bestIndex = -1;
                for (int k = 0; k < gv.size(); k++) {
                    sum += gv.get(k);
                    if (sum > bestSum) {
                        bestSum = sum;
                        bestIndex = k;
                    }
                }
                if (bestSum > 0) {
                    for (int k = 0; k <= bestIndex; k++) {
                        cut.flip(av.get(k));
                        cut.flip(bv.get(k));
                    }
                }
            }
        }
        initialCuts = cuts;
        return cuts;
    }

    //Returns the names of the supported initial cut generators.
    @Override
    public String[] getInitialCutGenerators() {
        return new String[] {initialCutsKernighanLin};
    }

    //Returns the names of the supported cost functions.
    @Override
    public String[] getCostFunctions() {
        return new String[] {costFunctionKernighanLin};
    }

    //Calculates cut costs using the Kernighan–Lin cost function.
    @Override
    public double[] getCutCosts(String costFunctionName) {
        int maxWeight = Integer.MIN_VALUE;
        int minWeight = Integer.MAX_VALUE;
        for (int i = 0; i < edges.length; i++) {
            if (edges[i][2] < minWeight) {
                minWeight = edges[i][2];
            }
            if (edges[i][2] > maxWeight) {
                maxWeight = edges[i][2];
            }
        }
        double[] costs = new double[initialCuts.length];
        for (int i = 0; i < initialCuts.length; i++) {
            for (int j = 0; j < dataPoints.length; j++) {
                costs[i] += getDValue(initialCuts[i], j, minWeight, maxWeight);
            }
        }
        return costs;
    }

    //Generates random cuts of different sizes.
    private BitSet[] getRandomCuts() {
        int iterations = 5;
        Random r = new Random();
        boolean chooseMiddle = false;
        int nCuts = dataPoints.length/a-1;
        if (nCuts <= 0) {
            nCuts = 1;
            chooseMiddle = true;
        }
        BitSet[] result = new BitSet[nCuts*iterations];
        int index = 0;
        for (int k = 0; k < iterations; k++) {
            for (int i = chooseMiddle ? dataPoints.length/2 : a; i <= (chooseMiddle ? dataPoints.length/2 : dataPoints.length-a); i += a) {
                List<Integer> indices = new ArrayList<>();
                for (int j = 0; j < dataPoints.length; j++) {
                    indices.add(j);
                }
                List<Integer> chosenIndices = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    int chosen = r.nextInt(indices.size());
                    chosenIndices.add(indices.get(chosen));
                    indices.remove(chosen);
                }
                result[index] = new BitSet(dataPoints.length);
                for (int j : chosenIndices) {
                    result[index].add(j);
                }
                index++;
            }
        }
        return result;
    }

    //Calculates the cost of an edge (0 if there is no edge).
    private int getCost(int node1, int node2, int minWeight, int maxWeight) {
        int weight = getEdgeWeight(node1, node2);
        if (weight == Integer.MIN_VALUE) {
            return 0; //Return 0 if there is no edge between node1 and node2.
        }
        else {
            return minWeight + maxWeight - weight;
        }
    }

    //Calculates the D value for the Kernighan–Lin algorithm.
    private int getDValue(BitSet cut, int node, int minWeight, int maxWeight) {
        int internalCost = 0;
        int externalCost = 0;
        for (int i = 0; i < cut.size(); i++) {
            if (cut.get(node) == cut.get(i)) {
                internalCost += getCost(node, i, minWeight, maxWeight);
            }
            else {
                externalCost += getCost(node, i, minWeight, maxWeight);
            }
        }
        return externalCost - internalCost;
    }

    //Returns the weight of the edge between two nodes (returns Integer.MIN_VALUE if there is no edge between the nodes).
    private int getEdgeWeight(int node1, int node2) {
        for (int i = 0; i < dataPoints[node1].length; i++) {
            if (dataPoints[node1][i][0] == node2) {
                return dataPoints[node1][i][1];
            }
        }
        for (int i = 0; i < dataPoints[node2].length; i++) {
            if (dataPoints[node2][i][0] == node1) {
                return dataPoints[node2][i][1];
            }
        }
        return Integer.MIN_VALUE;
    }

    //Returns the name of the dataset type.
    @Override
    public String getName() {return name;}

    //Returns the algorithms that we support for the type of dataset.
    @Override
    public String[] getSupportedAlgorithms() {
        return new String[] {Model.tangleName};
    }

    //Returns the ground truth (returns null if there is no ground truth).
    @Override
    public int[] getGroundTruth() {return groundTruth;}

    //Saves the dataset to a file as dot format.
    @Override
    public void saveToFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(asDot());
            writer.close();
            saveGroundTruth(file);
        } catch (IOException e) {}
    }

    //Converts this graph dataset to dot format.
    public String asDot() {
        String result = "graph G {\n";
        for (int i = 0; i < edges.length; i++) {
            result += " " + edges[i][0] + " -- " + edges[i][1] + " [label=" + edges[i][2] + "];\n";
        }
        return result + "}";
    }

}
