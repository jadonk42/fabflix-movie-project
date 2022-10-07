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

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/single-movie")
public class SingleMovieServlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /**
         * TODO: Implement JDBC to retrieve movie information and return in a json format.
         */

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
            final String query = "SELECT";

            // Declare our statement
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