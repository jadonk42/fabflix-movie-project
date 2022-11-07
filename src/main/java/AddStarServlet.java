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

@WebServlet(name = "AddStarServlet", value = "/api/add-star")
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
        // Set the response to be a JSON object
        response.setContentType("application/json");

        String starName = request.getParameter("starName");
        int birthYear = Integer.parseInt(request.getParameter("birthYear"));

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("CALL add_star(?, ?);");
            statement.setString(1, starName);
            statement.setInt(2, birthYear);

            ResultSet movieRs = statement.executeQuery();
            statement.close();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "success");
            jsonObject.addProperty("message", starName + " added successfully.");

            out.write(jsonObject.toString());
            response.setStatus(HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", e.getMessage());
            jsonObject.addProperty("status", "failure");
            out.write(jsonObject.toString());

            request.getServletContext().log("Error here:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        finally {
            out.close();
        }
    }

}
