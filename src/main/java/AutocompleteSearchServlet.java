import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.java.LogTimesToFile;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import javax.sql.DataSource;
import java.net.HttpURLConnection;
import java.sql.*;

@WebServlet(name = "AutocompleteSearchServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteSearchServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;
    private long startDatabaseTime;
    private long endDatabaseTime;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long startSearchTime = System.nanoTime();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String full_text = request.getParameter("full_text");
        full_text = transformFullText(full_text);

        startDatabaseTime = System.nanoTime();
        try (out; Connection conn = dataSource.getConnection()) {
            PreparedStatement statement;
            statement = conn.prepareStatement(getQueryStatement(full_text));

            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            endDatabaseTime = System.nanoTime();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                jsonArray.add(getMoviesAsJson(rs));
            }

            System.out.println("done getting json");

            //JsonObject resultData = new JsonObject();
            //resultData.addProperty("suggestions", jsonArray);

            rs.close();
            statement.close();
            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());

            response.setStatus(HttpURLConnection.HTTP_OK);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            out.close();
        }
        long endSearchTime = System.nanoTime();
        long totalSearchTime = endSearchTime - startSearchTime;
        long totalDatabaseTime = endDatabaseTime - startDatabaseTime;
        LogTimesToFile.writeToFile(totalSearchTime, totalDatabaseTime);
    }


    private String getQueryStatement(String full_text) {
        String topMoviesResults = "SELECT DISTINCT(m.id), m.title, r.rating " +
                "FROM ratings as r RIGHT JOIN (movies as m, stars_in_movies as sm, stars as s)  ON (m.id = r.movieId)" +
                "WHERE m.id = sm.movieId AND s.id = sm.starId AND " +
                "MATCH m.title AGAINST ('" + full_text + "' IN BOOLEAN MODE) " +
                "ORDER BY r.rating LIMIT 10;";

        return topMoviesResults;
    }



    private String transformFullText(String full_text) {
        String[] arrayString = full_text.split("[, ]");
        String result = "";

        for (String token: arrayString) {
            if (token != "") {
                result += "+" + token + "*";
            }
        }
        return result;
    }

    private JsonObject getMoviesAsJson(ResultSet rs) throws SQLException {
        String movieId = rs.getString("m.id");
        String movieTitle = rs.getString("m.title");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", movieId);
        jsonObject.addProperty("value", movieTitle);

        return jsonObject;
    }
}


