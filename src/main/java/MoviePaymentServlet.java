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
public class MoviePaymentServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNum = request.getParameter("creditCardNum");
        String expirationDate = request.getParameter("expirationDate");
        response.setContentType("application/json");

        String first_name_exists = "0";
        String last_name_exists = "0";
        String credit_card_exists = "0";
        String expiration_exists = "0";

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(confirmCardQuery(firstName, lastName, creditCardNum, expirationDate));

            ResultSet rs = statement.executeQuery();
            JsonObject jsonObject = new JsonObject();
            while (rs.next()) {
                first_name_exists = rs.getString("first_name_exists");
                last_name_exists = rs.getString("last_name_exists");
                credit_card_exists = rs.getString("credit_card_exists");
                expiration_exists = rs.getString("expiration_exists");
            }

            final boolean firstNameExists = (Integer.parseInt(first_name_exists) == 1);
            final boolean lastNameExists = (Integer.parseInt(last_name_exists) == 1);
            final boolean creditCardExists = (Integer.parseInt(credit_card_exists) == 1);
            final boolean expirationExists = (Integer.parseInt(expiration_exists) == 1);

            if (firstNameExists && lastNameExists && creditCardExists && expirationExists) {
                jsonObject.addProperty("status", "success");
            }
            else {
                jsonObject.addProperty("status", "fail");
                if (!firstNameExists) {
                    jsonObject.addProperty("message", "First Name does not exist");
                }
                else if (!lastNameExists) {
                    jsonObject.addProperty("message", "Last Name does not exist");
                }
                else if (!creditCardExists) {
                    jsonObject.addProperty("message", "Incorrect Credit Card Number");
                }
                else {
                    jsonObject.addProperty("message", "Invalid Card Expiration Date");
                }
            }
            rs.close();
            statement.close();
            out.write(jsonObject.toString());
            response.setStatus(HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error here:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        finally {
            out.close();
        }
    }

    private String confirmCardQuery(String firstName, String lastName, String creditCardNum, String expirationDate) {

        String getCardQuery = "SELECT EXISTS (SELECT c.firstName FROM creditcards as c " +
                "WHERE c.firstName = '" + firstName + "') AS first_name_exists, " +
                "EXISTS (SELECT c.firstName, c.lastName FROM creditcards as c " +
                "WHERE c.firstName = '" + firstName + "' AND c.lastName = '" + lastName + "') AS last_name_exists, " +
                "EXISTS (SELECT c.firstName, c.lastName, c.id FROM creditcards as c " +
                "WHERE c.firstName = '" + firstName + "' AND c.lastName = '" + lastName + "' AND c.id = '" +
                creditCardNum + "') AS credit_card_exists, " +
                "EXISTS (SELECT c.firstName, c.lastName, c.id, c.expiration FROM creditcards as c " +
                "WHERE c.firstName = '" + firstName + "' AND c.lastName = '" + lastName + "' AND c.id = '" +
                creditCardNum + "' AND c.expiration = '" + expirationDate + "') AS expiration_exists";

        return getCardQuery;
    }
}
