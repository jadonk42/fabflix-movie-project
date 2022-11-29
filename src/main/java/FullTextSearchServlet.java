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

@WebServlet(name = "FullTextSearchServlet", urlPatterns = "/api/movies/fullTextSearch")
public class FullTextSearchServlet extends HttpServlet{
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
        HttpSession session = request.getSession(true);
        session.setAttribute("lastQueryString", request.getQueryString());
        System.out.println("JUST SAVED: " + (String)session.getAttribute("lastQueryString"));
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String full_text = request.getParameter("full_text");
        String sortBy = request.getParameter("sortBy");
        int limit = Integer.parseInt(request.getParameter("limit"));
        int page = Integer.parseInt(request.getParameter("page"));
        full_text = transformFullText(full_text);

        startDatabaseTime = System.nanoTime();
        try (out; Connection conn = dataSource.getConnection()) {
            PreparedStatement statement;
            if (sortBy.equals("ratingDesc") || sortBy.equals("ratingAsc")) {
                statement = conn.prepareStatement(getQueryStatementForMoviesByRating(full_text, sortBy, limit, page));
            }
            else if (sortBy.equals("alphaDesc") || sortBy.equals("alphaAsc")) {
                statement = conn.prepareStatement(getQueryStatementForMoviesByName(full_text, sortBy, limit, page));
            }
            else {
                long endSearchTime = System.nanoTime();
                endDatabaseTime = endSearchTime;
                long totalSearchTime = endSearchTime - startSearchTime;
                long totalDatabaseTime = endDatabaseTime - startDatabaseTime;
                LogTimesToFile.writeToFile(totalSearchTime, totalDatabaseTime);
                return;
            }

            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                jsonArray.add(getMoviesAsJson(rs));
            }

            System.out.println("done getting json");

            rs.close();
            statement.close();
            endDatabaseTime = System.nanoTime();
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


    private String getQueryStatementForMoviesByRating(String full_text, String sortBy, int limit, int page) {
        String mode;
        if (sortBy.equals("ratingDesc")) {
            mode = "desc";
        }
        else {
            mode = "asc";
        }
        int offset = (page-1)*limit;

        String getAllMoviesQuery =  "SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM (TopMovies as T, movies as m,  genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm) LEFT JOIN ratings as r ON (m.id = r.movieId) " +
                "WHERE T.movieId = m.id AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                "m.id = sm.movieId AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY r.rating " + mode;
        String topMoviesResults = "WITH TopMovies AS ( " +
                "SELECT DISTINCT(m.id) as movieId, m.title, r.rating " +
                "FROM ratings as r RIGHT JOIN (movies as m, stars_in_movies as sm, stars as s)  ON (m.id = r.movieId)" +
                "WHERE m.id = sm.movieId AND s.id = sm.starId AND " +
                "MATCH title AGAINST ('" + full_text + "' IN BOOLEAN MODE) " +
                "ORDER BY m.title " + mode + " " +
                "LIMIT " + limit + " " +
                "OFFSET " + offset + ") ";;;

        return topMoviesResults + "\n" + getAllMoviesQuery;
    }

    private String getQueryStatementForMoviesByName(String full_text, String sortBy, int limit, int page) {
        String mode;
        if (sortBy.equals("alphaDesc")) {
            mode = "desc";
        }
        else {
            mode = "asc";
        }
        int offset = (page-1)*limit;

        String getAllMoviesQuery =  "SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM (TopMovies as T, movies as m,  genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm) LEFT JOIN ratings as r ON (m.id = r.movieId) " +
                "WHERE T.movieId = m.id AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                "m.id = sm.movieId AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY m.title " + mode;
        String topMoviesResults = "WITH TopMovies AS ( " +
                "SELECT DISTINCT(m.id) as movieId, m.title " +
                "FROM movies as m, stars_in_movies as sm, stars as s " +
                "WHERE m.id = sm.movieId AND s.id = sm.starId AND " +
                "MATCH title AGAINST ('" + full_text + "' IN BOOLEAN MODE) " +
                "ORDER BY m.title " + mode + " " +
                "LIMIT " + limit + " " +
                "OFFSET " + offset + ") ";;


        return topMoviesResults + "\n" + getAllMoviesQuery;
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