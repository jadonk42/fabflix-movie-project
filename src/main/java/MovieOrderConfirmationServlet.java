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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // retrieve cart info from session and sale id from database
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();

        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String saleDate = dateObj.format(formatter);

        HttpSession session = request.getSession();
        HashMap<String, Integer> cart = (HashMap) session.getAttribute("shoppingCart");

        User getUserEmail = (User) session.getAttribute("user");
        String customerIdQuery = getCustomerId();

        try(Connection conn = dataSource.getConnection()) {
            // get the customer id
            PreparedStatement statement = conn.prepareStatement(customerIdQuery);
            statement.setString(1, getUserEmail.getUsername());
            ResultSet rs = statement.executeQuery();
            String customerId = "";
            while (rs.next()) {
                customerId = rs.getString("id");
            }
            rs.close();
            statement.close();

            String movieId = "";
            for (Map.Entry<String, Integer> movie : cart.entrySet()) {
                if (!movie.getKey().equals("null")) {
                    // get the movie id
                    PreparedStatement movieStatement = conn.prepareStatement(getMovieId());
                    movieStatement.setString(1, movie.getKey());
                    ResultSet movieRs = movieStatement.executeQuery();
                    while (movieRs.next()) {
                        movieId = movieRs.getString("id");
                    }
                    movieRs.close();
                    movieStatement.close();

                    // insert each movie
                    PreparedStatement insertMovieStatement = conn.prepareStatement(insertMovieSale());
                    insertMovieStatement.setString(1, customerId);
                    insertMovieStatement.setString(2, movieId);
                    insertMovieStatement.setString(3, saleDate);
                    insertMovieStatement.executeUpdate();
                    insertMovieStatement.close();

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_name", movie.getKey());
                    jsonObject.addProperty("movie_quantity", movie.getValue().toString());
                    jsonArray.add(jsonObject);
                }
            }

            // Retrieve the sale id by the customer
            PreparedStatement transactionsStatement = conn.prepareStatement(getAllTransactions());
            transactionsStatement.setString(1, customerId);
            transactionsStatement.setString(2, saleDate);
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

    private String getMovieId() {
        final String movieId = "SELECT id FROM movies WHERE title = ?";
        return movieId;
    }

    private String getCustomerId() {
        final String customerId = "SELECT id FROM customers WHERE email = ?";
        return customerId;
    }
    private String insertMovieSale() {
        String insertIntoSalesTable = "INSERT INTO sales (customerId, movieId, saleDate) " +
                "VALUES(?, ?, ?)";
        return insertIntoSalesTable;
    }

    private String getAllTransactions() {
        final String transactions = "SELECT GROUP_CONCAT(id) as id FROM sales " +
                "WHERE customerId = ? AND saleDate = ?";
        return transactions;
    }
}
