package main.java;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



public class ParseStars extends DefaultHandler {

    HashMap<String, Integer> verifyStars;
    private String tempVal;
    List<Star> allStars;

    List<Star> inConsistentActors;
    private Star tempStar;

    private int currStarId;

    public ParseStars() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        verifyStars = new HashMap<String, Integer>();
        String loginUser = "CS122B";
        String loginPasswd = "FabFlix";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        PreparedStatement statement = connection.prepareStatement("select max(substring(id, 3)) as id from stars");
        ResultSet r = statement.executeQuery();
        r.next();

        currStarId = Integer.parseInt(r.getString("id"));

        String getAllStars = "SELECT name, birthYear FROM stars";
        PreparedStatement starsStatement = connection.prepareStatement(getAllStars);
        ResultSet starsInfo = starsStatement.executeQuery();
        while (starsInfo.next()) {
            String starName = starsInfo.getString("name");
            String birthYear = starsInfo.getString("birthYear");
            if (birthYear == null || birthYear.equals("")) {
                verifyStars.put(starName, 0);
            }
            else {
                int birth = Integer.parseInt(birthYear);
                verifyStars.put(starName, birth);
            }
        }

        allStars = new ArrayList<Star>();
        inConsistentActors = new ArrayList<Star>();
    }

    public void runParser(String XMLDoc) throws FileNotFoundException {
        parseXMLDocuments(XMLDoc);
        writeInconsistentStarsToFile();
        insertStarsIntoDatabase();
        printData();
    }

    public void writeInconsistentStarsToFile() throws FileNotFoundException{
        PrintWriter writer = new PrintWriter("inconsistent_stars.txt");

        Iterator<Star> it = inConsistentActors.iterator();
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
        System.out.println("Number of Stars Inserted: " + allStars.size());
        System.out.println("Number of Inconsistent Stars: " + inConsistentActors.size());
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            if (tempStar.getName() == null || tempStar.getName().equalsIgnoreCase("")) {
                inConsistentActors.add(tempStar);
            }
            else if (verifyStars.containsKey(tempStar.getName())) {
                if (verifyStars.get(tempStar.getName()) == tempStar.getBirthYear()) {
                    inConsistentActors.add(tempStar);
                }
            }
            else {
                ++currStarId;
                String actorId = "nm" + Integer.toString(currStarId);
                tempStar.setId(actorId);
                verifyStars.put(tempStar.getName(), tempStar.getBirthYear());
                allStars.add(tempStar);
            }

        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {

            try {
                int birthYear = Integer.parseInt(tempVal);
                tempStar.setBirthYear(birthYear);
            } catch (NumberFormatException e) {
                tempStar.setBirthYear(0);
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void insertStarsIntoDatabase() {
        String insertStars = "INSERT INTO stars (id, name, birthYear) " +
                "VALUES(?, ?, ?)";

        try {
            String loginUser = "CS122B";
            String loginPasswd = "FabFlix";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            PreparedStatement getStars = connection.prepareStatement(insertStars);
            connection.setAutoCommit(false);

            for (Star stars: allStars) {
                String starId = stars.getId();
                String name = stars.getName();
                int birthYear = stars.getBirthYear();

                getStars.setString(1, starId);
                getStars.setString(2, name);
                if (birthYear == 0) {
                    getStars.setNull(3, Types.INTEGER);
                }
                else {
                    getStars.setInt(3, birthYear);
                }
                getStars.addBatch();
            }

            int[] updateStarsTable = getStars.executeBatch();
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
