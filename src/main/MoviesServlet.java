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

@WebServlet(najme = "MoviesServlet", urlPatterns = "/movies")
public class MoviesServlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /**
         * TODO: Implement JDBC to retrieve movies and return in a json format.
         */
    }
}