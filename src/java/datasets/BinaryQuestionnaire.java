package datasets;

import util.BitSet;
import util.Util.Tuple;

import java.io.File;
import java.util.*;

public class BinaryQuestionnaire implements Dataset {
    public BitSet[] answers;

    private BitSet[] initialCuts;
    private int[] groundTruth;

    public BinaryQuestionnaire() {

    }

    public BinaryQuestionnaire(BitSet[] answers) {
        this.answers = answers;
    }

    public BinaryQuestionnaire(Tuple<BitSet[], int[]> answersWithGroundTruth) {
        answers = answersWithGroundTruth.x;
        groundTruth = answersWithGroundTruth.y;
    }

    public int[] getGroundTruth() {
        return groundTruth;
    }

    public void setA(int a) {
        //Does not need a. Do nothing.
    }

    private int getNumberOfParticipants() {
        return answers.length;
    }

    private int getNumberOfQuestions() {
        return answers[0].size();
    }

    public void loadAnswersFromFile(String fileName, int startRow, int endRow, int startColumn, int endColumn) {
        try {
            List<List<Boolean>> result = new ArrayList<>();
            Scanner fileScanner = new Scanner(new File("datasets/" + fileName));
            for (int i = 0; i < startRow; i++) {
                fileScanner.nextLine();
            }
            int line = startRow;
            while (fileScanner.hasNextLine() && (endRow < 0 || line <= endRow)) {
                result.add(new ArrayList<>());
                Scanner lineScanner = new Scanner(fileScanner.nextLine());
                lineScanner.useDelimiter(",");
                for (int i = 0; i < startColumn; i++) {
                    lineScanner.next();
                }
                int column = startColumn;
                while (lineScanner.hasNextInt() && (endColumn < 0 || column <= endColumn)) {
                    int nextInt = lineScanner.nextInt();
                    if (nextInt == 0) {
                        result.remove(result.size()-1);
                        break;
                    }
                    result.get(result.size()-1).add(nextInt == 1 ? false : true);
                    column++;
                }
                line++;
            }
            answers = new util.BitSet[result.size()];
            for (int i = 0; i < answers.length; i++) {
                answers[i] = new BitSet(result.get(0).size());
                if (result.get(i).size() != answers[i].size()) { //File not valid
                    throw new Exception();
                }
                for (int j = 0; j < answers[i].size(); j++) {
                    if (result.get(i).get(j)) {
                        answers[i].add(j);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getAnswer(int r, int c) {
        return answers[r].get(c);
    }

    public void print() {
        for (int i = 0; i < answers.length; i++) {
            for (int j = 0; j < answers[i].size(); j++) {
                System.out.print(answers[i].get(j) + " ");
            }
            System.out.println();
        }
        System.out.println(answers.length + " " + answers[0].size());
    }

    public BitSet[] getInitialCuts() {
        BitSet[] result = new BitSet[getNumberOfQuestions()];
        for (int i = 0; i < getNumberOfQuestions(); i++) {
            result[i] = new BitSet(getNumberOfParticipants());
            for (int j = 0; j < getNumberOfParticipants(); j++) {
                if (answers[j].get(i)) {
                    result[i].add(j);
                }
            }
        }
        initialCuts = result;
        return result;
    }

    public double[] getCutCosts() {
        double[] result = new double[getNumberOfQuestions()];
        for (int i = 0; i < getNumberOfQuestions(); i++) {
            long cost = 0;
            for (int j = 0; j < getNumberOfQuestions(); j++) {
                long intersection1 = BitSet.intersection(initialCuts[i], initialCuts[j], false, false); //Number of people who answered "true" on one side of cut.
                long intersection2 = BitSet.intersection(initialCuts[i], initialCuts[j], true, false); //Number of people who answered "true" on other side of cut.
                cost += intersection1*intersection2 + (initialCuts[i].count() - intersection1)*(initialCuts[i].size() - initialCuts[i].count() - intersection2);
            }
            long cutSize = getCutSize(i);
            if (cutSize == 0 || getNumberOfParticipants()-cutSize == 0) {
                cost = Integer.MAX_VALUE;
            }
            else {
                cost /= cutSize*(getNumberOfParticipants()-cutSize);
            }
            result[i] = (int)cost;
            initialCuts[i].cutCost = result[i];
        }
        return result;
    }

    private int calculateSimilarity(int p1, int p2) {
        return BitSet.XNor(answers[p1], answers[p2]);
    }

    //Returns the number of participants on one side of a cut.
    private int getCutSize(int cut) {
        return initialCuts[cut].count();
    }

    //Performs K-means clustering on a binary dataset
    public int[] kMeans() {
        int K = 2;  //Amount of clusters
        int[] resultingClustering = new int[getNumberOfParticipants()];   //The resulting cluster each participant is assigned to

        //Place centroids randomly
        Random r = new Random();
        BitSet[] centroids = new BitSet[K]; //K randomly generated participants used as centroids
        BitSet[] tempCentroids = new util.BitSet[K];
        for (int k = 0; k < K; k++) {
            centroids[k] = new BitSet(getNumberOfQuestions());
            tempCentroids[k] = new BitSet(getNumberOfQuestions());
            for (int i = 0; i < getNumberOfQuestions(); i++) {
                if (r.nextBoolean()) {
                    centroids[k].add(i);
                    tempCentroids[k].add(i);
                }
            }
        }

        while (true) {
            for (int i = 0; i < getNumberOfParticipants(); i++) {

                //Find nearest centroid
                int min = Integer.MAX_VALUE;
                int cluster = -1;
                for (int k = 0; k < K; k++) {
                    //Calculate distance between participant and centroids
                    int dist = getNumberOfQuestions() - BitSet.XNor(centroids[k], answers[i]);
                    if (dist < min) {
                        min = dist;
                        cluster = k;
                    }
                }
                //Assign participant to cluster
                resultingClustering[i] = cluster;
            }

            //Update centroid means
            for (int k = 0; k < K; k++) {
                for (int i = 0; i < getNumberOfQuestions(); i++) {
                    int trueCount = 0;
                    int count = 0;
                    for (int j = 0; j < getNumberOfParticipants(); j++) {
                        if (resultingClustering[j] == k) {
                            count++;
                            if (answers[j].get(i)) {
                                trueCount++;
                            }
                        }
                    }
                    if (trueCount >= count/2) {
                        centroids[k].add(i);
                    } else {
                        centroids[k].remove(i);
                    }
                }
            }

            //Break loop if centroids haven't moved; else update temporary centroids
            boolean b = true;
            for (int k = 0; k < K; k++) {
                for (int i = 0; i < getNumberOfQuestions(); i++) {
                    if (tempCentroids[k].get(i) != centroids[k].get(i)) {
                        b = false;
                        if (centroids[k].get(i)) {
                            tempCentroids[k].add(i);
                        } else {
                            tempCentroids[k].remove(i);
                        }
                    }
                }
            }

            if (b) {
                break;
            }
        }

        //Prints resulting centroids
        for (int k = 0; k < K; k++) {
            System.out.println("Centroid " + (k + 1) + ":");
            for (int i = 0; i < getNumberOfQuestions(); i++) {
                System.out.print(centroids[k].get(i) + ", ");
            }
            System.out.println();
        }

        return resultingClustering;
    }

}
