package main.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogTimesToFile {

    static String fileName;
    static int caseNumber;
    static File generateFile;

    static public void createFile() {
        caseNumber = 1;
        fileName = "timelogcase" + caseNumber + ".txt";

        if (Files.notExists(Path.of(fileName))) {
            try {
                generateFile = new File(fileName);
                generateFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Cannot create new file");
            }
        }

    }

    static public void writeToFile(long searchTime, long databaseTime) {
        createFile();
        try {
            FileWriter writeToFile = new FileWriter(generateFile, true);
            BufferedWriter writeNumbers = new BufferedWriter(writeToFile);
            writeNumbers.write(searchTime + " " + databaseTime);
            writeNumbers.newLine();
            writeNumbers.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot write to file");
        }
    }
}
