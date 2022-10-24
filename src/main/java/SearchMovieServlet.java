import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

@WebServlet(name = "SearchMovieServlet", urlPatterns = "/api/movies/search")
public class SearchMovieServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //SAVE THE LAST SEEN MOVIE SEARCH IN THE SESSION
        HttpSession session = request.getSession(true);
        session.setAttribute("lastQueryString", request.getQueryString());
        System.out.println("JUST SAVED: " + (String)session.getAttribute("lastQueryString"));

        response.setContentType("application/json");
        System.out.println(request.getQueryString());
        String sortBy = request.getParameter("sortBy");
        String name = request.getParameter("name");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        int limit = Integer.parseInt(request.getParameter("limit"));
        int page = Integer.parseInt(request.getParameter("page"));
        System.out.println(limit);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement;
            if (sortBy.equals("ratingDesc") || sortBy.equals("ratingAsc")) {
                statement = conn.prepareStatement(getQueryStatementForMoviesByRating(sortBy, name, year, director, star, limit, page));
            }
            else if (sortBy.equals("alphaDesc") || sortBy.equals("alphaAsc")) {
                statement = conn.prepareStatement(getQueryStatementForMoviesByName(sortBy, name, year, director, star, limit, page));
            }
            else {
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

    private String getQueryStatementForMoviesByRating(String sortBy, String name, String year, String director, String star, int limit, int page) {
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
                "FROM TopMovies as T, movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm " +
                "WHERE T.movieId = m.id AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                "m.id = sm.movieId AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY r.rating " + mode;;
        String topMoviesResults = "WITH TopMovies AS ( " +
                "SELECT DISTINCT(m.id) as movieId " +
                "FROM ratings as r, movies as m, stars_in_movies as sm, stars as s " +
                "WHERE m.id = sm.movieId AND r.movieId = m.id AND s.id = sm.starId ";
        if (name != "" && name != null) {
            topMoviesResults += "AND (m.title = '" + name + "' OR m.title LIKE '" + name +  "%' OR m.title LIKE '" + "% " + name + "%') " ;
        }
        if (isNumeric(year)) {
            topMoviesResults += "AND m.year = " + year + " ";
        }
        if (director != "" && director != null) {
            topMoviesResults += "AND (m.director = '" + director + "' OR m.director LIKE '" + director +  "%' OR m.director LIKE '" + "% " + director + "%') " ;
        }
        if (star != "" && star != null) {
            topMoviesResults += "AND (s.name = '" + star + "' OR s.name LIKE '" + star +  "%' OR s.name LIKE '" + "% " + star + "%') " ;
        }

        topMoviesResults += "ORDER BY r.rating " + mode + " " +
                "LIMIT " + limit + " " +
                "OFFSET " + offset + ") ";

        return topMoviesResults + "\n" + getAllMoviesQuery;
    }

    private String getQueryStatementForMoviesByName(String sortBy, String name, String year, String director, String star, int limit, int page) {
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
                "FROM TopMovies as T, movies as m, ratings as r, genres as g, genres_in_movies as gm, stars as s, " +
                "stars_in_movies as sm " +
                "WHERE T.movieId = m.id AND m.id = r.movieId AND m.id = gm.movieId AND gm.genreId = g.id AND " +
                "m.id = sm.movieId AND sm.starId = s.id " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                "ORDER BY r.rating " + mode;
        String topMoviesResults = "WITH TopMovies AS ( " +
                "SELECT DISTINCT(m.id) as movieId " +
                "FROM movies as m, stars_in_movies as sm, stars as s " +
                "WHERE m.id = sm.movieId AND s.id = sm.starId ";
        if (name != "" && name != null) {
            topMoviesResults += "AND m.title LIKE '" + name +  "%' ";
        }
        if (isNumeric(year)) {
            topMoviesResults += "AND m.year = " + year + " ";
        }
        if (director != "" && director != null) {
            topMoviesResults += "AND (m.director LIKE '" + director +  "%' OR m.director LIKE '" + "% " + director + "%') " ;
        }
        if (star != "" && star != null) {
            topMoviesResults += "AND (s.name LIKE '" + star +  "%' OR s.name LIKE '" + "% " + star + "%') " ;
        }

        topMoviesResults += "ORDER BY m.title " + mode + " " +
                "LIMIT " + limit + " " +
                "OFFSET " + offset + ") ";

        return topMoviesResults + "\n" + getAllMoviesQuery;
    }

    private boolean isNumeric(String year) {
        try {
            Integer.parseInt(year);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }
}