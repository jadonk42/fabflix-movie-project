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
        System.out.println("in shopping cart servlet");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();

        try {
            HttpSession session = request.getSession(true);
            HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("shoppingCart");

            if (cart == null) {
                System.out.println("there is no cart");
                out.write(jsonArray.toString());
                response.setStatus(HttpURLConnection.HTTP_OK);
                return;
            }

            System.out.println("there is a cart");
            request.getServletContext().log("getting " + cart.size() + " movies");


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
            String movie = request.getParameter("movie");
            //update K, V pair for the movie by adding the quantity
            if(shoppingCart.get(movie) == null){
                System.out.println("just added " + movie + " amount=" + quantity);
                shoppingCart.put(movie, quantity);
            }
            else {
                shoppingCart.put(movie, shoppingCart.get(movie)+ quantity);
                System.out.println("just added " + movie + " amount=" + quantity);
            }
        }
        else if (action.equals("removeFromCart")) {
            String movie = request.getParameter("movie");
            if(shoppingCart.get(movie) == null){
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR); //item was never in shopping cart, bad call
            }
            else {
                shoppingCart.remove(movie);
            }
        }
        else if (action.equals("modifyQuantity")) {
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String movie = request.getParameter("movie");
            if(shoppingCart.get(movie) == null){
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR); //item was never in shopping cart, bad call
            }
            else {
                shoppingCart.put(movie, quantity);
            }
        }
        session.setAttribute("shoppingCart", shoppingCart);

        response.setStatus(HttpURLConnection.HTTP_OK);
    }
}
