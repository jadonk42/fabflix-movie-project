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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet{
    private static final long serialVersionUID = 2L;

    // Creates a datasource from the web.xml file
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
            final String query = "SELECT s.name, s.birthYear, GROUP_CONCAT(m.title), GROUP_CONCAT(m.id)" +
                    "FROM stars AS s, movies AS m, stars_in_movies AS sm " +
                    "WHERE s.id = ? AND s.id = sm.starId AND sm.movieId = m.id";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set parameter represented by "?" in query to if we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // create a JSON object to store the results
            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {
                // Get the attributes from the results
                String starName = rs.getString("s.name");
                String starDob = rs.getString("s.birthYear");
                String movieTitles = rs.getString("GROUP_CONCAT(m.title)");
                String movieIds = rs.getString("GROUP_CONCAT(m.id)");

                // Store the attributes into a JSON object
                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_dob", starDob);
                jsonObject.addProperty("movie_titles", movieTitles);
                jsonObject.addProperty("movie_ids", movieIds);
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