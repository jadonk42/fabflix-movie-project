import com.google.gson.JsonObject;
import main.java.User;

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
import java.sql.*;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    // Create a database which is registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Set the response to be a JSON object
        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Establish connection with database and closes connection after being used
        try (Connection conn = dataSource.getConnection()) {
            final String query = "SELECT EXISTS (SELECT *  FROM customers AS c " +
                    "WHERE c.email = ?) AS user_exists, " +
                    "EXISTS (SELECT *  FROM customers AS c " +
                    "WHERE c.email = ? AND c.password = ?) AS password_correct";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the user and password
            statement.setString(1, username);
            statement.setString(2, username);
            statement.setString(3, password);

            ResultSet rs = statement.executeQuery();

            // Create jsonObject to return
            JsonObject jsonObject = new JsonObject();
            String user_exists = "0";
            String pass_correct = "0";

            while(rs.next()){
                user_exists = rs.getString("user_exists");
                pass_correct = rs.getString("password_correct");
            }

            if (Integer.parseInt(user_exists) == 1 && Integer.parseInt(pass_correct) == 1) {
                request.getSession().setAttribute("user",  new User(username));
                jsonObject.addProperty("status", "success");
            } else{
                jsonObject.addProperty("status", "fail");
            }
            // close the connections
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObject.toString());

            // Set response status to 200 (OK)
            response.setStatus(200);
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log the error for debugging
            request.getServletContext().log("Error here:", e);

            // Set Error code status to 500(Internal Server Error)
            response.setStatus(500);
        } finally {
            // close the connection
            out.close();
        }
    }
}