package datasets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.BitSet;
import util.Tuple;

public class DatasetGenerator {

    //This class contains methods used to generate random datasets of different types.

    //Generates a random BitSet of answers to a binary questionnaire and returns the result
    public static BitSet[] generateRandomBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions) {
        BitSet[] result = new BitSet[numberOfAnswers];
        Random r = new Random();
        for (int i = 0; i < numberOfAnswers; i++) {
            result[i] = new BitSet(numberOfQuestions);
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextBoolean()) {
                    result[i].add(j);
                }
            }
        }
        return result;
    }

    //Generates a biased BitSet of answers to a binary questionnaire and returns the result.
    public static Tuple<BitSet[], int[]> generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions, double distributionPercentage) {
        if (distributionPercentage < 0 || distributionPercentage > 1) {
            return null;
        }

        BitSet[] result = new BitSet[numberOfAnswers];
        int[] groundTruth = new int[numberOfAnswers];
        Random r = new Random();
        int nPartition = (int)(numberOfAnswers * distributionPercentage);

        //Cluster of mainly false answers
        for (int i = 0; i < nPartition; i++) {
            result[i] = new BitSet(numberOfQuestions);
            groundTruth[i] = 0;
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextInt(100) >= 90) {
                    result[i].add(j);
                }
            }
        }

        //Cluster of mainly true answers
        for (int i = nPartition; i < numberOfAnswers; i++) {
            result[i] = new BitSet(numberOfQuestions);
            groundTruth[i] = 1;
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextInt(100) < 90) {
                    result[i].add(j);
                }
            }
        }
        return new Tuple<>(result, groundTruth);
    }

    //Generates a biased BitSet of answers to a binary questionnaire and returns the result. Uses a distribution percentage of 0.5.
    public static Tuple<BitSet[], int[]> generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions) {
        return generateBiasedBinaryQuestionnaireAnswers(numberOfAnswers, numberOfQuestions, 0.5);
    }

    //Generates biased binary questionnaire answers with a specific number of clusters.
    public static Tuple<BitSet[], int[]> generateBiasedBinaryQuestionnaireAnswers(int numberOfAnswers, int numberOfQuestions, int numberOfClusters) {
        BitSet[] result = new BitSet[numberOfAnswers];
        int[] groundTruth = new int[numberOfAnswers];
        Random r = new Random();
        int index = 0;
        int extra = numberOfAnswers % numberOfClusters;
        for (int i = 0; i < numberOfClusters; i++) {
            BitSet center = new BitSet(numberOfQuestions);
            for (int j = 0; j < numberOfQuestions; j++) {
                if (r.nextBoolean()) {
                    center.add(j);
                }
            }
            for (int j = 0; j < numberOfAnswers/numberOfClusters+1; j++) {
                if (j == numberOfAnswers/numberOfClusters) {
                    if (extra == 0) {
                        continue;
                    }
                    extra--;
                }
                result[index] = new BitSet(numberOfQuestions);
                groundTruth[index] = i;
                for (int k = 0; k < numberOfQuestions; k++) {
                    result[index].setValue(k, center.get(k));
                    if (r.nextDouble() >= 0.9) {
                        result[index].flip(k);
                    }
                }
                index++;
            }
        }
        return new Tuple<>(result, groundTruth);
    }

    //Generates an array of points based on a Gaussian Mixture function and returns the result
    public static Tuple<double[][], int[]> generateGaussianMixturePoints(int numberOfPoints, int numberOfClusters) {
        double[][] result = new double[numberOfPoints][2];
        int[] groundTruth = new int[numberOfPoints];
        int K = numberOfClusters;
        Random r = new Random();

        //Generate means of clusters
        double[][] meanPoints = new double[K][2];
        double[][] stds = new double[K][2];
        double dist = numberOfPoints * 0.05 * numberOfClusters/1.1;
        double rad = Math.PI / K;
        for (int i = 0; i < K; i++) {
            meanPoints[i] = new double[] { dist * Math.cos(rad + 2*rad*i), dist * Math.sin(rad + 2*rad*i) };
            stds[i] = new double[] {numberOfPoints*0.02*(r.nextDouble(0.33, 3)), numberOfPoints*0.02*(r.nextDouble(0.33, 3))};
        }

        //Generate points around means
        for (int i = 0; i < numberOfPoints; i++) {
            int j = i % K;
            double meanX = meanPoints[j][0];
            double meanY = meanPoints[j][1];
            result[i] = new double[] { r.nextGaussian(meanX, stds[j][0]), r.nextGaussian(meanY, stds[j][1]) };
            groundTruth[i] = j;
        }

        return new Tuple<>(result, groundTruth);
    }

    //Generates a feature based dataset with a given amount of points, clusters and dimensions. Randomizes the standard deviations and centroids.
    //Makes sure that no clusters are placed too close to each other.
    public static Tuple<double[][], int[]> generateFeatureBasedDataPoints(int numOfPoints, int numOfClusters, int numOfFeatures) {
        double[][] result = new double[numOfPoints][numOfFeatures];
        int[] groundTruth = new int[numOfPoints];

        Random r = new Random();

        double maxDist = numOfPoints * 0.12 * numOfClusters;
        double maxStd = numOfPoints * 0.1;
        double minStd = numOfPoints * 0.01;

        //Generate means of clusters
        double[][] centroids = new double[numOfClusters][numOfFeatures];
        double[][] centroidStds = new double[numOfClusters][numOfFeatures];
        for (int i = 0; i < numOfClusters; i++) {
            for (int j = 0; j < numOfFeatures; j++) {
                //Generate until not too close to another cluster.
                boolean good = false;
                while (!good) {
                    good = true;
                    centroids[i][j] = r.nextDouble(-maxDist, maxDist);
                    centroidStds[i][j] = r.nextDouble(minStd, maxStd);
                    for (int k = 0; k < i; k++) {
                        if (Math.abs(centroids[k][j] - centroids[i][j]) < 1.75*(centroidStds[k][j] + centroidStds[i][j])) {
                            good = false;
                            break;
                        }
                    }
                }
            }
        }

        //Generate points around means
        for (int i = 0; i < numOfPoints; i++) {
            int centroid = i % numOfClusters;
            for (int j = 0; j < numOfFeatures; j++) {
                result[i][j] = r.nextGaussian(centroids[centroid][j], centroidStds[centroid][j]);
            }
            groundTruth[i] = centroid;
        }

        return new Tuple<>(result, groundTruth);
    }

    //Generates random graph dataset with a specific number of nodes and clusters.
    public static Tuple<int[][][], int[]> generateRandomGraph(int numOfNodes, int numOfClusters) {
        double averageEdgesToSameCluster = 5.0;
        double averageEdgesToOtherClusters = 0.2;
        double sameClusterProb = averageEdgesToSameCluster/(numOfNodes/numOfClusters);
        double differentClusterProb = averageEdgesToOtherClusters/(numOfNodes - numOfNodes/numOfClusters);

        int[][][] result = new int[numOfNodes][][];
        List<Integer> groundTruthList = new ArrayList<>(numOfNodes);
        boolean[] added = new boolean[numOfNodes];
        Random random = new Random();
        int[] namesMap = new int[numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            namesMap[i] = -1;
        }
        for (int i = 0; i < numOfNodes; i++) {
            int centroid = i/(numOfNodes/numOfClusters);
            List<int[]> edgesList = new ArrayList<>();
            if (!added[i] && i > 0 && (centroid != (i+1)/(numOfNodes/numOfClusters) || i+1 >= numOfNodes)) { //Insure all nodes have at least one edge.
                namesMap[i] = groundTruthList.size();
                groundTruthList.add(centroid);
                added[i] = true;
                int weight = (int)random.nextGaussian(5, 3);
                if (weight < 1) {
                    weight = 1;
                }
                edgesList.add(new int[] {namesMap[i-1], weight});
            }
            for (int j = i+1; j < numOfNodes; j++) {
                boolean forceAdd = !added[i] && ((centroid != (j+1)/(numOfNodes/numOfClusters)) || j+1 >= numOfNodes); //Insure all nodes have at least one edge.
                if (forceAdd || (centroid == j/(numOfNodes/numOfClusters) && random.nextDouble() <= sameClusterProb) || (centroid != j/(numOfNodes/numOfClusters) && random.nextDouble() <= differentClusterProb)) {
                    if (!added[i]) {
                        namesMap[i] = groundTruthList.size();
                        groundTruthList.add(centroid);
                        added[i] = true;
                    }
                    if (!added[j]) {
                        namesMap[j] = groundTruthList.size();
                        groundTruthList.add(j/(numOfNodes/numOfClusters));
                        added[j] = true;
                    }
                    int weight = (int)random.nextGaussian(centroid == j/(numOfNodes/numOfClusters) ? 5 : 10, 3);
                    if (weight < 1) {
                        weight = 1;
                    }
                    edgesList.add(new int[] {namesMap[j], weight});
                }
            }
            int[][] edges = new int[edgesList.size()][];
            for (int j = 0; j < edgesList.size(); j++) {
                edges[j] = edgesList.get(j);
            }
            result[namesMap[i]] = edges;
        }
        int[] groundTruth = new int[groundTruthList.size()];
        for (int i = 0; i < groundTruthList.size(); i++) {
            groundTruth[i] = groundTruthList.get(i);
        }
        return new Tuple<>(result, groundTruth);
    }

    //Generates two interleaving half circle clusters.
    public static Tuple<double[][], int[]> generateFixedMoonFeatureBasedDataPoints(int numOfPoints) {
        double[][] result = new double[numOfPoints][];
        int[] groundTruth = new int[numOfPoints];

        Random r = new Random();

        double[] center1 = new double[] {0.0, 0.0};
        double[] center2 = new double[] {100.0, 0.0};
        double radius = 100.0;
        double std = 10.0;
        int nSteps = 10;

        //First moon
        int moonSize = numOfPoints/2;
        int index = 0;
        for (double v = 0; v <= Math.PI; v += Math.PI/nSteps) {
            double[] centroid = new double[] {Math.cos(v)*radius + center1[0], Math.sin(v)*radius + center1[1]};
            for (int j = 0; v == Math.PI ? (index < moonSize) : (j < moonSize/nSteps); j++) {
                result[index] = new double[] {r.nextGaussian(centroid[0], std), r.nextGaussian(centroid[1], std)};
                groundTruth[index] = 0;
                index++;
            }
        }

        //Second moon
        index = moonSize;
        moonSize = numOfPoints/2 + numOfPoints%2;
        for (double v = Math.PI; v <= 2*Math.PI; v += Math.PI/nSteps) {
            double[] centroid = new double[] {Math.cos(v)*radius + center2[0], Math.sin(v)*radius + center2[1]};
            for (int j = 0; v == Math.PI ? (index < moonSize) : (j < moonSize/nSteps); j++) {
                result[index] = new double[] {r.nextGaussian(centroid[0], std), r.nextGaussian(centroid[1], std)};
                groundTruth[index] = 1;
                index++;
            }
        }
        return new Tuple<>(result, groundTruth);
    }

    //Generates half circle clusters with random positions, sizes and rotations.
    public static Tuple<double[][], int[]> generateMoonFeatureBasedDataPoints(int numOfPoints, int numOfClusters) {
        double[][] result = new double[numOfPoints][];
        int[] groundTruth = new int[numOfPoints];

        Random r = new Random();

        int nSteps = 10;

        for (int i = 0; i < numOfClusters; i++) {
            double[] center = new double[] {r.nextDouble(0, 10), r.nextDouble(0, 20)};
            double radius = r.nextDouble(0.5, 3.0);
            double std = radius/10;
            int moonSize = i == numOfClusters-1 ? numOfPoints/numOfClusters + numOfPoints%numOfClusters : numOfPoints/numOfClusters;
            int index = i*(numOfPoints/numOfClusters);
            double start = r.nextDouble(0, 2*Math.PI);
            for (double v = start; v <= start+Math.PI; v += Math.PI/nSteps) {
                double[] centroid = new double[] {Math.cos(v)*radius + center[0], Math.sin(v)*radius + center[1]};
                for (int j = 0; v == start+Math.PI ? (index < moonSize) : (j < moonSize/nSteps); j++) {
                    result[index] = new double[] {r.nextGaussian(centroid[0], std), r.nextGaussian(centroid[1], std)};
                    groundTruth[index] = i;
                    index++;
                }
            }
        }
        return new Tuple<>(result, groundTruth);
    }

}
