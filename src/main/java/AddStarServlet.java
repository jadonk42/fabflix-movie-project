package main.java;

import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "MoviePaymentServlet", value = "/api/movie-payment")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String starName = request.getParameter("starName");
        String lastName = request.getParameter("lastName");


        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(confirmCardQuery());

        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("status", "failure");
            out.write(jsonObject.toString());

            request.getServletContext().log("Error here:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        finally {
            out.close();
        }
    }

    private String confirmCardQuery() {
        final String getCardQuery = "SELECT EXISTS (SELECT c.firstName FROM creditcards as c " +
                "WHERE c.firstName = ?) AS first_name_exists, " +
                "EXISTS (SELECT c.firstName, c.lastName FROM creditcards as c " +
                "WHERE c.firstName = ? AND c.lastName = ?) AS last_name_exists, " +
                "EXISTS (SELECT c.firstName, c.lastName, c.id FROM creditcards as c " +
                "WHERE c.firstName = ? AND c.lastName = ? AND c.id = ?) AS credit_card_exists, " +
                "EXISTS (SELECT c.firstName, c.lastName, c.id, c.expiration FROM creditcards as c " +
                "WHERE c.firstName = ? AND c.lastName = ? AND c.id = ? AND c.expiration = ?) AS expiration_exists";

        return getCardQuery;
    }
}
