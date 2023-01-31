package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TangleSearchTree {

    private int a;
    public Node root = new Node(null);
    public List<Node> lowestDepthNodes = new ArrayList<>();
    private int currentDepth = -1;

    public TangleSearchTree(int a) {
        this.a = a;
        lowestDepthNodes.add(root);
    }

    public boolean addOrientation(Node node, Set<Integer> orientation, boolean left) {
        Node newNode = new Node(orientation);
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
            int intersection = intersection(newNode.orientation, newNode.parent.orientation);
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
                int intersection = intersection(newNode.orientation, otherNodes[i].orientation, otherNodes[j].orientation);
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

    public class Node {

        public Set<Integer> orientation = new HashSet<Integer>();
        public Node leftChild;
        public Node rightChild;
        public Node parent;

        public Node(Set<Integer> orientation) {
            this.orientation = orientation;
        }
    }
}
