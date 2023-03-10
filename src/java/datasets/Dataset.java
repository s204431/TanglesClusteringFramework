package datasets;

import model.Model;
import model.TangleClusterer;
import util.BitSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class Dataset {

    //This is an abstract superclass for datasets. Each type of dataset should extend this and override the relevant methods.

    //The names of all the dataset types that we support.
    public static final String[] supportedDatasetTypes = new String[] {FeatureBasedDataset.name, BinaryQuestionnaire.name, GraphDataset.name};

    public BitSet[] initialCuts;
    protected int[] groundTruth;

    //Generates initial cuts for this dataset using the giving initial cut generator name and returns it as a BitSet array.
    public BitSet[] getInitialCuts(String generatorName) {return null;}

    //Generates costs for the initial cuts for this dataset using the giving cost function name and returns it as a double array.
    public double[] getCutCosts(String costFunctionName) {return null;}

    //Returns the ground truth (returns null if there is no ground truth).
    public int[] getGroundTruth() {return null;}

    //Sets the value of a (agreement parameter) if the type of dataset needs it.
    public void setA(int a) {}

    //Performs k-means clustering (if supported).
    public int[] kMeans(int clusters) {return null;}

    //Performs spectral clustering (if supported).
    public int[] spectralClustering(int clusters, double sigma) {return null;}

    //Performs hierarchical clustering (if supported).
    public int[] hierarchicalClustering(int clusters) {return null;}

    //Returns the name of the dataset type.
    public String getName() {return "Dataset";}

    //Returns the algorithms that we support for the type of dataset.
    public String[] getSupportedAlgorithms() {return null;}

    //Returns the names of the supported initial cut generators.
    public String[] getInitialCutGenerators() {return null;}

    //Returns the names of the supported cost functions.
    public String[] getCostFunctions() {return null;}

    //Returns true if algorithm with name algorithmName is supported by the type of dataset. Returns false otherwise.
    public boolean supportsAlgorithm(String algorithmName) {
        for (String string : getSupportedAlgorithms()) {
            if (string.equals(algorithmName)) {
                return true;
            }
        }
        return false;
    }

    //Saves the dataset to a file.
    public void saveToFile(File file) {}

    //Loads the ground truth corresponding to a file that the dataset is stored in (if such a ground truth is stored).
    public void loadGroundTruth(File originalFile, int startRow, int endRow) {
        try {
            String newName = originalFile.getName().substring(0, originalFile.getName().length()-4)+".gt";
            File newFile = new File(originalFile.getParent()+"\\"+newName);
            if (newFile.exists()) {
                Scanner scanner = new Scanner(newFile);
                int numberOfLines = endRow - startRow + 1;
                for (int i = 0; i < startRow; i++) {
                    scanner.nextLine();
                }
                groundTruth = new int[numberOfLines];
                for (int i = 0; i < numberOfLines; i++) {
                    groundTruth[i] = scanner.nextInt();
                }
            }
        } catch(IOException e) {}
    }

    //Saves the ground truth of this dataset (if it has a ground truth).
    public void saveGroundTruth(File originalFile) {
        try {
            String newName = originalFile.getName().substring(0, originalFile.getName().length()-4)+".gt";
            File newFile = new File(originalFile.getParent()+"\\"+newName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            for (int i = 0; i < groundTruth.length; i++) {
                writer.write(""+groundTruth[i]);
                if (i != groundTruth.length-1) {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException e) {}
    }

}
