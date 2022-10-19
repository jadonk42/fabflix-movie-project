import com.google.gson.JsonArray;
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
import java.sql.*;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private static final String getMoviesQuery = "SELECT m.id, m.title, m.year, m.director, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
            "r.rating " +
            "FROM Top20Movies as T, movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
            "stars_in_movies as sm " +
            "WHERE T.movieId = m.id AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
            "m.id = sm.movieId AND sm.starId = s.id " +
            "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
            "ORDER BY r.rating DESC " +
            "LIMIT 20";
    private static final String top20Movies = "WITH Top20Movies AS ( " +
            "SELECT movieId, rating " +
            "FROM ratings " +
            "ORDER BY rating DESC " +
            "LIMIT 20)";

    private static final String getTop20MoviesQuery = top20Movies + "\n" + getMoviesQuery;
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
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(getTop20MoviesQuery);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                jsonArray.add(getTop20MoviesAsJson(rs));
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

    private JsonObject getTop20MoviesAsJson(ResultSet rs) throws SQLException {
        String movieId = rs.getString("m.id");
        String movieTitle = rs.getString("m.title");
        String movieYear = rs.getString("m.year");
        String movieDirector = rs.getString("m.director");
        String movieGenres =
                rs.getString("movie_genres");
        String movieStars =
                rs.getString("movie_starrings");
        String movieStarIds =
                rs.getString("movie_starring_ids");
        String movieRating = rs.getString("r.rating");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("movie_id", movieId);
        jsonObject.addProperty("movie_title", movieTitle);
        jsonObject.addProperty("movie_year", movieYear);
        jsonObject.addProperty("movie_director", movieDirector);
        jsonObject.addProperty("movie_genres", movieGenres);
        jsonObject.addProperty("movie_stars", movieStars);
        jsonObject.addProperty("movie_rating", movieRating);
        jsonObject.addProperty("movie_star_ids", movieStarIds);

        return jsonObject;
    }
}