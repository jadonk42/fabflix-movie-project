package main.java;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class ParseMovies extends DefaultHandler {

    HashMap<String, Integer> verifyMovies;

    Set<String> verifyMovieId;

    HashMap<String, Integer> genreNameToGenreId;

    Set<String> verifyGenres;

    private String tempVal;
    List<Movie> allMovies;
    List<GenresInMovies> allGenreInMovies;
    List<Movie> inconsistentMovies;
    List<Genre> inconsistentGenres;
    private Movie tempMovie;

    List<Genre> allGenres;

    private Genre tempGenre;

    private GenresInMovies tempGenreInMovie;

    int maxId;

    public ParseMovies() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        verifyMovies = new HashMap<String, Integer>();
        verifyGenres = new HashSet<String>();
        verifyMovieId = new HashSet<String>();
        genreNameToGenreId = new HashMap<String, Integer>();
        String loginUser = "CS122B";
        String loginPasswd = "FabFlix";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";


        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        String getAllMovies = "SELECT id, title, year FROM movies";

        PreparedStatement statement = connection.prepareStatement(getAllMovies);
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            String movieId = r.getString("id");
            String movieName = r.getString("title");
            String movieYear = r.getString("year");
            int year = Integer.parseInt(movieYear);
            verifyMovies.put(movieName, year);
            verifyMovieId.add(movieId);
        }

        String getAllGenres = "SElECT id, name FROM genres";
        PreparedStatement getGenres = connection.prepareStatement(getAllGenres);
        ResultSet rs = getGenres.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            String genre = rs.getString("name");
            verifyGenres.add(genre);
            genreNameToGenreId.put(genre, id);
        }

        String genreMaxId = "SELECT MAX(id) as id FROM genres";

        PreparedStatement getMaxId = connection.prepareStatement(genreMaxId);
        ResultSet maxGenreId = getMaxId.executeQuery();
        maxGenreId.next();

        maxId = Integer.parseInt(maxGenreId.getString("id"));
        allMovies = new ArrayList<Movie>();
        allGenres = new ArrayList<Genre>();
        inconsistentMovies = new ArrayList<Movie>();
        inconsistentGenres = new ArrayList<Genre>();
        allGenreInMovies = new ArrayList<GenresInMovies>();
    }

    public void runParser(String XMLDoc) throws FileNotFoundException {
        parseXMLDocuments(XMLDoc);
        writeInconsistentMoviesToFile();
        writeInconsistentGenresToFile();
        insertMoviesIntoDatabase();
        insertGenresIntoDatabase();
        insertGenresInMoviesToDatabase();
        printData();
    }

    public void writeInconsistentMoviesToFile() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("inconsistent_movies.txt");

        Iterator<Movie> it = inconsistentMovies.iterator();
        while (it.hasNext()) {
            writer.println(it.next().toString());
        }
    }

    public void writeInconsistentGenresToFile() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("inconsistent_genres.txt");

        Iterator<Genre> it = inconsistentGenres.iterator();
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
        System.out.println("Number of Movies Inserted: " + allMovies.size());
        System.out.println("Number of Inconsistent Movies: " + inconsistentMovies.size());
        System.out.println("Number of Genres Inserted: " + allGenres.size());
        System.out.println("Number of Inconsistent Genres: " + inconsistentGenres.size());
        System.out.println("Number of Genres In Movies Inserted: " + allGenreInMovies.size());

    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
            tempGenre = new Genre();
            tempGenreInMovie = new GenresInMovies();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            if (tempMovie.getYear() == 0) {
                inconsistentMovies.add(tempMovie);
            }
            else if (tempMovie.getTitle() == null || tempMovie.getTitle().equalsIgnoreCase("")) {
                inconsistentMovies.add(tempMovie);
            }
            else if (tempMovie.getDirector() == null || tempMovie.getDirector().equalsIgnoreCase("")) {
                inconsistentMovies.add(tempMovie);
            }
            else if (tempGenre.getName() == null || tempGenre.getName().equalsIgnoreCase("")) {
                inconsistentMovies.add(tempMovie);
            }
            else if (tempMovie.getId() == null || tempMovie.getId().equalsIgnoreCase("")) {
                inconsistentMovies.add(tempMovie);
            }
            else if (verifyMovieId.contains(tempMovie.getId())) {
                inconsistentMovies.add(tempMovie);
            }
            else if (verifyMovies.containsKey(tempMovie.getTitle())) {
                if (verifyMovies.get(tempMovie.getTitle()) == tempMovie.getYear()) {
                    inconsistentMovies.add(tempMovie);
                }
            }
            else if (!inconsistentMovies.contains(tempMovie)) {
                verifyMovies.put(tempMovie.getTitle(), tempMovie.getYear());
                tempGenreInMovie.setMovieId(tempMovie.getId());
                tempGenreInMovie.setGenreName(tempGenre.getName());
                allGenreInMovies.add(tempGenreInMovie);
                verifyMovieId.add(tempMovie.getId());
                allMovies.add(tempMovie);
            }

            if (tempGenre.getName() == null || tempGenre.getName().equalsIgnoreCase("")) {
                inconsistentGenres.add(tempGenre);
            }
            else if (verifyGenres.contains(tempGenre.getName())) {
                inconsistentGenres.add(tempGenre);
            }
            else if (!inconsistentGenres.contains(tempGenre)) {
                ++maxId;
                tempGenre.setId(maxId);
                verifyGenres.add(tempGenre.getName());
                allGenres.add(tempGenre);
            }

        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                int movieYear = Integer.parseInt(tempVal);
                tempMovie.setYear(movieYear);
            } catch (NumberFormatException e) {
                tempMovie.setYear(0);
            }
        } else if (qName.equalsIgnoreCase("dirn")) {
            tempMovie.setDirector(tempVal);
        } else if (qName.equalsIgnoreCase("cat")) {
            tempGenre.setName(tempVal);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void insertMoviesIntoDatabase() {
        String insertMovies = "INSERT INTO movies (id, title, year, director) " +
                "VALUES(?, ?, ?, ?)";

        try {
            String loginUser = "CS122B";
            String loginPasswd = "FabFlix";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            PreparedStatement getMovies = connection.prepareStatement(insertMovies);
            connection.setAutoCommit(false);

            for (Movie currMovie: allMovies) {
                String id = currMovie.getId();
                String title = currMovie.getTitle();
                int year = currMovie.getYear();
                String director = currMovie.getDirector();

                getMovies.setString(1, id);
                getMovies.setString(2, title);
                getMovies.setInt(3, year);
                getMovies.setString(4, director);
                getMovies.addBatch();
            }

            int[] updateMoviesTable = getMovies.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertGenresIntoDatabase() {
        String genreQuery = "INSERT INTO genres (id, name) " +
                "VALUES(?, ?)";

        try {
            String loginUser = "CS122B";
            String loginPasswd = "FabFlix";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            PreparedStatement getGenres = connection.prepareStatement(genreQuery);
            connection.setAutoCommit(false);

            for (Genre genre: allGenres) {
                int id = genre.getId();
                String name = genre.getName();

                getGenres.setInt(1, id);
                getGenres.setString(2, name);
                getGenres.addBatch();
            }

            int[] updateGenreTable = getGenres.executeBatch();
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

    public void insertGenresInMoviesToDatabase()  {
        String genreInMovie = "INSERT INTO genres_in_movies (genreId, movieId) " +
                "VALUES(?, ?)";

        try {
            String loginUser = "CS122B";
            String loginPasswd = "FabFlix";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            PreparedStatement getGenresToMovies = connection.prepareStatement(genreInMovie);
            connection.setAutoCommit(false);

            for (GenresInMovies insertGenreToMovie: allGenreInMovies) {
                String movieId = insertGenreToMovie.getMovieId();
                String genreName = insertGenreToMovie.getGenreName();
                int genreId = genreNameToGenreId.get(genreName);

                getGenresToMovies.setInt(1, genreId);
                getGenresToMovies.setString(2, movieId);
                getGenresToMovies.addBatch();
            }

            int[] updateGenreToMovieTable = getGenresToMovies.executeBatch();
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
