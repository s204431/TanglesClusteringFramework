package datasets;

import model.Model;
import smile.clustering.HierarchicalClustering;
import smile.clustering.KMeans;
import smile.clustering.PartitionClustering;
import smile.clustering.SpectralClustering;
import smile.clustering.linkage.CompleteLinkage;
import util.BitSet;
import util.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FeatureBasedDataset extends Dataset {

    public static final String name = "Feature Based";
    private static final int precision = 1; //Determines the number of cuts generated.
    public double[][] dataPoints;
    private int a;
    public double[][] axisParallelCuts; //Only used when cuts are axis parallel. Used for visualization.
    public boolean cutsAreAxisParallel = true;
    public double[] cutCosts; //Used for visualization.

    public FeatureBasedDataset() {

    }

    public FeatureBasedDataset(double[][] dataPoints) {
        this.dataPoints = dataPoints;
    }

    public FeatureBasedDataset(Tuple<double[][], int[]> dataPointsWithGroundTruth) {
        dataPoints = dataPointsWithGroundTruth.x;
        groundTruth = dataPointsWithGroundTruth.y;
    }

    public int[] getGroundTruth() {
        return groundTruth;
    }

    public void setA(int a) {
        this.a = a;
    }

    public BitSet[] getInitialCutsOld() {
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
            for (int j = 0; j < dataPoints.length-1; j++) {
                currentBitSet.add(originalIndices[j]);
                if (j > 0 && j % (a/precision) == 0) {
                    if (dataPoints.length - j <= (a/precision) - 1) {
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

    public BitSet[] getInitialCutsRange() {
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            for (int j = 0; j < dataPoints[0].length; j++) {
                copy[i][j] = dataPoints[i][j];
            }
        }
        for (int i = 0; i < dataPoints[0].length; i++) {
            axisParallelCuts[i] = new ArrayList<>();
            mergeSort(copy, originalIndices, i, 0, dataPoints.length-1);
            BitSet currentBitSet = new BitSet(dataPoints.length);
            currentBitSet.setAll();
            cuts.add(currentBitSet);
            BitSet accumulated = new BitSet(dataPoints.length);
            accumulated.setAll();
            axisParallelCuts[i].add(dataPoints[originalIndices[0]][i]);
            int cutIndex = 0;
            for (int j = 0; j < dataPoints.length; j++) {
                accumulated.remove(originalIndices[j]);
                if (j <= cutIndex) {
                    currentBitSet.remove(originalIndices[j]);
                }
                if (j > 0 && j % (a/precision) == 0) {
                    if (dataPoints.length - j <= (a/precision) - 1) {
                        break;
                    }
                    currentBitSet = new BitSet(dataPoints.length);
                    currentBitSet.unionWith(accumulated);
                    cuts.add(currentBitSet);
                    //Find where to put the cut.
                    double maxRange = -1;
                    for (int k = j+1; k < j+a/precision-1; k++) {
                        if (copy[k+1][i] - copy[k][i] > maxRange) {
                            maxRange = copy[k+1][i] - copy[k][i];
                            cutIndex = k;
                        }
                    }
                    axisParallelCuts[i].add(dataPoints[originalIndices[cutIndex]][i]);
                }
            }
        }
        BitSet[] result = new BitSet[cuts.size()];
        for (int i = 0; i < cuts.size(); i++) {
            result[i] = cuts.get(i);
        }
        initialCuts = result;
        this.axisParallelCuts = new double[axisParallelCuts.length][];
        for (int i = 0; i < axisParallelCuts.length; i++) {
            this.axisParallelCuts[i] = new double[axisParallelCuts[i].size()];
            for (int j = 0; j < axisParallelCuts[i].size(); j++) {
                this.axisParallelCuts[i][j] = axisParallelCuts[i].get(j);
            }
        }
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
        cutCosts = distanceToMeanCostFunction();
        /*for (int i = 0; i < initialCuts.length; i++) {
            System.out.println(costs[i]);
            int[] clusters = new int[dataPoints.length];
            for (int j = 0; j < initialCuts[i].size(); j++) {
                clusters[j] = initialCuts[i].get(j) ? 1 : 0;
            }
            Model model = new Model();
            model.setDataset(this);
            View view = new View(model);
            view.loadDataPoints();
            view.loadClusters(clusters, null);
            view.selectedSidePanel.setValues(costs[i], 0);
        }*/
        return cutCosts;
    }

    private double[] testCostFunction() {
        double[] costs = new double[initialCuts.length];
        for (int i = 0; i < initialCuts.length; i++) {
            double[] mean1 = new double[dataPoints[0].length];
            double[] mean2 = new double[dataPoints[0].length];
            int n1 = 0;
            int n2 = 0;
            for (int k = 0; k < dataPoints.length; k++) {
                for (int l = 0; l < dataPoints[k].length; l++) {
                    if (initialCuts[i].get(k)) {
                        mean1[l] += dataPoints[k][l];
                        if (l == 0) {
                            n1++;
                        }
                    }
                    else {
                        mean2[l] += dataPoints[k][l];
                        if (l == 0) {
                            n2++;
                        }
                    }
                }
            }
            for (int k = 0; k < mean1.length; k++) {
                mean1[k] /= n1;
                mean2[k] /= n2;
            }
            for(int j = 0; j < dataPoints.length; j++) {
                double[] ownMean = initialCuts[i].get(j) ? mean1 : mean2;
                double[] otherMean = ownMean == mean1 ? mean2 : mean1;
                if (getDistance(dataPoints[j], otherMean) < getDistance(dataPoints[j], ownMean)) {
                    costs[i]++;
                }
            }
        }
        cutCosts = costs;
        return costs;
    }

    private double[] pairwiseDistanceCostFunction() {
        double[] costs = new double[initialCuts.length];
        double maxRange = getMaxRange();
        for (int i = 0; i < initialCuts.length; i++) {
            double cost = 0;
            for (int j = 0; j < dataPoints.length; j++) {
                if (initialCuts[i].get(j)) {
                    continue;
                }
                for (int k = j; k < dataPoints.length; k++) {
                    if (!initialCuts[i].get(k)) {
                        continue;
                    }
                    cost += Math.exp(-5.0*(1.0/maxRange)*getDistance(dataPoints[j], dataPoints[k]));
                }
            }
            costs[i] = cost/(initialCuts[i].count()*(initialCuts[i].size()-initialCuts[i].count()));
        }
        cutCosts = costs;
        return costs;
    }

    private double[] distanceToMeanCostFunction() {
        double[] costs = new double[initialCuts.length];
        double maxRange = getMaxRange();
        for (int i = 0; i < initialCuts.length; i++) {
            int cutCount = initialCuts[i].count();
            double[] mean1 = new double[dataPoints[0].length];
            double[] mean2 = new double[dataPoints[0].length];
            //Calculate means of the two sides of the cut.
            for (int j = 0; j < initialCuts[i].size(); j++) {
                for (int k = 0; k < dataPoints[0].length; k++) {
                    if (initialCuts[i].get(j)) {
                        mean1[k] += dataPoints[j][k];
                    }
                    else {
                        mean2[k] += dataPoints[j][k];
                    }
                }
            }
            for (int j = 0; j < mean1.length; j++) {
                mean1[j] /= cutCount;
                mean2[j] /= initialCuts[i].size() - cutCount;
            }
            //Sum up distances from the means.
            for (int j = 0; j < initialCuts[i].size(); j++) {
                double[] mean = initialCuts[i].get(j) ? mean2 : mean1;
                int otherSideSize = initialCuts[i].get(j) ? initialCuts[i].size() - cutCount : cutCount;
                costs[i] += Math.exp(-(1.0/maxRange)*getDistance(dataPoints[j], mean));//*otherSideSize;
            }
            //costs[i] /= initialCuts[i].count()*(initialCuts[i].size() - initialCuts[i].count());
        }
        cutCosts = costs;
        return costs;
    }

    private double getMaxRange() {
        double maxRange = 0;
        for (int i = 0; i < dataPoints.length; i++) {
            double minValue = Double.MAX_VALUE;
            double maxValue = Double.MIN_VALUE;
            for (int j = 0; j < dataPoints[i].length; j++) {
                if (dataPoints[i][j] < minValue) {
                    minValue = dataPoints[i][j];
                }
                if (dataPoints[i][j] > maxValue) {
                    maxValue = dataPoints[i][j];
                }
            }
            if (maxValue - minValue > maxRange) {
                maxRange = maxValue - minValue;
            }
        }
        return maxRange;
    }

    private double getMaxRange(int dimension) {
        double minValue = Integer.MAX_VALUE;
        double maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < dataPoints.length; i++) {
            if (dataPoints[i][dimension] < minValue) {
                minValue = dataPoints[i][dimension];
            }
            if (dataPoints[i][dimension] > maxValue) {
                maxValue = dataPoints[i][dimension];
            }
        }
        return maxValue - minValue;
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
            File file = new File(fileName);
            Scanner fileScanner = new Scanner(file);
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
            loadGroundTruth(file, startRow, endRow == -1 ? dataPoints.length-1 : endRow);
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

    //Performs K-means clustering on a feature based dataset
    public int[] kMeans(int k) {
        KMeans clusters = PartitionClustering.run(1, () -> KMeans.fit(dataPoints, k));
        return clusters.y;
    }

    //Performs spectral clustering on a feature based dataset
    public int[] spectralClustering(int k, double sigma) {
        SpectralClustering clusters = SpectralClustering.fit(dataPoints, k, sigma);
        return clusters.y;
    }

    //Performs hierarchical clustering on a feature based dataset
    public int[] hierarchicalClustering(int k) {
        HierarchicalClustering clusters = HierarchicalClustering.fit(CompleteLinkage.of(dataPoints));
        return clusters.partition(k);
    }

    public void printKMeansResults(int[] resultingClustering) {

        //Print ground truth vs k-means clusters
        System.out.println("Ground truth: ");
        for (int i = 0; i < groundTruth.length; i++) {
            System.out.print(groundTruth[i] + " ");
        }
        System.out.println("\nK-means: ");
        for (int i = 0; i < resultingClustering.length; i++) {
            System.out.print(resultingClustering[i] + " ");
        }

    }

    public String[] getSupportedAlgorithms() {
        return new String[] {Model.tangleName, Model.kMeansName, Model.spectralClusteringName, Model.linkageName};
    }

    public void saveToFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < dataPoints.length; i++) {
                for (int j = 0; j < dataPoints[i].length; j++) {
                    writer.write(""+dataPoints[i][j]);
                    if (j != dataPoints[i].length-1) {
                        writer.write(",");
                    }
                }
                if (i != dataPoints.length-1) {
                    writer.write("\n");
                }
            }
            writer.close();
            saveGroundTruth(file);
        } catch (IOException e) {}
    }

    public String getName() {
        return name;
    }

    public BitSet[] getInitialCutsLocalMeans() {
        double range = getMaxRange();
        List<Double> costs = new ArrayList<>();
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            for (int j = 0; j < dataPoints[0].length; j++) {
                copy[i][j] = dataPoints[i][j];
            }
        }
        for (int i = 0; i < dataPoints[0].length; i++) {
            axisParallelCuts[i] = new ArrayList<>();
            mergeSort(copy, originalIndices, i, 0, dataPoints.length-1);
            BitSet currentBitSet = new BitSet(dataPoints.length);
            currentBitSet.setAll();
            cuts.add(currentBitSet);
            BitSet accumulated = new BitSet(dataPoints.length);
            accumulated.setAll();
            axisParallelCuts[i].add(dataPoints[originalIndices[0]][i]);
            int cutIndex = 0;
            double[] mean1 = null;
            double[] mean2 = null;
            double cost = 0.0;
            for (int j = 0; j < dataPoints.length; j++) {
                accumulated.remove(originalIndices[j]);
                if (j <= cutIndex) {
                    if (mean1 == null || getDistance(dataPoints[originalIndices[j]], mean1) < getDistance(dataPoints[originalIndices[j]], mean2)) {
                        currentBitSet.remove(originalIndices[j]);
                    }
                }
                else if (mean1 != null && getDistance(dataPoints[originalIndices[j]], mean1) < getDistance(dataPoints[originalIndices[j]], mean2)) {
                    currentBitSet.remove(originalIndices[j]);
                }
                if (mean1 != null) {
                    cost += Math.exp(-((1.0/range)*getDistance(dataPoints[originalIndices[j]], (currentBitSet.get(originalIndices[j]) ? mean1 : mean2))));
                }
                if (j > 0 && j % (a/precision) == 0) {
                    if (dataPoints.length - j <= (a/precision) - 1) {
                        break;
                    }
                    currentBitSet = new BitSet(dataPoints.length);
                    currentBitSet.unionWith(accumulated);
                    cuts.add(currentBitSet);
                    costs.add(cost);
                    cost = 0.0;
                    //Find where to put the cut.F
                    double maxRange = -1;
                    for (int k = j+1; k < j+a/precision-1; k++) {
                        if (copy[k+1][i] - copy[k][i] > maxRange) {
                            maxRange = copy[k+1][i] - copy[k][i];
                            cutIndex = k;
                        }
                    }
                    axisParallelCuts[i].add(dataPoints[originalIndices[cutIndex]][i]);
                    //Calculate means.
                    mean1 = new double[dataPoints[0].length];
                    mean2 = new double[dataPoints[0].length];
                    int n1 = 0;
                    int n2 = 0;
                    for (int k = j+1; k < j+a/precision-1; k++) {
                        for (int l = 0; l < dataPoints[originalIndices[k]].length; l++) {
                            if (k <= cutIndex) {
                                mean1[l] += dataPoints[originalIndices[k]][l];
                                if (l == 0) {
                                    n1++;
                                }
                            }
                            else if (k > cutIndex) {
                                mean2[l] += dataPoints[originalIndices[k]][l];
                                if (l == 0) {
                                    n2++;
                                }
                            }
                        }
                    }
                    for (int k = 0; k < mean1.length; k++) {
                        mean1[k] /= n1;
                        mean2[k] /= n2;
                    }
                }
            }
            costs.add(cost);
        }
        BitSet[] result = new BitSet[cuts.size()];
        for (int i = 0; i < cuts.size(); i++) {
            result[i] = cuts.get(i);
        }
        initialCuts = result;
        this.axisParallelCuts = new double[axisParallelCuts.length][];
        for (int i = 0; i < axisParallelCuts.length; i++) {
            this.axisParallelCuts[i] = new double[axisParallelCuts[i].size()];
            for (int j = 0; j < axisParallelCuts[i].size(); j++) {
                this.axisParallelCuts[i][j] = axisParallelCuts[i].get(j);
            }
        }
        cutCosts = new double[costs.size()];
        for (int i = 0; i < costs.size(); i++) {
            cutCosts[i] = costs.get(i);
        }
        cutsAreAxisParallel = false;
        return result;
    }

    public BitSet[] getInitialCuts() {
        int localK = 4;
        double range = getMaxRange();
        List<Double> costs = new ArrayList<>();
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            for (int j = 0; j < dataPoints[0].length; j++) {
                copy[i][j] = dataPoints[i][j];
            }
        }
        for (int i = 0; i < dataPoints[0].length; i++) {
            axisParallelCuts[i] = new ArrayList<>();
            mergeSort(copy, originalIndices, i, 0, dataPoints.length-1);
            BitSet currentBitSet = new BitSet(dataPoints.length);
            currentBitSet.setAll();
            cuts.add(currentBitSet);
            BitSet accumulated = new BitSet(dataPoints.length);
            accumulated.setAll();
            axisParallelCuts[i].add(dataPoints[originalIndices[0]][i]);
            int cutIndex = 0;
            KMeans kMeans = null;
            double cost = 0.0;
            int index = 0;
            for (int j = 0; j < dataPoints.length; j++) {
                accumulated.remove(originalIndices[j]);
                if (kMeans == null || kMeans.centroids[kMeans.y[index]][i] <= dataPoints[originalIndices[cutIndex]][i]) {
                    currentBitSet.remove(originalIndices[j]);
                }
                double pointCost = 0.0;
                if (kMeans != null) {
                    int count = 0;
                    for (int k = 0; k < kMeans.centroids.length; k++) {
                        if (kMeans.centroids[kMeans.y[index]][i] <= dataPoints[originalIndices[cutIndex]][i] != kMeans.centroids[k][i] <= dataPoints[originalIndices[cutIndex]][i]) {
                            pointCost += getDistance(dataPoints[originalIndices[j]], kMeans.centroids[k]);
                            count++;
                        }
                    }
                    cost += Math.exp(-((1.0/range)*(pointCost/count)));
                }
                index++;
                if (j > 0 && j % (a/precision) == 0) {
                    if (dataPoints.length - j <= (a/precision) + 1) {
                        break;
                    }
                    index = 0;
                    currentBitSet = new BitSet(dataPoints.length);
                    currentBitSet.unionWith(accumulated);
                    cuts.add(currentBitSet);
                    costs.add(cost);
                    cost = 0.0;
                    //Find where to put the cut.
                    double[][] localCopy = new double[(j+a/precision+1)-(j+1)][];
                    double maxRange = -1;
                    for (int k = j+1; k < j+a/precision+1; k++) {
                        localCopy[k-(j+1)] = dataPoints[originalIndices[k]];
                        if (copy[k+1][i] - copy[k][i] > maxRange) {
                            maxRange = copy[k+1][i] - copy[k][i];
                            cutIndex = k;
                        }
                    }
                    kMeans = PartitionClustering.run(1, () -> KMeans.fit(localCopy, localK));
                    axisParallelCuts[i].add(dataPoints[originalIndices[cutIndex]][i]);
                }
            }
            costs.add(cost);
        }
        BitSet[] result = new BitSet[cuts.size()];
        for (int i = 0; i < cuts.size(); i++) {
            result[i] = cuts.get(i);
        }
        initialCuts = result;
        this.axisParallelCuts = new double[axisParallelCuts.length][];
        for (int i = 0; i < axisParallelCuts.length; i++) {
            this.axisParallelCuts[i] = new double[axisParallelCuts[i].size()];
            for (int j = 0; j < axisParallelCuts[i].size(); j++) {
                this.axisParallelCuts[i][j] = axisParallelCuts[i].get(j);
            }
        }
        cutCosts = new double[costs.size()];
        for (int i = 0; i < costs.size(); i++) {
            cutCosts[i] = costs.get(i);
        }
        cutsAreAxisParallel = false;
        return result;
    }
}
