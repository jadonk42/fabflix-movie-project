package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import java.sql.SQLException;

@WebServlet(name = "BrowseMoviesServlet", value = "/api/browse-movies")
public class BrowseMoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    private static final String getMoviesByGenre = "SELECT m.id, m.title, m.year, m.director, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
            "r.rating " +
            "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
            "stars_in_movies as sm " +
            "WHERE g.name = ? AND gm.genreId = g.id AND m.id = gm.movieId AND r.movieId = m.id AND " +
            "sm.movieId = m.id AND sm.starId = s.id " +
            "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
            "ORDER BY m.title ASC " +
            "LIMIT 20";


    private static final String getMoviesByFirstCharacter = "SELECT m.id, m.title, m.year, m.director, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
            "r.rating " +
            "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
            "stars_in_movies as sm " +
            "WHERE LEFT(m.title, 1) = ? AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
            "m.id = sm.movieId AND sm.starId = s.id " +
            "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
            "ORDER BY m.title ASC " +
            "LIMIT 20";

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String movieGenre = request.getParameter("genre");
        String movieTitle = request.getParameter("character");

        if ((movieGenre == null && movieTitle == null) || (movieGenre != null && movieTitle != null)) {
            throw new IllegalArgumentException("only 'genre' or 'character' must be specified");
        }
        else if (movieGenre != null) {
            getMoviesByGenre(request, response, movieGenre);
        }
        else {
            getMoviesByCharacter(request, response, movieTitle);
        }
    }

    protected void getMoviesByGenre(HttpServletRequest request, HttpServletResponse response, String movieGenre)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(getMoviesByGenre);
            statement.setString(1, movieGenre);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                getMoviesAsJson(rs);
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

    protected void getMoviesByCharacter(HttpServletRequest request, HttpServletResponse response, String movieTitle)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(getMoviesByFirstCharacter);
            statement.setString(1, movieTitle);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                jsonArray.add(getMoviesAsJson(rs));
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
