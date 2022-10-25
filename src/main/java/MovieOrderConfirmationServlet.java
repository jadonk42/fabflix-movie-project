package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "MovieOrderConfirmationServlet", value = "/api/movie-confirmation")
public class MovieOrderConfirmationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Insert the movies into the database

        // get the sale id from the database

        // retrieve the rest of the information through sessions
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();

        try {
            HttpSession session = request.getSession();
            HashMap<String, Integer> cart = (HashMap) session.getAttribute("shoppingCart");

//            if (cart == null) {
//                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR); // cart is empty
//            }
//
//            assert cart != null;
//            request.getServletContext().log("getting " + cart.size() + " movies");

            // Populate JSON object
            int totalMoviePrice = 0;
            for (Map.Entry<String, Integer> movie : cart.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_name", movie.getKey());
                jsonObject.addProperty("movie_price", "20");
                jsonObject.addProperty("movie_quantity", movie.getValue().toString());
//                totalMoviePrice = totalMoviePrice + (20 * movie.getValue());
//                jsonObject.addProperty("movie_total_price", Integer.toString(totalMoviePrice));
                jsonArray.add(jsonObject);
            }

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
}
