package main;

import java.util.HashSet;
import java.util.Set;
import main.TangleSearchTree.Node;

public class TangleClusterer {

    public static void generateClusters(Dataset dataset, int a) {
        boolean[][] initialCuts = dataset.getInitialCuts();
        int[] costs = dataset.getCutCosts();
        for (int cost : costs) {
            System.out.print(cost + " ");
        }
        TangleSearchTree tree = generateTangleSearchTree(initialCuts, costs, a);
        System.out.println();
        System.out.println(tree.lowestDepthNodes.size());
        System.out.println(tree.getDepth(tree.lowestDepthNodes.get(0)));
    }

    private static TangleSearchTree generateTangleSearchTree(boolean[][] initialCuts, int[] costs, int a) {
        TangleSearchTree tree = new TangleSearchTree(a);
        int[] indices = new int[costs.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        quicksort(costs, indices, 0, costs.length-1);
        for (int i = 0; i < costs.length; i++) {
            for (Node node : tree.lowestDepthNodes) {
                Set<Integer> leftOrientation = new HashSet<>();
                Set<Integer> rightOrientation = new HashSet<>();
                for (int j = 0; j < initialCuts[indices[i]].length; j++) {
                    if (initialCuts[indices[i]][j]) {
                        leftOrientation.add(j);
                    }
                    else {
                        rightOrientation.add(j);
                    }
                }
                tree.addOrientation(node, leftOrientation, true);
                tree.addOrientation(node, rightOrientation, false);
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
