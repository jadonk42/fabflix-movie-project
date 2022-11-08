package main.java;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public class MainParser {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException {
        ParseMovies pm = new ParseMovies();
        pm.runParser("mains243.xml");

        ParseStars ps = new ParseStars();
        ps.runParser("actors63.xml");

        ParseStarsInMovies psm = new ParseStarsInMovies();
        psm.runParser("casts124.xml");
    }
}
