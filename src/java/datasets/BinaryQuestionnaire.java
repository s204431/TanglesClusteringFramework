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

public class BinaryQuestionnaire extends Dataset {

    //This class represents a binary questionnaire dataset.

    public static final String name = "Binary Questionnaire";
    public BitSet[] answers;

    public static final String initialCutsQuestionCuts = "Question cuts";
    public static final String costFunctionPairwiseDifference = "Pairwise difference";

    public BinaryQuestionnaire() {

    }

    //Constructor taking answers as a BitSet array.
    public BinaryQuestionnaire(BitSet[] answers) {
        this.answers = answers;
    }

    //Constructor taking answers as a BitSet array and ground truth as an integer array.
    public BinaryQuestionnaire(Tuple<BitSet[], int[]> answersWithGroundTruth) {
        answers = answersWithGroundTruth.x;
        groundTruth = answersWithGroundTruth.y;
    }

    //Returns the ground truth (returns null if there is no ground truth).
    @Override
    public int[] getGroundTruth() {
        return groundTruth;
    }

    //Sets the value of a (agreement parameter) if this type of dataset needs it.
    @Override
    public void setA(int a) {
        //Does not need a. Do nothing.
    }

    //Returns the number of participants of the questionnaire (the number of data points).
    private int getNumberOfParticipants() {
        return answers.length;
    }

    //Returns the number of questions of the questionnaire (the number of dimensions).
    private int getNumberOfQuestions() {
        return answers[0].size();
    }

    //Loads a binary questionnaire from a file and overrides this object.
    public void loadAnswersFromFile(String fileName, int startRow, int endRow, int startColumn, int endColumn) {
        try {
            List<List<Boolean>> result = new ArrayList<>();
            File file = new File(fileName);
            Scanner fileScanner = new Scanner(file);
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
                    result.get(result.size()-1).add(nextInt == 1);
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
            fileScanner.close();
            loadGroundTruth(file, startRow, endRow == -1 ? answers.length-1 : endRow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Returns the answer giving by participant r to question c.
    private boolean getAnswer(int r, int c) {
        return answers[r].get(c);
    }

    //Prints the data set (for debugging).
    public void print() {
        for (int i = 0; i < answers.length; i++) {
            for (int j = 0; j < answers[i].size(); j++) {
                System.out.print(answers[i].get(j) + " ");
            }
            System.out.println();
        }
        System.out.println(answers.length + " " + answers[0].size());
    }

    //Returns the names of the supported initial cut generators.
    @Override
    public String[] getInitialCutGenerators() {
        return new String[] {initialCutsQuestionCuts};
    }

    //Returns the names of the supported cost functions.
    @Override
    public String[] getCostFunctions() {
        return new String[] {costFunctionPairwiseDifference};
    }

    //Generates initial cuts for this dataset using the giving initial cut generator name and returns it as a BitSet array (this dataset type only has one initial cut generator).
    @Override
    public BitSet[] getInitialCuts(String initialCutGenerator) {
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

    //Generates costs for the initial cuts for this dataset using the giving cost function name and returns it as a double array (this dataset type only has one cost function).
    @Override
    public double[] getCutCosts(String costFunctionName) {
        double[] result = new double[getNumberOfQuestions()];
        for (int i = 0; i < getNumberOfQuestions(); i++) {
            double cost = 0;
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
            result[i] = cost;
        }
        return result;
    }

    //Returns the number of participants on one side of a cut.
    private int getCutSize(int cut) {
        return initialCuts[cut].count();
    }

    //Performs k-means clustering on a binary questionnaire
    public int[] kMeans(int k) {
        if (k < 2) {
            return new int[answers.length];
        }
        double[][] dataPoints = convertAnswersToDataPoints();
        KMeans clusters = PartitionClustering.run(1, () -> KMeans.fit(dataPoints, k));
        return clusters.y;
    }

    //Performs spectral clustering on a binary questionnaire
    public int[] spectralClustering(int k, double sigma) {
        if (k < 2) {
            return new int[answers.length];
        }
        double[][] dataPoints = convertAnswersToDataPoints();
        SpectralClustering clusters = SpectralClustering.fit(dataPoints, k, sigma);
        return clusters.y;
    }

    //Performs hierarchical clustering on a binary questionnaire
    public int[] hierarchicalClustering(int k) {
        if (k < 2) {
            return new int[answers.length];
        }
        double[][] dataPoints = convertAnswersToDataPoints();
        HierarchicalClustering clusters = HierarchicalClustering.fit(CompleteLinkage.of(dataPoints));
        return clusters.partition(k);
    }

    //Converts data set to a double 2D array where the first possible answer to a question is represented by a 0 and the second possible answer is represented by a 1.
    //This practically converts the dataset to a feature based dataset, so we can use the other clustering algorithms.
    private double[][] convertAnswersToDataPoints() {
        double[][] dataPoints = new double[answers.length][answers[0].size()];
        for (int i = 0; i < dataPoints.length; i++) {
            for (int j = 0; j < dataPoints[0].length; j++) {
                dataPoints[i][j] = answers[i].get(j) ? 1 : 0;
            }
        }
        return dataPoints;
    }

    //Returns the algorithms that we support for this type of dataset.
    @Override
    public String[] getSupportedAlgorithms() {
        return new String[] {Model.tangleName, Model.kMeansName, Model.spectralClusteringName, Model.linkageName};
    }

    //Saves the dataset to a file.
    @Override
    public void saveToFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < answers.length; i++) {
                for (int j = 0; j < answers[i].size(); j++) {
                    writer.write(""+(answers[i].get(j) ? 1 : 2));
                    if (j != answers[i].size()-1) {
                        writer.write(",");
                    }
                }
                if (i != answers.length-1) {
                    writer.write("\n");
                }
            }
            writer.close();
            saveGroundTruth(file);
        } catch (IOException ignored) {}
    }

    //Returns the name of this type of dataset.
    @Override
    public String getName() {
        return name;
    }

    //Checks if this object is equal to the given object.
    @Override
    public boolean equals(Object o) {
        return o instanceof BinaryQuestionnaire && Arrays.equals(((BinaryQuestionnaire) o).answers, answers);
    }

}
