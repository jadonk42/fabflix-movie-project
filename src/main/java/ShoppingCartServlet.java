package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.io.PrintWriter;
import java.util.Map;

import static java.lang.Integer.parseInt;

@WebServlet(name = "ShoppingCartServlet", value = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();
        String movieTitle = request.getParameter("movieToBuy");

        try {
            HttpSession session = request.getSession();
            HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("shoppingCart");

            if (cart == null) {
                cart = new HashMap<>();
                session.setAttribute("shoppingCart", cart);
            }

            request.getServletContext().log("getting " + cart.size() + " movies");

            synchronized (cart) {
                if (cart.containsKey(movieTitle)) {
                    cart.put(movieTitle, cart.get(movieTitle) + 1);
                }
                else if (movieTitle != null && !movieTitle.equals("null")) {
                    cart.put(movieTitle, 1);
                }
            }

            // Populate JSON object
            for (Map.Entry<String, Integer> movie : cart.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_name", movie.getKey());
                jsonObject.addProperty("movie_quantity", movie.getValue().toString());
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
        String action = request.getParameter("action");
        HttpSession session = request.getSession(true);
        if (session.getAttribute("shoppingCart") == null) {
            session.setAttribute("shoppingCart", new HashMap<String, Integer>());
        }
        HashMap<String, Integer> shoppingCart = (HashMap)session.getAttribute("shoppingCart");

        if (action.equals("addToCart")) {
            int quantity = 1;
            String movieID = request.getParameter("movie");
            //update K, V pair for the movie by adding the quantity
            if(shoppingCart.get(movieID) == null){
                shoppingCart.put(movieID, quantity);
            }
            else {
                shoppingCart.put(movieID, shoppingCart.get(movieID)+ quantity);
            }
        }
        else if (action.equals("removeFromCart")) {
            String movieID = request.getParameter("movie");
            //update K, V pair for the movie by subtracting the quantity
            if(shoppingCart.get(movieID) == null){
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR); //item was never in shopping cart, bad call
            }
            else {
                shoppingCart.remove(movieID);
            }
        }
        else if (action.equals("modifyQuantity")) {
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String movieID = request.getParameter("movie");
            if(shoppingCart.get(movieID) == null){
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR); //item was never in shopping cart, bad call
            }
            else {
                shoppingCart.put(movieID, quantity);
            }
        }

        response.setStatus(HttpURLConnection.HTTP_OK);
    }
}
