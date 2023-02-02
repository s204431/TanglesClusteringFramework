package main;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
        System.out.println("Tangle search tree time: " + (time4-time3) + " ms");
        System.out.println();
        System.out.println(tree.lowestDepthNodes.size());
        System.out.println(tree.getDepth(tree.lowestDepthNodes.get(0)));
        System.out.println(tree.n);
        tree.printTree();
    }

    private static TangleSearchTree generateTangleSearchTree(BitSet[] initialCuts, int[] costs, int a, int psi) {
        TangleSearchTree tree = new TangleSearchTree(a);
        int[] indices = new int[costs.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        quicksort(costs, indices, 0, costs.length-1);
        for (int i = 0; i < costs.length; i++) {
            if (psi > 0 && costs[i] > psi) {
                break;
            }
            for (Node node : tree.lowestDepthNodes) {
                BitSet orientation = initialCuts[indices[i]];
                tree.addOrientation(node, orientation, true);
                tree.addOrientation(node, orientation, false);
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
