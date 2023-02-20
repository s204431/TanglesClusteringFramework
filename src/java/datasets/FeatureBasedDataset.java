package datasets;

import model.Model;
import smile.clustering.HierarchicalClustering;
import smile.clustering.KMeans;
import smile.clustering.PartitionClustering;
import smile.clustering.SpectralClustering;
import smile.clustering.linkage.CompleteLinkage;
import util.BitSet;
import util.Util.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FeatureBasedDataset extends Dataset {

    public static final String name = "Feature Based";
    public double[][] dataPoints;
    private int a;

    private static int precision = 1; //Determines the number of cuts generated.

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
        int[] hardClustering = new int[result[0].size()];
        double[][] softClustering = new double[result[0].size()][result.length];
        //BEGIN TEST
        /*int n = 1;
        for (int j = 0; j < result[n].size(); j++) {
            hardClustering[j] = result[n].get(j) ? 0 : 1;
            softClustering[j][0] = result[n].get(j) ? 1 : 0;
            softClustering[j][1] = result[n].get(j) ? 0 : 1;
        }
        new PlottingView().loadPointsWithClustering(dataPoints, hardClustering, softClustering);*/
        //END TEST
        return result;
    }

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
            BitSet accumulated = new BitSet(dataPoints.length);
            int cutIndex = 0;
            for (int j = 0; j < dataPoints.length-1; j++) {
                accumulated.add(originalIndices[j]);
                if (j <= cutIndex) {
                    currentBitSet.add(originalIndices[j]);
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
                }
            }
        }
        BitSet[] result = new BitSet[cuts.size()];
        for (int i = 0; i < cuts.size(); i++) {
            result[i] = cuts.get(i);
        }
        initialCuts = result;
        //BEGIN TEST
        /*int[] hardClustering = new int[result[0].size()];
        double[][] softClustering = new double[result[0].size()][result.length];
        int n = 6;
        for (int j = 0; j < result[n].size(); j++) {
            hardClustering[j] = result[n].get(j) ? 0 : 1;
            softClustering[j][0] = result[n].get(j) ? 1 : 0;
            softClustering[j][1] = result[n].get(j) ? 0 : 1;
        }
        new PlottingView().loadPointsWithClustering(dataPoints, hardClustering, softClustering);*/
        //END TEST
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
        return distanceToMeanCostFunction();
    }

    private double[] pairwiseDistanceCostFunction() {
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
            costs[i] = cost;///(initialCuts[i].count()*(initialCuts[i].size()-initialCuts[i].count()));
            //costs[i] = cost;
        }
        return costs;
    }

    private double[] distanceToMeanCostFunction() {
        double[] costs = new double[initialCuts.length];
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for (int i = 0; i < dataPoints.length; i++) {
            for (int j = 0; j < dataPoints[i].length; j++) {
                if (dataPoints[i][j] < minValue) {
                    minValue = dataPoints[i][j];
                }
                if (dataPoints[i][j] > maxValue) {
                    maxValue = dataPoints[i][j];
                }
            }
        }
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
                costs[i] += Math.exp(-(1.0/(maxValue-minValue))*getDistance(dataPoints[j], mean));//*otherSideSize;
            }
            //costs[i] /= initialCuts[i].count()*(initialCuts[i].size() - initialCuts[i].count());
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
            File file = new File("datasets/" + fileName);
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
        KMeans clusters = PartitionClustering.run(20, () -> KMeans.fit(dataPoints, k));
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

    /*
    //Performs K-means clustering on a feature based dataset
    public int[] kMeans(int clusters) {
        int[] resultingClustering = new int[dataPoints.length];
        int features = dataPoints[0].length;

        //Find min and max value for every feature of the dataset
        double[][] minMaxValues = new double[features][2];
        for (int i = 0; i < features; i++) {
            minMaxValues[i][0] = Double.MAX_VALUE;  //Min
            minMaxValues[i][1] = Double.MIN_VALUE;  //Max
            for (int j = 0; j < dataPoints.length; j++) {
                if (dataPoints[j][i] < minMaxValues[i][0]) {
                    minMaxValues[i][0] = dataPoints[j][i];
                }
                if (dataPoints[j][i] > minMaxValues[i][1]) {
                    minMaxValues[i][1] = dataPoints[j][i];
                }
            }
        }

        //Place centroids randomly
        Random r = new Random();
        double[][] centroids = new double[clusters][features];
        double[][] tempCentroids = new double[clusters][features];
        double[][] tempTempCentroids = new double[clusters][features];
        for (int i = 0; i < clusters; i++) {
            for (int j = 0; j < features; j++) {
                double value = r.nextDouble(minMaxValues[j][0], minMaxValues[j][1]);
                centroids[i][j] = value;
                tempCentroids[i][j] = value;
                tempTempCentroids[i][j] = value;
            }
        }

        int count = 0;
        boolean run = true;
        while (run) {
            count++;

            if (count > 200) {
                break;
            }

            //Find the nearest centroid for every participant
            for (int i = 0; i < dataPoints.length; i++) {
                double min = Double.MAX_VALUE;
                for (int j = 0; j < clusters; j++) {
                    double[] centroid = centroids[j];
                    double manhattanDistance = 0;
                    for (int k = 0; k < features; k++) {
                        manhattanDistance += Math.abs(centroid[k] - dataPoints[i][k]);
                    }
                    if (manhattanDistance < min) {
                        min = manhattanDistance;
                        resultingClustering[i] = j;
                    }
                }
            }

            //Calculate sums used to update centroid means
            double[][] sums = new double[clusters][features];
            int[] participantsInClusters = new int[clusters];
            for (int i = 0; i < dataPoints.length; i++) {
                int cluster = resultingClustering[i];
                for (int j = 0; j < features; j++) {
                    sums[cluster][j] += dataPoints[i][j];
                    participantsInClusters[cluster]++;
                }
            }

            //Update centroid + break loop if centroids haven't changed or if centroids keep moving back and forth; else update temporary centroids
            run = false;
            boolean brk = true;
            for (int i = 0; i < clusters; i++) {
                for (int j = 0; j < features; j++) {
                    //Update centroid means
                    if (participantsInClusters[i] != 0) {
                        centroids[i][j] = sums[i][j] / participantsInClusters[i];
                    }
                    //Check if centroids are moving back and forth
                    if (centroids[i][j] != tempTempCentroids[i][j]) {
                        brk = false;
                    }
                    tempTempCentroids[i][j] = tempCentroids[i][j];

                    //Check if centroids haven't changed
                    if (centroids[i][j] != tempCentroids[i][j]) {
                        tempCentroids[i][j] = centroids[i][j];
                        run = true;
                    }
                }
            }

            if (brk) {
                break;
            }
        }

        //Prints resulting centroids
        System.out.println("Resulting centroid means:");
        for (int i = 0; i < clusters; i++) {
            System.out.print("Centroid " + (i+1) + ": ");
            for (int j = 0; j < features; j++) {
                System.out.print(centroids[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        return resultingClustering;
    }
    */

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
        return new String[] {Model.tangleName, Model.kMeansName, Model.spectralClusteringName};
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
}
