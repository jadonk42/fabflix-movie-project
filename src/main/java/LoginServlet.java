import com.google.gson.JsonObject;
import main.java.User;
import main.java.RecaptchaVerifyUtils;

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
import org.jasypt.util.password.StrongPasswordEncryptor;

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
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        // Set the response to be a JSON object
        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Recaptcha incorrect, please try again.");
            jsonObject.addProperty("status", "fail");

            out.write(jsonObject.toString());
            out.close();
            return;
        }
        // Establish connection with database and closes connection after being used
        try (Connection conn = dataSource.getConnection()) {

            /**
             * Two criteria for login:
             * 1.Username exists (for the correct message to show if not)
             * 2.Password matches password for the given username (both must exist).
             *
             * Perform both - one for employees and one for customers.
             */
            final String customerQuery = "SELECT EXISTS (SELECT c.email  FROM customers AS c " +
                    "WHERE c.email = ?) AS user_exists, " +
                    "c.password AS encrypted_pass FROM customers AS c " +
                    "WHERE c.email = ?";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(customerQuery);

            // Set the parameter represented by "?" in the query to the user and password
            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet rs = statement.executeQuery();

            // Create jsonObject to return
            JsonObject jsonObject = new JsonObject();
            String user_exists = "0";
            String encrypted_pass = " ";

            while(rs.next()){
                user_exists = rs.getString("user_exists");
                encrypted_pass = rs.getString("encrypted_pass");
            }
            boolean password_success = new StrongPasswordEncryptor().checkPassword(password, encrypted_pass);

            if (Integer.parseInt(user_exists) == 1 && password_success) {
                request.getSession().setAttribute("user",  new User(username, false));
                jsonObject.addProperty("status", "success");
            } else{
                jsonObject.addProperty("status", "fail");
                if (Integer.parseInt(user_exists) != 1) {
                    jsonObject.addProperty("message", "Username does not exist");
                } else {
                    jsonObject.addProperty("message", "Incorrect password");
                }
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
            System.out.println("exception thrown");
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