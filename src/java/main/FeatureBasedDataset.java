package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class FeatureBasedDataset implements Dataset {

    public double[][] dataPoints;
    private BitSet[] initialCuts;
    private int a;

    public FeatureBasedDataset(int a) {
        this.a = a;
    }

    public FeatureBasedDataset(double[][] dataPoints, int a) {
        this.dataPoints = dataPoints;
        this.a = a;
    }

    @Override
    public BitSet[] getInitialCuts() {
        List<BitSet> cuts = new ArrayList<>();
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            for (int j = 0; j < dataPoints[0].length; j++) {
                copy[i][j] = dataPoints[i][j];
            }
        }
        for (int i = 0; i < dataPoints[0].length; i++) {
            mergeSort(copy, originalIndices, i, 0, dataPoints.length-1);
            //BitSet first = new BitSet(dataPoints.length);
            //first.add(originalIndices[0]);
            //cuts.add(first);
            BitSet currentBitSet = new BitSet(dataPoints.length);
            cuts.add(currentBitSet);
            for (int j = 0; j < dataPoints.length; j++) {
                currentBitSet.add(originalIndices[j]);
                if (j % a == 0) {
                    if (dataPoints.length - j <= a - 1) {
                        break;
                    }
                    BitSet newBitSet = new BitSet(dataPoints.length);
                    newBitSet.unionWith(currentBitSet);
                    currentBitSet = newBitSet;
                    cuts.add(currentBitSet);
                }
            }
        }
        BitSet[] result = new BitSet[cuts.size()];
        for (int i = 0; i < cuts.size(); i++) {
            result[i] = cuts.get(i);
        }
        initialCuts = result;
        return result;
    }

    //Sorts data points by a specific dimension.
    public static void mergeSort(double[][] points, int[] originalIndices, int dimension, int l, int h) {
        if (l >= h) {
            return;
        }
        mergeSort(points, originalIndices, dimension, l, (l+h)/2);
        mergeSort(points, originalIndices, dimension, (l+h)/2+1, h);
        merge(points, originalIndices, dimension, l, h);
    }

    private static void merge(double[][] points, int[] originalIndices, int dimension, int l, int h) {
        double[][] L = new double[(h-l)/2+1][];
        double[][] R = new double[(h-l) % 2 == 0 ? (h-l)/2 : (h-l)/2+1][];
        int[] L2 = new int[L.length];
        int[] R2 = new int[R.length];
        for (int i = 0; i < L.length; i++) {
            L[i] = points[l+i];
            L2[i] = originalIndices[l+i];
        }
        for (int i = 0; i < R.length; i++) {
            R[i] = points[l+L.length+i];
            R2[i] = originalIndices[l+L.length+i];
        }
        int p1 = 0;
        int p2 = 0;
        for (int i = l; i <= h; i++) {
            if ((p2 >= R.length) || (p1 < L.length && L[p1][dimension] < R[p2][dimension])) {
                points[i] = L[p1];
                originalIndices[i] = L2[p1];
                p1++;
            }
            else {
                points[i] = R[p2];
                originalIndices[i] = R2[p2];
                p2++;
            }
        }
    }

    @Override
    public double[] getCutCosts() {
        double[] costs = new double[initialCuts.length];
        for (int i = 0; i < initialCuts.length; i++) {
            double cost = 0;
            for (int j = 0; j < dataPoints.length; j++) {
                if (initialCuts[i].get(j)) {
                    continue;
                }
                for (int k = 0; k < dataPoints.length; k++) {
                    if (!initialCuts[i].get(k)) {
                        continue;
                    }
                    cost += Math.exp(-getDistance(dataPoints[j], dataPoints[k]));
                }
            }
            costs[i] = cost/(initialCuts[i].count()*(initialCuts[i].size()-initialCuts[i].count()));
            //costs[i] = cost;
        }
        return costs;
    }

    private double getDistance(double[] point1, double[] point2) {
        double length = 0;
        for (int i = 0; i < point1.length; i++) {
            length += (point1[i]-point2[i])*(point1[i]-point2[i]);
        }
        return Math.sqrt(length);
    }

    public void loadDataFromFile(String fileName, int startRow, int endRow, int startColumn, int endColumn) {
        try {
            List<List<Double>> result = new ArrayList<>();
            Scanner fileScanner = new Scanner(new File("datasets/" + fileName));
            for (int i = 0; i < startRow; i++) {
                fileScanner.nextLine();
            }
            int line = startRow;
            while (fileScanner.hasNextLine() && (endRow < 0 || line <= endRow)) {
                result.add(new ArrayList<>());
                Scanner lineScanner = new Scanner(fileScanner.nextLine());
                lineScanner.useLocale(Locale.ENGLISH);
                lineScanner.useDelimiter(",");
                for (int i = 0; i < startColumn; i++) {
                    lineScanner.next();
                }
                int column = startColumn;
                while (lineScanner.hasNextDouble() && (endColumn < 0 || column <= endColumn)) {
                    double nextDouble = lineScanner.nextDouble();
                    if (Double.isNaN(nextDouble)) {
                        result.remove(result.size()-1);
                        break;
                    }
                    result.get(result.size()-1).add(nextDouble);
                    column++;
                }
                line++;
            }
            dataPoints = new double[result.size()][result.get(0).size()];
            for (int i = 0; i < dataPoints.length; i++) {
                if (result.get(i).size() != dataPoints[i].length) { //File not valid
                    throw new Exception();
                }
                for (int j = 0; j < dataPoints[i].length; j++) {
                    dataPoints[i][j] = result.get(i).get(j);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void print() {
        for (int i = 0; i < dataPoints.length; i++) {
            for (int j = 0; j < dataPoints[i].length; j++) {
                System.out.print(dataPoints[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println(dataPoints.length + " " + dataPoints[0].length);
    }
}
