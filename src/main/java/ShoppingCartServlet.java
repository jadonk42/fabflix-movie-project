package main.java;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

@WebServlet(name = "ShoppingCartServlet", value = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /**
         * SHOULD GET THE SHOPPING CART VARIABLES AND RETURN IN A JSON FORMAT SIMILAR TO QUERY.
         */
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(true);
        if (session.getAttribute("shoppingCart") == null) {
            session.setAttribute("shoppingCart", new HashMap<String, Integer>());
        }
        HashMap<String, Integer> shoppingCart = (HashMap)session.getAttribute("lastQueryString");

        if (action.equals("addToCart")) {
            int quantity = Integer.parseInt(request.getParameter("quantity"));
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
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String movieID = request.getParameter("movie");
            //update K, V pair for the movie by subtracting the quantity
            if(shoppingCart.get(movieID) == null){
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR); //item was never in shopping cart, bad call
            }
            else {
                shoppingCart.put(movieID, shoppingCart.get(movieID) - quantity);
                if (shoppingCart.get(movieID) < 0) {
                    shoppingCart.put(movieID, 0); //shopping cart shouldn't have negative quantity - could lead to bugs
                }
            }
        }

        response.setStatus(HttpURLConnection.HTTP_OK);
    }
}
