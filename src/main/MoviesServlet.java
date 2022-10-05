import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "MoviesServlet", urlPatterns = "/movies")
public class MoviesServlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /**
         * TODO: Implement JDBC to retrieve movies and return in a json format.
         */
    }
}