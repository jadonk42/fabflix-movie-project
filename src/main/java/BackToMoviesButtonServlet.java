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

@WebServlet(name = "BackToMoviesButtonServlet", urlPatterns = "/api/backToMovies")
public class BackToMoviesButtonServlet extends HttpServlet{
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
        String lastQueryString = (String)session.getAttribute("lastQueryString");
        System.out.println("just grabbed: " + lastQueryString);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lastQueryString", lastQueryString);
        out.write(jsonObject.toString());
        response.setStatus(HttpURLConnection.HTTP_OK);
    }
}