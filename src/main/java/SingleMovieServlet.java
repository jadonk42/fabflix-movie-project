import com.google.gson.JsonObject;

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
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet{
    private static final long serialVersionUID = 2L;
    private static final String singleMovieQuery = "SELECT m.title, m.year, m.director, GROUP_CONCAT(DISTINCT g.name) " +
            "as movie_genres, GROUP_CONCAT(DISTINCT s.name ORDER BY s.id) as movie_starrings, " +
            "GROUP_CONCAT(DISTINCT s.id ORDER BY s.id) as movie_starring_ids, r.rating " +
            "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
            "stars_in_movies as sm " +
            "WHERE m.id = ? AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id " +
            "AND m.id = sm.movieId AND sm.starId = s.id " +
            "GROUP BY m.title, m.year, m.director, r.rating";

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
        response.setContentType("application/json");
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(singleMovieQuery);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            JsonObject jsonObject = new JsonObject();

            while (rs.next()) {
                String movieTitle = rs.getString("m.title");
                String movieYear = rs.getString("m.year");
                String movieDirector = rs.getString("m.director");
                String movieGenres = rs.getString("movie_genres");
                String movieStars = rs.getString("movie_starrings");
                String movieStarIds = rs.getString( "movie_starring_ids");
                String movieRating = rs.getString("r.rating");

                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonObject.addProperty("movie_stars", movieStars);
                jsonObject.addProperty("movie_star_ids", movieStarIds);
                jsonObject.addProperty("movie_rating", movieRating);
            }
            rs.close();
            statement.close();
            out.write(jsonObject.toString());
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