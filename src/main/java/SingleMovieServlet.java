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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet{
    private static final long serialVersionUID = 2L;

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
        System.out.println("DOING GET HERE WE GO");
        // Set the response to be a JSON object
        response.setContentType("application/json");

        // retrieves parameter id from url request
        String id = request.getParameter("id");

        // log message for debugging
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Establish connection with database and closes connection after being used
        try (Connection conn = dataSource.getConnection()) {

            // Construct a query with parameter based on ?
            // ? = parameter
            final String query = "SELECT m.title, m.year, m.director, GROUP_CONCAT(DISTINCT g.name), " +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY s.id), GROUP_CONCAT(DISTINCT s.id ORDER BY s.id), r.rating " +
                    "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm " +
                    "WHERE m.id = ? AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id " +
                    "AND m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.title, m.year, m.director, r.rating";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // Create jsonObject to return
            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {
                // Get the attributes from the results
                String movieTitle = rs.getString("m.title");
                String movieYear = rs.getString("m.year");
                String movieDirector = rs.getString("m.director");
                String movieGenres = rs.getString("GROUP_CONCAT(DISTINCT g.name)");
                String movieStars = rs.getString("GROUP_CONCAT(DISTINCT s.name ORDER BY s.id)");
                String movieStarIds = rs.getString( "GROUP_CONCAT(DISTINCT s.id ORDER BY s.id)");
                String movieRating = rs.getString("r.rating");

                // Store the attributes into a JSON object
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonObject.addProperty("movie_stars", movieStars);
                jsonObject.addProperty("movie_star_ids", movieStarIds);
                jsonObject.addProperty("movie_rating", movieRating);
            }

            // close the connections
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObject.toString());

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