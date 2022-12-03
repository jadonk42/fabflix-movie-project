package main.java;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogTimesToFile {

    static String fileName;
    static int caseNumber;

    static PrintWriter writer;

    static public void createFile() {
        caseNumber = 1;
        fileName = "timelogcase" + caseNumber + ".txt";

        if (Files.notExists(Path.of(fileName))) {
            try {
                 FileWriter fileWriter = new FileWriter(fileName, true);
                 writer = new PrintWriter(fileWriter);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Cannot create new file");
            }
        }

    }

    static public void writeToFile(long searchTime, long databaseTime) {
        createFile();
        writer.println(searchTime + " " + databaseTime);
    }
}
