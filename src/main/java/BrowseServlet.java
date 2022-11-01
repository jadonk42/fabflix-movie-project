import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "BrowseMovieServlet", value = "/api/browse")
public class BrowseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String genreQuery = "SELECT DISTINCT name as genre_types FROM genres ORDER BY name ASC";

    private static final String movieAlphaNumericQuery = "SELECT DISTINCT LEFT(title, 1) as " +
            "movie_categories " +
            "FROM movies " +
            "WHERE LEFT(title, 1) REGEXP '^[A-Za-z0-9]+$' " +
            "ORDER BY movie_categories ASC";
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String getMethod = request.getParameter("method");
        if (getMethod.equals("getMovieGenres")) {
            getMovieGenres(request, response);
        }
        else if (getMethod.equals("getMovieCharacters")) {
            getMovieCharacters(request, response);
        }
        else {
            throw new IllegalArgumentException("'method' parameter required, must be 'getMovieGenres' or 'getMovieCharacters'");
        }
    }

    protected void getMovieGenres(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(genreQuery);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String movieGenres = rs.getString("genre_types");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonArray.add(jsonObject);
            }
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
    }

    protected void getMovieCharacters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(movieAlphaNumericQuery);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            JsonObject nonAlphaNumeric = new JsonObject();
            nonAlphaNumeric.addProperty("movie_characters", "*");

            while (rs.next()) {
                String movieCharacters = rs.getString("movie_categories");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_characters", movieCharacters);
                jsonArray.add(jsonObject);
            }
            jsonArray.add(nonAlphaNumeric);

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
    }
}
