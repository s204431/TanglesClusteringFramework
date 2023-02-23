package datasets;

import util.BitSet;

import java.io.File;
import java.util.*;

public class GraphDataset extends Dataset {
    private int[][][] dataPoints;
    private int[][] edges;

    //Loads a graph from graphviz format.
    public void loadGraphFromFile(String fileName) {
        try {
            List<List<int[]>> result = new ArrayList<>();
            List<int[]> edgesResult = new ArrayList<>();
            List<String> names = new ArrayList<>();
            File file = new File("datasets/" + fileName);
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
            loadGroundTruth(file, 0, dataPoints.length-1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Performs the Kernighan–Lin algorithm to generate initial cuts.
    public BitSet[] getInitialCuts() {
        int numberOfCuts = 20;
        BitSet[] cuts = new BitSet[numberOfCuts];
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
        for (int i = 0; i < numberOfCuts; i++) {
            BitSet cut = getRandomBitSet();
            cuts[i] = cut;
            int size1 = cut.count();
            int size2 = cut.size() - cut.count();
            for (int j = 0; j < 2; j++) {
                int[] dValues = new int[dataPoints.length];
                for (int k = 0; k < dValues.length; k++) {
                    dValues[k] = getDValue(cut, k, minWeight, maxWeight);
                }
                List<Integer> gv = new ArrayList<>();
                List<Integer> av = new ArrayList<>();
                List<Integer> bv = new ArrayList<>();
                boolean[] inactiveNodes = new boolean[dataPoints.length];
                for (int k = 0; k < (size1 > size2 ? size2 : size1); k++) {
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

    public double[] getCutCosts() {
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

    private BitSet getRandomBitSet() {
        BitSet result = new BitSet(dataPoints.length);
        int n0 = 0;
        int n1 = 0;
        Random random = new Random();
        for (int i = 0; i < result.size(); i++) {
            if (n0 - n1 >= result.size() - i) {
                result.add(i);
                n1++;
            }
            else if (n1 - n0 >= result.size() - i) {
                n0++;
            }
            else {
                if (random.nextBoolean()) {
                    result.add(i);
                    n1++;
                }
                else {
                    n0++;
                }
            }
        }
        return result;
    }

    private int getCost(int node1, int node2, int minWeight, int maxWeight) {
        int weight = getEdgeWeight(node1, node2);
        if (weight == Integer.MIN_VALUE) {
            return 0; //Return 0 if there is no edge between node1 and node2.
        }
        else {
            return minWeight + maxWeight - weight;
        }
    }

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

    public String asGraphviz() {
        String result = "digraph G {\n";
        for (int i = 0; i < dataPoints.length; i++) {
            for (int j = 0; j < dataPoints[i].length; j++) {
                result += " " + i + " -- " + dataPoints[i][j][0] + " [label=" + dataPoints[i][j][1] + "];\n";
            }
        }
        return result + "}";
    }

}
