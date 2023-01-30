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

    public boolean addOrientation(Node node, Set<Integer> orientation) {
        boolean leftChild = node.leftChild == null;
        Node newNode = new Node(orientation);
        newNode.parent = node;
        if (leftChild) {
            node.leftChild = newNode;
        }
        else {
            node.rightChild = newNode;
        }
        boolean consistent = isConsistent(newNode);
        if (!consistent) {
            newNode.parent = null;
            if (leftChild) {
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
                lowestDepthNodes.add(newNode);
                currentDepth = depth;
            }
            else {
                lowestDepthNodes.add(newNode);
            }
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
            Set<Integer> copy = new HashSet<>(newNode.orientation);
            copy.retainAll(newNode.parent.orientation);
            if (copy.size() < a) {
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
                Set<Integer> copy = new HashSet<>(newNode.orientation);
                copy.retainAll(otherNodes[i].orientation);
                copy.retainAll(otherNodes[j].orientation);
                if (copy.size() < a) {
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
