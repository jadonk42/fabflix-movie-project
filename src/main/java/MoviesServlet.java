import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import javax.sql.DataSource;
import java.sql.*;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    // Create a database which is registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set the response to be a JSON object
        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Establish connection with database and closes connection after being used
        try (Connection conn = dataSource.getConnection()) {

            // Construct a query to retrieve the 20 top rated movies
            final String query = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name SEPARATOR ','), ',', 3), " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name SEPARATOR ','), ',', 3), " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id SEPARATOR ','), ',', 3), r.rating " +
                    "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm " +
                    "WHERE m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND m.id = sm.movieId " +
                    "AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // create a JSON array to store the results
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // Get the attributes from the results
                String movieId = rs.getString("m.id");
                String movieTitle = rs.getString("m.title");
                String movieYear = rs.getString("m.year");
                String movieDirector = rs.getString("m.director");
                String movieGenres =
                        rs.getString("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name SEPARATOR ','), ',', 3)");
                String movieStars =
                        rs.getString("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name SEPARATOR ','), ',', 3)");
                String movieStarIds =
                        rs.getString("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id SEPARATOR ','), ',', 3)");
                String movieRating = rs.getString("r.rating");

                // Store the attributes into a JSON object
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonObject.addProperty("movie_stars", movieStars);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("movie_star_ids", movieStarIds);

                // Add the JSON Object to the array
                jsonArray.add(jsonObject);
            }

            // close the connections
            rs.close();
            statement.close();

            // Creates a log to localhost
            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());

            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log the error for debugging
            request.getServletContext().log("Error:", e);

            // Set Error code status to 500(Internal Server Error)
            response.setStatus(500);
        } finally {
            // close the connection
            out.close();
        }

    }
}