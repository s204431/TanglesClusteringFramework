package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BinaryQuestionnaire implements Dataset {
    private boolean[][] answers;

    public int getNumberOfParticipants() {
        return answers.length;
    }

    public int getNumberOfQuestions() {
        return answers[0].length;
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
                    result.get(result.size()-1).add(lineScanner.nextInt() == 1 ? false : true);
                    column++;
                }
                line++;
            }
            answers = new boolean[result.size()][result.get(0).size()];
            for (int i = 0; i < answers.length; i++) {
                if (result.get(i).size() != answers[i].length) { //File not valid
                    throw new Exception();
                }
                for (int j = 0; j < answers[i].length; j++) {
                    answers[i][j] = result.get(i).get(j);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getAnswer(int r, int c) {
        return answers[r][c];
    }

    public void print() {
        for (int i = 0; i < answers.length; i++) {
            for (int j = 0; j < answers[i].length; j++) {
                System.out.print(answers[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println(answers.length + " " + answers[0].length);
    }

    public boolean[][] getInitialCuts() {
        boolean[][] result = new boolean[getNumberOfQuestions()][getNumberOfParticipants()];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = answers[j][i];
            }
        }
        return result;
    }

    public int[] getCutCosts() {
        int[] result = new int[getNumberOfQuestions()];
        for (int i = 0; i < getNumberOfQuestions(); i++) {
            for (int j = 0; j < getNumberOfParticipants(); j++) {
                if (answers[j][i]) {
                    continue; //Only look at "false" answers.
                }
                for (int k = 0; k < getNumberOfParticipants(); k++) {
                    if (!answers[k][i]) {
                        continue; //Only look at "true" answers.
                    }
                    result[i] += calculateSimilarity(j, k);
                }
            }
            int cutSize = getCutSize(i);
            result[i] /= cutSize*(getNumberOfParticipants()-cutSize);
        }
        return result;
    }

    private int calculateSimilarity(int p1, int p2) {
        int similarity = 0;
        for (int i = 0; i < getNumberOfQuestions(); i++) {
            if (answers[p1][i] == answers[p2][i]) {
                similarity++;
            }
        }
        return similarity;
    }

    //Returns the number of participants on one side of a cut.
    private int getCutSize(int cut) {
        int size = 0;
        for (int i = 0; i < getNumberOfParticipants(); i++) {
            if (!answers[i][cut]) {
                size++;
            }
        }
        return size;
    }
}
