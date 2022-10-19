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

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet{
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;
    private static final String getSingleStarQuery = "SELECT s.name, s.birthYear, " +
            "GROUP_CONCAT(m.title ORDER BY m.year DESC) as movie_years, " +
            "GROUP_CONCAT(m.id ORDER BY m.year DESC) as movie_ids " +
            "FROM stars AS s, movies AS m, stars_in_movies AS sm " +
            "WHERE s.id = ? AND s.id = sm.starId AND sm.movieId = m.id";

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
            PreparedStatement statement = conn.prepareStatement(getSingleStarQuery);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            while (rs.next()) {
                String starName = rs.getString("s.name");
                String starDob = rs.getString("s.birthYear");
                String movieTitles = rs.getString("movie_years");
                String movieIds = rs.getString("movie_ids");

                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_dob", starDob);
                jsonObject.addProperty("movie_titles", movieTitles);
                jsonObject.addProperty("movie_ids", movieIds);
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