import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BinaryQuestionnaire {
    private boolean[][] answers;

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

}
