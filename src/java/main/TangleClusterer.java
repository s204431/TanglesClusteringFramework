package main;

import java.util.Date;

import main.TangleSearchTree.Node;

public class TangleClusterer {

    public static void generateClusters(Dataset dataset, int a, int psi) {
        long time1 = new Date().getTime();
        BitSet[] initialCuts = dataset.getInitialCuts();
        long time2 = new Date().getTime();
        System.out.println("Initial cuts time: " + (time2-time1) + " ms");
        int[] costs = dataset.getCutCosts2();
        for (int cost : costs) {
            System.out.print(cost + " ");
        }
        long time3 = new Date().getTime();
        System.out.println();
        System.out.println("Cost function time: " + (time3-time2) + " ms");
        TangleSearchTree tree = generateTangleSearchTree(initialCuts, costs, a, psi);
        long time4 = new Date().getTime();
        System.out.println("Tree generation time: " + (time4-time3) + " ms");
        System.out.println("Nodes at lowest depth: " + tree.lowestDepthNodes.size());
        System.out.println("Depth of tree: " + tree.getDepth(tree.lowestDepthNodes.get(0)));
        System.out.println("Total nodes in tree: " + tree.n);
        tree.condenseTree(2);
        long time5 = new Date().getTime();
        System.out.println("Condensing time: " + (time5-time4) + " ms");
        tree.contractTree();
        long time6 = new Date().getTime();
        System.out.println("Contracting time: " + (time6-time5) + " ms");
        double[][] softClustering = tree.calculateSoftClustering(initialCuts[0].size());
        long time7 = new Date().getTime();
        System.out.println("Clustering time: " + (time7-time6) + " ms");
        for (int i = softClustering.length/2-10; i < softClustering.length/2+40; i++) {
            for (double d : softClustering[i]) {
                System.out.print(d + " ");
            }
            System.out.println();
        }
        System.out.println("Number of clusters found: " + softClustering[0].length);
        long time8 = new Date().getTime();
        int count = 0;
        for (int i = 0; i < softClustering.length; i++) {
            double max = 0;
            int cluster = -1;
            for (int j = 0; j < softClustering[i].length; j++) {
                if (softClustering[i][j] > max) {
                    max = softClustering[i][j];
                    cluster = j;
                }
            }
            if (i < softClustering.length/2 && cluster == 0) {
                count++;
            }
            else if (i >= softClustering.length/2 && cluster == 1) {
                count++;
            }
        }
        System.out.println("Wrong clusterings: " + count + " Percentage correct: " + (((double)softClustering.length-count)/softClustering.length));
        System.out.println("Total tangle search tree time: " + (time8-time3) + " ms");
        System.out.println();
    }

    private static TangleSearchTree generateTangleSearchTree(BitSet[] initialCuts, int[] costs, int a, int psi) {
        int[] indices = new int[costs.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        quicksort(costs, indices, 0, costs.length-1);
        TangleSearchTree tree = new TangleSearchTree(a, initialCuts, costs);
        for (int i = 0; i < costs.length; i++) {
            if (psi > 0 && costs[i] > psi) {
                break;
            }
            boolean consistent = false;
            for (Node node : tree.lowestDepthNodes) {
                consistent = tree.addOrientation(node, indices[i], true) || consistent;
                consistent = tree.addOrientation(node, indices[i], false) || consistent;
            }
            if (!consistent) { //Stop if no nodes were added to the tree.
                break;
            }
        }
        return tree;
    }

    private static void quicksort(int[] costs, int[] indices, int l, int h) {
        if (l >= h || l < 0) {
            return;
        }
        int p = partition(costs, indices, l, h);
        quicksort(costs, indices, l, p-1);
        quicksort(costs, indices, p+1, h);
    }

    private static int partition(int[] costs, int[] indices, int l, int h) {
        int pivot = costs[h];
        int i = l-1;
        for (int j = l; j < h; j++) {
            if (costs[j] <= pivot) {
                i = i + 1;
                int temp = costs[i];
                costs[i] = costs[j];
                costs[j] = temp;
                temp = indices[i];
                indices[i] = indices[j];
                indices[j] = temp;
            }
        }
        i = i + 1;
        int temp = costs[i];
        costs[i] = costs[h];
        costs[h] = temp;
        temp = indices[i];
        indices[i] = indices[h];
        indices[h] = temp;
        return i;
    }

}
