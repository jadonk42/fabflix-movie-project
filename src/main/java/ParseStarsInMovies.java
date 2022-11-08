package main.java;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

// THIS PARSER MUST BE RAN AFTER THE MOVIES AND STARS HAVE BEEN ADDED
public class ParseStarsInMovies extends DefaultHandler {

    HashMap<String, String> mapStarNameToStarId;

    Set<String> verifyMovieId;
    private String tempVal;
    List<StarsInMovies> allStarsInMovies;
    List<StarsInMovies> inconsistentStarsInMovies;
    private StarsInMovies tempStarsInMovie;

    public ParseStarsInMovies() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        mapStarNameToStarId = new HashMap<String, String>();
        verifyMovieId = new HashSet<String>();
        String loginUser = "CS122B";
        String loginPasswd = "FabFlix";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        String getStarNamesIds = "SELECT name, id FROM stars";
        PreparedStatement statement = connection.prepareStatement(getStarNamesIds);
        ResultSet r = statement.executeQuery();
        while (r.next()) {
          String starName = r.getString("name");
          String starId = r.getString("id");
          mapStarNameToStarId.put(starName, starId);
        }

        String getAllMovieIds = "SELECT id FROM movies";
        PreparedStatement allMovieIds = connection.prepareStatement(getAllMovieIds);
        ResultSet allMovies = allMovieIds.executeQuery();
        while (allMovies.next()) {
            String movieId = allMovies.getString("id");
            verifyMovieId.add(movieId);
        }

        allStarsInMovies = new ArrayList<StarsInMovies>();
        inconsistentStarsInMovies = new ArrayList<StarsInMovies>();
    }

    public void runParser(String XMLDoc) throws FileNotFoundException {
        parseXMLDocuments(XMLDoc);
        writeInconsistentStarsInMoviesToFile();
        insertStarsInMoviesIntoDatabase();
        printData();
    }

    public void writeInconsistentStarsInMoviesToFile() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("inconsistent_moviesInStars.txt");

        Iterator<StarsInMovies> it = inconsistentStarsInMovies.iterator();
        while (it.hasNext()) {
            writer.println(it.next().toString());
        }
    }



    private void parseXMLDocuments(String uri) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(uri, this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void printData() {

        System.out.println("Number of StarsInMovies Inserted: " + allStarsInMovies.size());
        System.out.println("Number of Inconsistent StarsInMovies: " + inconsistentStarsInMovies.size());

    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            tempStarsInMovie = new StarsInMovies();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            if (tempStarsInMovie.getStarName() == null || tempStarsInMovie.getMovieId() == null) {
                inconsistentStarsInMovies.add(tempStarsInMovie);
            }
            else if (tempStarsInMovie.getStarName().equalsIgnoreCase("") || tempStarsInMovie.getMovieId().equalsIgnoreCase("")) {
                inconsistentStarsInMovies.add(tempStarsInMovie);
            }
            else if (!verifyMovieId.contains(tempStarsInMovie.getMovieId())) {
                inconsistentStarsInMovies.add(tempStarsInMovie);
            }
            else if (!mapStarNameToStarId.containsKey(tempStarsInMovie.getStarName())) {
                inconsistentStarsInMovies.add(tempStarsInMovie);
            }
            else {
                allStarsInMovies.add(tempStarsInMovie);
            }

        } else if (qName.equalsIgnoreCase("f")) {
            tempStarsInMovie.setMovieId(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
            tempStarsInMovie.setStarName(tempVal);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void insertStarsInMoviesIntoDatabase() {
        String starsInMovies = "INSERT INTO stars_in_movies (starId, movieId) " +
                "VALUES(?, ?)";

        try {
            String loginUser = "CS122B";
            String loginPasswd = "FabFlix";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            PreparedStatement getStarsInMovies = connection.prepareStatement(starsInMovies);
            connection.setAutoCommit(false);

            for (StarsInMovies starInMovie: allStarsInMovies) {
                String movieId = starInMovie.getMovieId();
                String starName = starInMovie.getStarName();
                String starId = mapStarNameToStarId.get(starName);

                getStarsInMovies.setString(1, starId);
                getStarsInMovies.setString(2, movieId);
                getStarsInMovies.addBatch();
            }

            int[] updateStarsInMovieTable = getStarsInMovies.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
