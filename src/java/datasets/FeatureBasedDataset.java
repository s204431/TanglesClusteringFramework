package datasets;

import model.Model;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.EnumOptions;
import smile.clustering.HierarchicalClustering;
import smile.clustering.KMeans;
import smile.clustering.PartitionClustering;
import smile.clustering.SpectralClustering;
import smile.clustering.linkage.CompleteLinkage;
import smile.stat.Hypothesis;
import util.BitSet;
import util.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FeatureBasedDataset extends Dataset {

    //This class represents a feature based dataset.

    public static final String name = "Feature Based";
    private static final int precision = 1; //Determines the number of cuts generated.
    public double[][] dataPoints;
    private int a;
    public double[][] axisParallelCuts; //Only used when cuts are axis parallel. Used for visualization.
    public boolean cutsAreAxisParallel = true;
    public double[] cutCosts; //Used for visualization.

    public static final String initialCutsRange = "Range";
    public static final String initialCutsSimple = "Simple";
    public static final String initialCutsLocalMeans = "Local means";
    public static final String initialCutsKMeansAdjust = "K-Means adjust";
    public static final String initialCutsTanglesAdjust = "Tangles adjust";

    public static final String costFunctionPairwiseDistance = "Pairwise distance";
    public static final String costFunctionPairwiseSquaredDistance = "Squared distances";
    public static final String costFunctionDistanceToMean = "Distance to mean";
    public static final String costFunctionLocalMeans = "Local means";
    public static final String costFunctionKMeansAdjust = "K-Means adjust";

    //Empty constructor.
    public FeatureBasedDataset() {

    }

    //Constructor taking data points as a double 2D array.
    public FeatureBasedDataset(double[][] dataPoints) {
        this.dataPoints = dataPoints;
    }

    //Constructor taking data points and a ground truth.
    public FeatureBasedDataset(Tuple<double[][], int[]> dataPointsWithGroundTruth) {
        dataPoints = dataPointsWithGroundTruth.x;
        groundTruth = dataPointsWithGroundTruth.y;
    }

    //Returns the ground truth (returns null if there is no ground truth).
    @Override
    public int[] getGroundTruth() {
        return groundTruth;
    }

    //Sets the value of a (agreement parameter).
    public void setA(int a) {
        this.a = a;
    }

    //Original initial cut generator using simple axis parallel cuts with specific amount of points between them.
    public BitSet[] getInitialCutsSimple() {
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            System.arraycopy(dataPoints[i], 0, copy[i], 0, dataPoints[0].length);
        }
        for (int i = 0; i < dataPoints[0].length; i++) {
            axisParallelCuts[i] = new ArrayList<>();
            mergeSort(copy, originalIndices, i, 0, dataPoints.length-1);
            //BitSet first = new BitSet(dataPoints.length);
            //first.add(originalIndices[0]);
            //cuts.add(first);
            BitSet currentBitSet = new BitSet(dataPoints.length);
            cuts.add(currentBitSet);
            axisParallelCuts[i].add(dataPoints[originalIndices[0]][i]);
            for (int j = 0; j < dataPoints.length-1; j++) {
                currentBitSet.add(originalIndices[j]);
                if (j > 0 && j % (a/precision) == 0) {
                    if (dataPoints.length - j <= (a/precision) - 1) {
                        break;
                    }
                    axisParallelCuts[i].add(dataPoints[originalIndices[j]][i]);
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
        this.axisParallelCuts = new double[axisParallelCuts.length][];
        for (int i = 0; i < axisParallelCuts.length; i++) {
            this.axisParallelCuts[i] = new double[axisParallelCuts[i].size()];
            for (int j = 0; j < axisParallelCuts[i].size(); j++) {
                this.axisParallelCuts[i][j] = axisParallelCuts[i].get(j);
            }
        }
        cutsAreAxisParallel = true;
        return result;
    }

    //Initial cut generator using axis parallel cuts. Has a number of intervals with the same amount of points in each.
    //Each interval has one cut and each cut is placed at the largest range between two points in the interval.
    public BitSet[] getInitialCutsRange() {
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            System.arraycopy(dataPoints[i], 0, copy[i], 0, dataPoints[0].length);
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
        cutsAreAxisParallel = true;
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

    //Merge part of the merge sort algorithm.
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

    //Returns the names of the supported initial cut generators.
    @Override
    public String[] getInitialCutGenerators() {
        return new String[] {initialCutsKMeansAdjust, initialCutsRange, initialCutsLocalMeans, initialCutsSimple, initialCutsTanglesAdjust};
    }

    //Returns the names of the supported cost functions.
    @Override
    public String[] getCostFunctions() {
        return new String[] {initialCutsKMeansAdjust, costFunctionDistanceToMean, costFunctionPairwiseDistance, costFunctionPairwiseSquaredDistance, costFunctionLocalMeans};
    }

    //Generates initial cuts for this dataset using the giving initial cut generator name and returns it as a BitSet array.
    @Override
    public BitSet[] getInitialCuts(String generatorName) {
        if (generatorName == null || generatorName.equals(initialCutsKMeansAdjust)) {
            return getInitialCutsKMeansAdjust();
        }
        else if (generatorName.equals(initialCutsRange)) {
            return getInitialCutsRange();
        }
        else if (generatorName.equals(initialCutsSimple)) {
            return getInitialCutsSimple();
        }
        else if (generatorName.equals(initialCutsLocalMeans)) {
            return getInitialCutsLocalMeans();
        }
        else if (generatorName.equals(initialCutsTanglesAdjust)) {
            return getInitialCutsTanglesAdjust();
        }
        return getInitialCutsKMeansAdjust();
    }

    //Generates costs for the initial cuts for this dataset using the giving cost function name and returns it as a double array.
    @Override
    public double[] getCutCosts(String costFunctionName) {
        if (costFunctionName == null || costFunctionName.equals(costFunctionKMeansAdjust)) {
            //Do nothing
        }
        else if (costFunctionName.equals(costFunctionDistanceToMean)) {
            cutCosts = distanceToMeanCostFunction();
        }
        else if (costFunctionName.equals(costFunctionPairwiseDistance)) {
            cutCosts = pairwiseDistanceCostFunction();
        }
        else if (costFunctionName.equals(costFunctionPairwiseSquaredDistance)) {
            cutCosts = pairwiseSquaredDistanceCostFunction();
        }
        else if (costFunctionName.equals(costFunctionLocalMeans)) {
            //Do nothing
        }
        else {
            cutCosts = distanceToMeanCostFunction();
        }
        return cutCosts;
    }

    //Pairwise distance cost function, which uses the sum of the pairwise distances of every pair on different sides of the cut.
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

    //Cost function using pairwise squared distance efficiently.
    private double[] pairwiseSquaredDistanceCostFunction() {
        double[] costs = new double[initialCuts.length];
        double maxRange = getMaxRange();
        for (int i = 0; i < initialCuts.length; i++) {
            double[] squaredSums1 = new double[dataPoints[0].length];
            double[] sums1 = new double[dataPoints[0].length];
            double[] squaredSums2 = new double[dataPoints[0].length];
            double[] sums2 = new double[dataPoints[0].length];
            for (int j = 0; j < dataPoints.length; j++) {
                for (int k = 0; k < dataPoints[j].length; k++) {
                    if (initialCuts[i].get(j)) {
                        sums1[k] += dataPoints[j][k];
                        squaredSums1[k] += dataPoints[j][k]*dataPoints[j][k];
                    }
                    else {
                        sums2[k] += dataPoints[j][k];
                        squaredSums2[k] += dataPoints[j][k]*dataPoints[j][k];
                    }
                }
            }
            for (int j = 0; j < sums1.length; j++) {
                costs[i] += -sums1[j]*sums2[j] + squaredSums1[j] + squaredSums2[j];
            }
            costs[i] *= -2.0;
        }
        cutCosts = costs;
        return costs;
    }

    //Distance to mean cost function, which uses the sum of the distance to the opposite side mean for every point (has linear time complexity).
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

    //Calculates the largest range in a dimension between two points.
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

    //Returns the euclidean distance between two points.
    private double getDistance(double[] point1, double[] point2) {
        double length = 0;
        for (int i = 0; i < point1.length; i++) {
            length += (point1[i]-point2[i])*(point1[i]-point2[i]);
        }
        return Math.sqrt(length);
    }

    //Loads the dataset from a file.
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
            fileScanner.close();
            loadGroundTruth(file, startRow, endRow == -1 ? dataPoints.length-1 : endRow);
            if (dataPoints.length == 0 || dataPoints[0].length == 0) {
                throw new RuntimeException("Failed to load dataset.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Prints the dataset (for debugging).
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

    //Returns the algorithms that we support for the type of dataset.
    @Override
    public String[] getSupportedAlgorithms() {
        return new String[] {Model.tangleName, Model.kMeansName, Model.spectralClusteringName, Model.linkageName};
    }

    //Saves the dataset to a file.
    @Override
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
        } catch (IOException ignored) {}
    }

    //Returns the name of the dataset type.
    @Override
    public String getName() {
        return name;
    }

    //Initial cut generator that uses axis parallel cuts and adjusts them using distances to local means in the interval on each side of the cut. Generates non axis parallel cuts.
    //This initial cut generator also has its own cost function built in.
    public BitSet[] getInitialCutsLocalMeans() {
        double range = getMaxRange();
        List<Double> costs = new ArrayList<>();
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            System.arraycopy(dataPoints[i], 0, copy[i], 0, dataPoints[0].length);
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
                            else {
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

    //Initial cut generator that uses axis parallel cuts and adjusts them using local clusters generated with K-Means in the interval. Generates non axis parallel cuts.
    public BitSet[] getInitialCutsKMeansAdjust() {
        int localK = 4;
        double range = getMaxRange();
        List<Double> costs = new ArrayList<>();
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            System.arraycopy(dataPoints[i], 0, copy[i], 0, dataPoints[0].length);
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
            int side1Count = 0;
            int side2Count = 0;
            for (int j = 0; j < dataPoints.length; j++) {
                accumulated.remove(originalIndices[j]);
                if (kMeans == null || kMeans.centroids[kMeans.y[index]][i] <= dataPoints[originalIndices[cutIndex]][i]) {
                    currentBitSet.remove(originalIndices[j]);
                    side1Count++;
                }
                else {
                    side2Count++;
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
                    if (count == 0) {
                        count = 1;
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
                    if (side1Count == 0 || side2Count == 0) {
                        cost = range*localK*a;
                    }
                    side1Count = side1Count == 0 ? 1 : side1Count;
                    side2Count = side2Count == 0 ? 1 : side2Count;
                    costs.add(cost/(side1Count*side2Count));
                    side1Count = 0;
                    side2Count = 0;
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

    //Similar to the K-Means adjust initial cut generator, but recursively uses clustering with tangles instead.
    public BitSet[] getInitialCutsTanglesAdjust() {
        int localK = 4;
        double range = getMaxRange();
        List<Double> costs = new ArrayList<>();
        List<BitSet> cuts = new ArrayList<>();
        List<Double>[] axisParallelCuts = new ArrayList[dataPoints[0].length]; //For visualization.
        double[][] copy = new double[dataPoints.length][dataPoints[0].length];
        int[] originalIndices = new int[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            originalIndices[i] = i;
            System.arraycopy(dataPoints[i], 0, copy[i], 0, dataPoints[0].length);
        }
        for (int i = 0; i < dataPoints[0].length; i++) {
            costs.add(Double.MAX_VALUE);
            axisParallelCuts[i] = new ArrayList<>();
            mergeSort(copy, originalIndices, i, 0, dataPoints.length-1);
            BitSet currentBitSet = new BitSet(dataPoints.length);
            currentBitSet.setAll();
            cuts.add(currentBitSet);
            BitSet accumulated = new BitSet(dataPoints.length);
            accumulated.setAll();
            axisParallelCuts[i].add(dataPoints[originalIndices[0]][i]);
            int cutIndex = 0;
            int[] localClustering = null;
            double[][] centroids = null;
            int index = 0;
            for (int j = 0; j < dataPoints.length; j++) {
                accumulated.remove(originalIndices[j]);
                if (localClustering == null || centroids[localClustering[index]][i] <= dataPoints[originalIndices[cutIndex]][i]) {
                    currentBitSet.remove(originalIndices[j]);
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
                    String initialCutFunction = localCopy.length <= localK*8 ? initialCutsRange : initialCutsTanglesAdjust;
                    int localA = (int)(localCopy.length/localK*(2.0/3.0)) <= 0 ? 1 : (int)(localCopy.length/localK*(2.0/3.0));
                    localClustering = new Model().generateClusters(new FeatureBasedDataset(localCopy), localA, -1, initialCutFunction, costFunctionDistanceToMean);
                    int max = Arrays.stream(localClustering).max().getAsInt();
                    centroids = new double[max+1][localCopy[0].length];
                    int[] count = new int[max+1];
                    for (int k = 0; k < localClustering.length; k++) {
                        for (int d = 0; d < localCopy[0].length; d++) {
                            centroids[localClustering[k]][d] += localCopy[k][d];
                            count[localClustering[k]]++;
                        }
                    }
                    for (int k = 0; k < centroids.length; k++) {
                        for (int d = 0; d < localCopy[0].length; d++) {
                            centroids[k][d] /= count[k];
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
        cutCosts = new double[costs.size()];
        for (int i = 0; i < costs.size(); i++) {
            cutCosts[i] = costs.get(i);
        }
        cutsAreAxisParallel = false;
        return result;
    }

    //Checks if this object is equal to the given object.
    @Override
    public boolean equals(Object o) {
        return o instanceof FeatureBasedDataset && Arrays.deepEquals(((FeatureBasedDataset) o).dataPoints, dataPoints);
    }
}
