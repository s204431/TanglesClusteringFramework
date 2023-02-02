package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TangleSearchTree {

    private int a;
    public Node root = new Node(null, false);
    public List<Node> lowestDepthNodes = new ArrayList<>();
    private int currentDepth = -1;

    public TangleSearchTree(int a) {
        this.a = a;
        lowestDepthNodes.add(root);
    }

    public int n;
    public boolean addOrientation(Node node, BitSet orientation, boolean left) {
        Node newNode = new Node(orientation, left);
        newNode.parent = node;
        if (left) {
            node.leftChild = newNode;
        }
        else {
            node.rightChild = newNode;
        }
        boolean consistent = isConsistent(newNode);
        if (!consistent) {
            newNode.parent = null;
            if (left) {
                node.leftChild = null;
            }
            else {
                node.rightChild = null;
            }
        }
        else {
            n++;
            int depth = getDepth(newNode);
            if (depth != currentDepth) {
                lowestDepthNodes = new ArrayList<>();
                currentDepth = depth;
            }
            lowestDepthNodes.add(newNode);
        }
        return consistent;
    }

    public boolean isConsistent(Node newNode) {
        int depth = getDepth(newNode);
        if (depth < 2) {
            if (newNode.originalOrientation.size() < a) {
                return false;
            }
            else {
                return true;
            }
        }
        if (depth == 2) {
            int intersection = BitSet.intersection(newNode.originalOrientation, newNode.parent.originalOrientation, newNode.side, newNode.parent.side);
            //Set<Integer> copy = new HashSet<>(newNode.orientation);
            //copy.retainAll(newNode.parent.orientation);
            //int intersection = copy.size();
            if (intersection < a) {
                return false;
            }
            else {
                return true;
            }
        }
        Node[] otherNodes = new Node[depth-1];
        otherNodes[0] = newNode.parent;
        for (int i = 1; i < depth-1; i++) {
            otherNodes[i] = otherNodes[i-1].parent;
        }
        for (int i = 0; i < depth-1; i++) {
            for (int j = i+1; j < depth-1; j++) {
                int intersection = BitSet.intersection(newNode.originalOrientation, otherNodes[i].originalOrientation, otherNodes[j].originalOrientation, newNode.side, otherNodes[i].side, otherNodes[j].side);
                //Set<Integer> copy = new HashSet<>(newNode.orientation);
                //copy.retainAll(otherNodes[i].orientation);
                //copy.retainAll(otherNodes[j].orientation);
                //int intersection = copy.size();
                if (intersection < a) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getDepth(Node node) {
        int depth = 0;
        while (node.parent != null) {
            node = node.parent;
            depth++;
        }
        return depth;
    }

    //Computes the size of the intersection of two given sets.
    private int intersection(Set<Integer> set1, Set<Integer> set2) {
        int count = 0;
        for (int i : set1) {
            if (set2.contains(i)) {
                count++;
            }
        }
        return count;
    }

    //Computes the size of the intersection of three given sets.
    private int intersection(Set<Integer> set1, Set<Integer> set2, Set<Integer> set3) {
        int count = 0;
        for (int i : set1) {
            if (set2.contains(i) && set3.contains(i)) {
                count++;
            }
        }
        return count;
    }

    public double[][] calculateSoftClustering(int numberOfDataPoints) {
        int clusters = getNumberOfClusters(root);
        double[][] result = new double[numberOfDataPoints][clusters];
        for (int i = 0; i < numberOfDataPoints; i++) {
            getSoftClustering(root, i, 0, 1, result[i]);
        }
        return result;
    }

    private double getWeight(int cost) {
        return 1.0/cost;
    }

    private int getSoftClustering(Node node, int datapoint, int index, double accumulated, double[] result) {
        if (node.getChildCount() == 0) {
            result[index] = accumulated;
            return index+1;
        }
        else {
            double sum1 = 0;
            double sum2 = 0;
            for (BitSet distinguished : node.distinguishedCuts) {
                for (int i = 0; i < node.leftChild.condensedOrientations.size(); i++) {
                    BitSet condensed = node.leftChild.condensedOrientations.get(i);
                    if (distinguished == condensed) {
                        sum2 += getWeight(condensed.cutCost);
                        boolean side = node.leftChild.sides.get(i);
                        if (side == condensed.get(datapoint)) {
                            sum1 += getWeight(condensed.cutCost);
                        }
                    }
                }
            }
            double prob = sum1/sum2;
            index = getSoftClustering(node.leftChild, datapoint, index, accumulated*prob, result);
            index = getSoftClustering(node.rightChild, datapoint, index, accumulated*(1-prob), result);
            return index;
        }
    }

    private int getNumberOfClusters(Node node) {
        if (node.getChildCount() == 0) {
            return 1;
        }
        else {
            return getNumberOfClusters(node.leftChild) + getNumberOfClusters(node.rightChild);
        }
    }

    public void contractTree() {
        contractTree(root);
    }

    private void contractTree(Node node) {
        if (node.getChildCount() > 0) { //This is not a leaf.
            contractTree(node.leftChild);
            contractTree(node.rightChild);
            for (int i = 0; i < node.leftChild.condensedOrientations.size(); i++) {
                BitSet cut1 = node.leftChild.condensedOrientations.get(i);
                boolean side1 = node.leftChild.sides.get(i);
                for (int j = 0; j < node.rightChild.condensedOrientations.size(); j++) {
                    BitSet cut2 = node.rightChild.condensedOrientations.get(j);
                    boolean side2 = node.rightChild.sides.get(j);
                    if (cut1 == cut2) {
                        if (side1 == side2) {
                            node.condensedOrientations.add(cut1);
                            node.sides.add(side1);
                        }
                        else {
                            node.distinguishedCuts.add(cut1);
                        }
                    }
                }
            }
        }
    }

    //Removes branches of length "pruneDepth" or lower from the tree.
    public void condenseTree(int pruneDepth) {
        removeInternalNodes(root);
        pruneBranches(root, pruneDepth);
    }

    private void pruneBranches(Node node, int pruneDepth) {
        if (node.getChildCount() == 0) { //This is a leaf.
            if (node.originalDepth <= pruneDepth) {
                if (node.parent.leftChild == node) {
                    node.parent.leftChild = null;
                }
                else {
                    node.parent.rightChild = null;
                }
                if (node.parent.getChildCount() == 1) {
                    removeNode(node.parent);
                }
            }
        }
        else { //This is not a leaf.
            pruneBranches(node.leftChild, pruneDepth);
            pruneBranches(node.rightChild, pruneDepth);
        }
    }

    private void removeNode(Node node) {
        Node child = node.leftChild == null ? node.rightChild : node.leftChild;
        child.originalDepth++;
        child.parent = node.parent;
        if (node.parent != null) { //Not root.
            if (node.parent.leftChild == node) {
                node.parent.leftChild = child;
            }
            else{
                node.parent.rightChild = child;
            }
        }
        else {
            root = child;
        }
        for (int i = 0; i < node.condensedOrientations.size(); i++) {
            BitSet cut = node.condensedOrientations.get(i);
            boolean side = node.sides.get(i);
            boolean found = false;
            for (int j = 0; j < child.condensedOrientations.size(); j++) {
                if (child.condensedOrientations.get(i) == cut && child.sides.get(i) == side) {
                    found = true;
                }
            }
            if (!found) {
                child.condensedOrientations.add(cut);
                child.sides.add(side);
            }
        }
    }

    private void removeInternalNodes(Node node) {
        if (node.leftChild != null) {
            removeInternalNodes(node.leftChild);
        }
        if (node.rightChild != null) {
            removeInternalNodes(node.rightChild);
        }
        if (node.getChildCount() == 1) { //Remove node.
            removeNode(node);
        }
    }

    //Prints the side of the cut for each node in the tree (for debugging).
    public void printTree() {
        List<Node> currentNodes = new ArrayList<>();
        currentNodes.add(root);
        while (!currentNodes.isEmpty()) {
            for (int i = 0; i < currentNodes.size(); i++) {
                System.out.print(currentNodes.get(i).side + " " + currentNodes.get(i).getChildCount());
            }
            List<Node> newNodes = new ArrayList<>();
            for (Node node : currentNodes) {
                if (node.leftChild != null) {
                    newNodes.add(node.leftChild);
                }
                if (node.rightChild != null) {
                    newNodes.add(node.rightChild);
                }
            }
            currentNodes = newNodes;
            System.out.println();
        }
    }

    public class Node {

        public BitSet originalOrientation;
        public List<BitSet> condensedOrientations = new ArrayList<>();
        public List<BitSet> distinguishedCuts = new ArrayList<>();
        public List<Boolean> sides = new ArrayList<>();
        public Node leftChild;
        public Node rightChild;
        public Node parent;
        public boolean side;
        public int originalDepth = 1;

        public Node(BitSet orientation, boolean side) {
            this.originalOrientation = orientation;
            this.side = side;
            condensedOrientations.add(orientation);
            sides.add(side);
        }

        public int getChildCount() {
            int count = 0;
            if (leftChild != null) {
                count++;
            }
            if (rightChild != null) {
                count++;
            }
            return count;
        }
    }
}
