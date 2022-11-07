package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "BrowseMoviesServlet", value = "/api/movies/browse")
public class BrowseMoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //SAVE THE LAST SEEN MOVIE SEARCH IN THE SESSION
        HttpSession session = request.getSession(true);
        session.setAttribute("lastQueryString", request.getQueryString());
        System.out.println("JUST SAVED: " + session.getAttribute("lastQueryString"));

        response.setContentType("application/json");
        String movieGenre = request.getParameter("genre");
        String movieTitle = request.getParameter("character");
        String sortBy = request.getParameter("sortBy");
        int limit = Integer.parseInt(request.getParameter("limit"));
        int page = Integer.parseInt(request.getParameter("page"));
        int offset = (page-1)*limit;

        if (movieTitle.equals("null")) {
            movieTitle = null;
        }
        if (movieGenre.equals("null")) {
            movieGenre = null;
        }
        PrintWriter out = response.getWriter();
        boolean orderByName;

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement;
            System.out.println(movieGenre);
            if ((movieGenre == null && movieTitle == null) || (movieGenre != null && movieTitle != null)) {
                throw new IllegalArgumentException("only 'genre' or 'character' must be specified");
            }
            else if (movieGenre != null && (sortBy.equals("ratingDesc") || sortBy.equals("ratingAsc"))) {
                String mode = (sortBy.equals("ratingDesc")) ? "DESC" : "ASC";
                orderByName = false;
                statement = getMovieGenrePrepStatement(conn, mode, movieGenre, limit, offset, orderByName);
            }
            else if (movieGenre != null) {
                String mode = (sortBy.equals("alphaDesc")) ? "DESC" : "ASC";
                orderByName = true;
                statement = getMovieGenrePrepStatement(conn, mode, movieGenre, limit, offset, orderByName);
            }
            else if (sortBy.equals("ratingDesc") || sortBy.equals("ratingAsc")) {
                String mode = (sortBy.equals("ratingDesc")) ? "DESC" : "ASC";
                orderByName = false;
                statement = getMovieCharPrepStatement(conn, mode, movieTitle, limit, offset, orderByName);
            }
            else {
                String mode = (sortBy.equals("alphaDesc")) ? "DESC" : "ASC";
                orderByName = true;
                statement = getMovieCharPrepStatement(conn, mode, movieTitle, limit, offset, orderByName);
            }

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                jsonArray.add(getMoviesAsJson(rs));
            }

            System.out.println("done getting json");
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

    private PreparedStatement getMovieGenrePrepStatement(Connection conn, String mode, String movieGenre,
                                                         int limit, int offset, boolean orderByName) throws SQLException {
        PreparedStatement movieGenreStatement;
        if (orderByName) {
            movieGenreStatement = conn.prepareStatement(getMoviesByGenreSortedByName(mode));
        }
        else {
            movieGenreStatement = conn.prepareStatement(getMoviesByGenreSortedByRating(mode));
        }
        movieGenreStatement.setString(1, movieGenre);
        movieGenreStatement.setInt(2, limit);
        movieGenreStatement.setInt(3, offset);

        return movieGenreStatement;
    }

    private PreparedStatement getMovieCharPrepStatement(Connection conn, String mode, String movieTitle,
                                                        int limit, int offset, boolean orderByName) throws SQLException {
        PreparedStatement movieCharStatement;
        if (movieTitle.equals("*")) {
            movieCharStatement = conn.prepareStatement(nonAlphaNumericCharacters(orderByName, mode));
            movieCharStatement.setInt(1, limit);
            movieCharStatement.setInt(2, offset);
        }
        else if (orderByName) {
            movieCharStatement = conn.prepareStatement(getMoviesByCharacterSortedByName(mode));
            movieCharStatement.setString(1, movieTitle);
            movieCharStatement.setInt(2, limit);
            movieCharStatement.setInt(3, offset);
        }
        else {
            movieCharStatement = conn.prepareStatement(getMoviesByCharacterSortedByRating(mode));
            movieCharStatement.setString(1, movieTitle);
            movieCharStatement.setInt(2, limit);
            movieCharStatement.setInt(3, offset);
        }
        return movieCharStatement;
    }

    private String getMoviesByGenreSortedByRating(String mode) {
        String moviesByGenre = "WITH MoviesByGenre AS ( " +
                "SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM (ratings as r) RIGHT JOIN (movies as m, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm) ON (m.id = r.movieId) " +
                "WHERE g.name = ? AND gm.genreId = g.id AND m.id = gm.movieId  " +
                "AND " +
                "sm.movieId = m.id AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY r.rating " + mode +
                " LIMIT ? " +
                "OFFSET ?) ";

        String getAllGenres = "SELECT mg.id, mg.title, mg.year, mg.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ','), ',', 3) " +
                "as movie_genres, mg.movie_starrings, mg.movie_starring_ids, mg.rating " +
                "FROM MoviesByGenre as mg, genres as g, genres_in_movies as gm " +
                "WHERE mg.id = gm.movieId AND gm.genreId = g.id " +
                "GROUP BY mg.id, mg.title, mg.year, mg.director, mg.rating " +
                "ORDER BY mg.rating " + mode;

        String genreQuery = moviesByGenre + "\n" + getAllGenres;

        return genreQuery;
    }

    private String getMoviesByCharacterSortedByRating(String mode) {
        String moviesByChar = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                    "r.rating " +
                    "FROM (ratings as r) RIGHT JOIN (movies as m, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm) ON (m.id = r.movieId) " +
                    "WHERE LEFT(m.title, 1) = ? AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                    "m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY r.rating " + mode +
                    " LIMIT ? " +
                    "OFFSET ?";
        return moviesByChar;
    }

    private String getMoviesByGenreSortedByName(String mode) {
        String moviesByGenre = "WITH MoviesByGenre AS ( SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM (ratings as r) RIGHT JOIN (movies as m, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm) ON (m.id = r.movieId) " +
                "WHERE g.name = ? AND gm.genreId = g.id AND m.id = gm.movieId " +
                "AND " +
                "sm.movieId = m.id AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY m.title " + mode +
                " LIMIT ? " +
                "OFFSET ?) ";

        String getAllGenres = "SELECT mg.id, mg.title, mg.year, mg.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ','), ',', 3) " +
                "as movie_genres, mg.movie_starrings, mg.movie_starring_ids, mg.rating " +
                "FROM MoviesByGenre as mg, genres as g, genres_in_movies as gm " +
                "WHERE mg.id = gm.movieId AND gm.genreId = g.id " +
                "GROUP BY mg.id, mg.title, mg.year, mg.director, mg.rating " +
                "ORDER BY mg.title " + mode;

        String genreQuery = moviesByGenre + "\n" + getAllGenres;
        return genreQuery;
    }

    private String getMoviesByCharacterSortedByName(String mode) {
        String moviesByChar = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                    "r.rating " +
                    "FROM (ratings as r) RIGHT JOIN (movies as m, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm) ON (m.id = r.movieId) " +
                    "WHERE LEFT(m.title, 1) = ? AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                    "m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY m.title " + mode +
                    " LIMIT ? " +
                    "OFFSET ?";
        return moviesByChar;
    }

    private String nonAlphaNumericCharacters(boolean orderFlag, String mode) {
        String movieNonAlphaQuery = "SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM (ratings as r) LEFT JOIN (movies as m, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm) ON (m.id = r.movieId) " +
                "WHERE m.title REGEXP '^[^0-9A-Za-z]' AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                "m.id = sm.movieId AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating ";

        String orderQuery = "";

        // determines how to order the query by name or by rating
        if (orderFlag) {
            orderQuery = "ORDER BY m.title " + mode +
                         " LIMIT ? " +
                         "OFFSET ?";
        }
        else {
            orderQuery = "ORDER BY r.rating " + mode +
                         " LIMIT ? " +
                         "OFFSET ? ";
        }

        String nonAlphaCharMovies = movieNonAlphaQuery + orderQuery;
        return nonAlphaCharMovies;
    }

    private JsonObject getMoviesAsJson(ResultSet rs) throws SQLException {
        String movieId = rs.getString("id");
        String movieTitle = rs.getString("title");
        String movieYear = rs.getString("year");
        String movieDirector = rs.getString("director");
        String movieGenres =
                rs.getString("movie_genres");
        String movieStars =
                rs.getString("movie_starrings");
        String movieStarIds =
                rs.getString("movie_starring_ids");
        String movieRating = rs.getString("rating");

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
