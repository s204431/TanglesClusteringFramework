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
            if (newNode.orientation.size() < a) {
                return false;
            }
            else {
                return true;
            }
        }
        if (depth == 2) {
            int intersection = BitSet.intersection(newNode.orientation, newNode.parent.orientation, newNode.side, newNode.parent.side);
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
                int intersection = BitSet.intersection(newNode.orientation, otherNodes[i].orientation, otherNodes[j].orientation, newNode.side, otherNodes[i].side, otherNodes[j].side);
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

    //Prints the side of the cut for each node in the tree (for debugging).
    public void printTree() {
        List<Node> currentNodes = new ArrayList<>();
        currentNodes.add(root);
        while (!currentNodes.isEmpty()) {
            for (int i = 0; i < currentNodes.size(); i++) {
                System.out.print(currentNodes.get(i).side + "\t");
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

        public BitSet orientation;
        public Node leftChild;
        public Node rightChild;
        public Node parent;
        public boolean side;

        public Node(BitSet orientation, boolean side) {
            this.orientation = orientation;
            this.side = side;
        }
    }
}
