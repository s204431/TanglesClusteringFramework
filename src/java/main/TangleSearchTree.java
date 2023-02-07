package main;

import java.util.*;

public class TangleSearchTree {

    private static final boolean USE_HASHING = false; //Determines if hashing of intersections is used.
    private int a;
    public Node root;
    public List<Node> lowestDepthNodes = new ArrayList<>();
    private int currentDepth = -1;

    private BitSet[] orientations;
    private double[] cutCosts;
    private int integerBits; //Number of bits to represent the index of an orientation.
    private Hashtable<Long, Integer> hashtable = new Hashtable();

    public TangleSearchTree(int a, BitSet[] orientations, double[] cutCosts) {
        this.a = a;
        this.orientations = orientations;
        this.cutCosts = cutCosts;
        integerBits = (int)(Math.log(cutCosts.length)/Math.log(2))+1;
        root = new Node();
        lowestDepthNodes.add(root);
    }

    public int n;
    public boolean addOrientation(Node node, int orientationIndex, boolean left) {
        Node newNode = new Node(orientationIndex, left);
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
            if (orientations[newNode.originalOrientation].size() < a) {
                return false;
            }
            else {
                return true;
            }
        }
        if (depth == 2) {
            int intersection = BitSet.intersectionEarlyStop(orientations[newNode.originalOrientation], orientations[newNode.parent.originalOrientation], newNode.side, newNode.parent.side, a);
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
                int intersection;
                if (USE_HASHING) {
                    int hashed = getHashValue(newNode.originalOrientation, otherNodes[i].originalOrientation, otherNodes[j].originalOrientation, newNode.side, otherNodes[i].side, otherNodes[j].side);
                    if (hashed >= 0) {
                        intersection = hashed;
                    }
                    else {
                        intersection = BitSet.intersectionEarlyStop(orientations[newNode.originalOrientation], orientations[otherNodes[i].originalOrientation], orientations[otherNodes[j].originalOrientation], newNode.side, otherNodes[i].side, otherNodes[j].side, a);
                        addToHash(newNode.originalOrientation, otherNodes[i].originalOrientation, otherNodes[j].originalOrientation, newNode.side, otherNodes[i].side, otherNodes[j].side, intersection);
                    }
                }
                else {
                    intersection = BitSet.intersectionEarlyStop(orientations[newNode.originalOrientation], orientations[otherNodes[i].originalOrientation], orientations[otherNodes[j].originalOrientation], newNode.side, otherNodes[i].side, otherNodes[j].side, a);
                }
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

    public double[][] calculateSoftClustering(int numberOfDataPoints) {
        int clusters = getNumberOfClusters(root);
        double[][] result = new double[numberOfDataPoints][clusters];
        for (int i = 0; i < numberOfDataPoints; i++) {
            getSoftClustering(root, i, 0, 1, result[i]);
        }
        return result;
    }

    private double getWeight(double cost) {
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
            for (int distinguished : node.distinguishedCuts) {
                if (node.leftChild.condensedOrientations.get(distinguished)) {
                    sum2 += getWeight(cutCosts[distinguished]);
                    if (orientations[distinguished].get(datapoint)) {
                        sum1 += getWeight(cutCosts[distinguished]);
                    }
                }
                if (node.leftChild.condensedOrientations.get(distinguished+node.leftChild.condensedOrientations.size()/2)) {
                    sum2 += getWeight(cutCosts[distinguished]);
                    if (!orientations[distinguished].get(datapoint)) {
                        sum1 += getWeight(cutCosts[distinguished]);
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
            int size = node.leftChild.condensedOrientations.size();
            for (int i = 0; i < size; i++) {
                if (node.leftChild.condensedOrientations.get(i)) {
                    if (node.rightChild.condensedOrientations.get(i)) { //Left and right child orient this cut the same way.
                        node.condensedOrientations.add(i);
                    }
                    else if ((i < size/2 && node.rightChild.condensedOrientations.get(i+size/2)) || (i >= size/2 && node.rightChild.condensedOrientations.get(i-size/2))) { //Oriented different ways.
                        node.distinguishedCuts.add(i < size/2 ? i : i-size/2);
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
        child.condensedOrientations.unionWith(node.condensedOrientations);
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

    private void addToHash(long cut1, long cut2, long cut3, boolean side1, boolean side2, boolean side3, int value) {
        long hashKey = getHashKey(cut1, cut2, cut3, side1, side2, side3);
        hashtable.put(hashKey, value);
    }

    public int getHashValue(long cut1, long cut2, long cut3, boolean side1, boolean side2, boolean side3) {
        long hashKey = getHashKey(cut1, cut2, cut3, side1, side2, side3);
        Integer hashValue = hashtable.get(hashKey);
        if (hashValue != null) {
            return hashValue;
        }
        return -1;
    }

    public long getHashKey(long cut1, long cut2, long cut3, boolean side1, boolean side2, boolean side3) {
        long l1 = ((side1 ? 0L : 1L) << ((integerBits+1)*3-1)) | (cut1 << (integerBits+1)*2);
        long l2 = ((side2 ? 0L : 1L) << ((integerBits+1)*2-1)) | (cut2 << (integerBits+1));
        long l3 = ((side3 ? 0L : 1L) << integerBits) | cut3;
        return l1 | l2 | l3;
    }

    public class Node {

        public int originalOrientation;
        public BitSet condensedOrientations;
        public List<Integer> distinguishedCuts = new ArrayList<>();
        public Node leftChild;
        public Node rightChild;
        public Node parent;
        public boolean side;
        public int originalDepth = 1;

        public Node() {
            condensedOrientations = new BitSet(orientations.length*2);
        }

        public Node(int orientationIndex, boolean side) {
            this.originalOrientation = orientationIndex;
            this.side = side;
            condensedOrientations = new BitSet(orientations.length*2);
            condensedOrientations.add(side ? orientationIndex : orientationIndex+condensedOrientations.size()/2);
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
