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

@WebServlet(najme = "MoviesServlet", urlPatterns = "/movies")
public class MoviesServlet extends HttpServlet{
    private static final long serialVersionUID = 2L;

    // Create a database which is registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /**
         * TODO: Implement JDBC to retrieve movies and return in a json format.
         */

        // Set the response to be a JSON object
        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Establish connection with database and closes connection after being used
        try (Connection conn = dataSource.getConnection()) {

            // Construct a query with parameter based on ?
            // ? = parameter
            final String query = "SELECT";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // create a JSON array to store the results
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // Get the attributes from the results

                // Store the attributes into a JSON object
                JsonObject jsonObject = new JsonObject();

                // Add the JSON Object to the array
                jsonArray.add(jsonObject);
            }

            // close the connections
            rs.close();
            statement.close();

            // Creates a log to localhost
            request.getServletContext().log("getting " + jsonArray.size() + " results");

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