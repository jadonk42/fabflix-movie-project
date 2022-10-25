package main.java;

import com.google.gson.JsonArray;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "MovieOrderConfirmationServlet", value = "/api/movie-confirmation")
public class MovieOrderConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Need to get movie ID for each movie title
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Insert the movies into the database

        // get the sale id from the database

        // retrieve the rest of the information through sessions
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();

        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String saleDate = dateObj.format(formatter);

        HttpSession session = request.getSession();
        HashMap<String, Integer> cart = (HashMap) session.getAttribute("shoppingCart");

        User getUserEmail = (User) session.getAttribute("user");
        String customerIdQuery = getCustomerId(getUserEmail.username);

        try(Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(customerIdQuery);
            ResultSet rs = statement.executeQuery();
            String customerId = "";
            String movieId = "";
            while (rs.next()) {
                customerId = rs.getString("id");
            }
            rs.close();
            statement.close();

            for (Map.Entry<String, Integer> movie : cart.entrySet()) {
                if (!movie.getKey().equals("null")) {
                    PreparedStatement movieStatement = conn.prepareStatement(getMovieId(movie.getKey()));
                    ResultSet movieRs = movieStatement.executeQuery();
                    while (movieRs.next()) {
                        movieId = movieRs.getString("id");
                    }
                    movieRs.close();
                    movieStatement.close();

                    PreparedStatement insertMovieStatement = conn.prepareStatement(insertMovieSale(movieId, saleDate, customerId));
                    insertMovieStatement.executeUpdate();
                    insertMovieStatement.close();

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_name", movie.getKey());
                    jsonObject.addProperty("movie_quantity", movie.getValue().toString());
                    jsonArray.add(jsonObject);
                }
            }

            // Retrieve the sale id by the customer
            PreparedStatement transactionsStatement = conn.prepareStatement(getAllTransactions(saleDate, customerId));
            ResultSet transactionsRs = transactionsStatement.executeQuery();

            while (transactionsRs.next()) {
                String sales = transactionsRs.getString("id");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("saleId", sales);
                jsonArray.add(jsonObject);
            }
            transactionsRs.close();
            transactionsStatement.close();

            out.write(jsonArray.toString());
            response.setStatus(HttpURLConnection.HTTP_OK);


        } catch(Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private String getMovieId(String movieName) {
        String movieId = "SELECT id FROM movies WHERE title = '" + movieName + "'";
        return movieId;
    }

    // Need to get the current customer id

    private String getCustomerId(String customerEmail) {
        String customerId = "SELECT id FROM customers WHERE email = '" + customerEmail + "'";
        return customerId;
    }
    private String insertMovieSale(String movieId, String saleDate, String customerId) {
        String insertIntoSalesTable = "INSERT INTO sales (customerId, movieId, saleDate) " +
                "VALUES('" + customerId + "', '" + movieId + "', '" + saleDate + "')";
        return insertIntoSalesTable;
    }

    private String getAllTransactions(String saleDate, String customerId) {
        String transactions = "SELECT GROUP_CONCAT(id) as id FROM sales " +
                "WHERE customerId = '" + customerId + "' AND saleDate = '" + saleDate + "'";
        return transactions;
    }
}
