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


        response.setContentType("application/json");
        String movieGenre = request.getParameter("genre");
        String movieTitle = request.getParameter("character");
        String sortBy = request.getParameter("sortBy");
        int limit = Integer.parseInt(request.getParameter("limit"));
        int page = Integer.parseInt(request.getParameter("page"));

        if (movieTitle.equals("null")) {
            movieTitle = null;
        }
        if (movieGenre.equals("null")) {
            movieGenre = null;
        }
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement;
            System.out.println(movieGenre);
            if ((movieGenre == null && movieTitle ==null) || (movieGenre != null && movieTitle != null)) {
                throw new IllegalArgumentException("only 'genre' or 'character' must be specified");
            }
            else if (movieGenre != null && (sortBy.equals("ratingDesc") || sortBy.equals("ratingAsc"))) {
                statement = conn.prepareStatement(getMoviesByGenreSortedByRating(sortBy, movieGenre, limit, page));
            }
            else if (movieGenre != null) {
                statement = conn.prepareStatement(getMoviesByGenreSortedByName(sortBy, movieGenre, limit, page));
            }
            else if (sortBy.equals("ratingDesc") || sortBy.equals("ratingAsc")) {
                statement = conn.prepareStatement(getMoviesByCharacterSortedByRating(sortBy, movieTitle, limit, page));
            }
            else {
                statement = conn.prepareStatement(getMoviesByCharacterSortedByName(sortBy, movieTitle, limit, page));
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

    private String getMoviesByGenreSortedByRating(String sortBy, String movieGenre, int limit, int page) {
        String mode = (sortBy.equals("ratingDesc")) ? "DESC" : "ASC";
        int offset = (page-1)*limit;

        String moviesByGenre = "WITH MoviesByGenre AS ( " +
                "SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm " +
                "WHERE g.name = '" + movieGenre + "' AND gm.genreId = g.id AND m.id = gm.movieId AND r.movieId = m.id " +
                "AND " +
                "sm.movieId = m.id AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY r.rating " + mode + " " +
                "LIMIT " + limit + " " +
                "OFFSET " + offset + ") ";

        String getAllGenres = "SELECT mg.id, mg.title, mg.year, mg.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ','), ',', 3) " +
                "as movie_genres, mg.movie_starrings, mg.movie_starring_ids, mg.rating " +
                "FROM MoviesByGenre as mg, genres as g, genres_in_movies as gm " +
                "WHERE mg.id = gm.movieId AND gm.genreId = g.id " +
                "GROUP BY mg.id, mg.title, mg.year, mg.director, mg.rating " +
                "ORDER BY mg.rating " + mode + " ";

        String genreQuery = moviesByGenre + "\n" + getAllGenres;

        return genreQuery;
    }

    private String getMoviesByCharacterSortedByRating(String sortBy, String movieCharacter, int limit, int page) {
        String mode = (sortBy.equals("ratingDesc")) ? "DESC" : "ASC";
        String moviesByChar = "";
        int offset = (page-1)*limit;
        if (movieCharacter.equals("*")) {
            moviesByChar = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                    "r.rating " +
                    "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm " +
                    "WHERE m.title REGEXP '^[^0-9A-Za-z]' AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                    "m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY r.rating " + mode + " " +
                    "LIMIT " + limit + " " +
                    "OFFSET " + offset + "";
        }
        else {
            moviesByChar = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                    "r.rating " +
                    "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm " +
                    "WHERE LEFT(m.title, 1) = '" + movieCharacter + "' AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                    "m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY r.rating " + mode + " " +
                    "LIMIT " + limit + " " +
                    "OFFSET " + offset + "";
        }
        return moviesByChar;
    }

    private String getMoviesByGenreSortedByName(String sortBy, String movieGenre, int limit, int page) {
        String mode = (sortBy.equals("alphaDesc")) ? "DESC" : "ASC";
        int offset = (page-1)*limit;

        String moviesByGenre = "WITH MoviesByGenre AS ( SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                "r.rating " +
                "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm " +
                "WHERE g.name = '" + movieGenre + "' AND gm.genreId = g.id AND m.id = gm.movieId AND r.movieId = m.id " +
                "AND " +
                "sm.movieId = m.id AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY m.title " + mode + " " +
                "LIMIT " + limit + " " +
                "OFFSET " + offset + ") ";

        String getAllGenres = "SELECT mg.id, mg.title, mg.year, mg.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ','), ',', 3) " +
                "as movie_genres, mg.movie_starrings, mg.movie_starring_ids, mg.rating " +
                "FROM MoviesByGenre as mg, genres as g, genres_in_movies as gm " +
                "WHERE mg.id = gm.movieId AND gm.genreId = g.id " +
                "GROUP BY mg.id, mg.title, mg.year, mg.director, mg.rating " +
                "ORDER BY mg.rating " + mode + " ";;

        String genreQuery = moviesByGenre + "\n" + getAllGenres;

        return genreQuery;
    }

    private String getMoviesByCharacterSortedByName(String sortBy, String movieCharacter, int limit, int page) {
        String mode = (sortBy.equals("alphaDesc")) ? "DESC" : "ASC";
        int offset = (page-1)*limit;

        String moviesByChar = "";
        if (movieCharacter.equals("*")) {
            moviesByChar = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                    "r.rating " +
                    "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm " +
                    "WHERE m.title REGEXP '^[^0-9A-Za-z]' AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                    "m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY m.title " + mode + " " +
                    "LIMIT " + limit + " " +
                    "OFFSET " + offset;
        }
        else {
            moviesByChar = "SELECT m.id, m.title, m.year, m.director, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ','), ',', 3) as movie_genres, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starrings, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.id SEPARATOR ','), ',', 3) as movie_starring_ids, " +
                    "r.rating " +
                    "FROM movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                    "stars_in_movies as sm " +
                    "WHERE LEFT(m.title, 1) = '" + movieCharacter + "' AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                    "m.id = sm.movieId AND sm.starId = s.id " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY m.title " + mode + " " +
                    "LIMIT " + limit + " " +
                    "OFFSET " + offset;
        }
        return moviesByChar;
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
