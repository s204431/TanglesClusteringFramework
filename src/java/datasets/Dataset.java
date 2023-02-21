package datasets;

import util.BitSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class Dataset {
    public static final String[] supportedDatasetTypes = new String[] {FeatureBasedDataset.name, BinaryQuestionnaire.name};
    protected BitSet[] initialCuts;
    protected int[] groundTruth;

    public BitSet[] getInitialCuts() {return null;}

    public double[] getCutCosts() {return null;}

    public int[] getGroundTruth() {return null;}
    public void setA(int a) {}
    public int[] kMeans(int clusters) {return null;}
    public int[] spectralClustering(int clusters, double sigma) {return null;}
    public String getName() {return "Dataset";}
    public String[] getSupportedAlgorithms() {return null;}

    public boolean supportsAlgorithm(String algorithmName) {
        for (String string : getSupportedAlgorithms()) {
            if (string.equals(algorithmName)) {
                return true;
            }
        }
        return false;
    }

    public void saveToFile(File file) {}

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
