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

@WebServlet(name = "AddMovieServlet", value = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {
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

        String title = request.getParameter("title");
        int year = Integer.parseInt(request.getParameter("year"));
        String director = request.getParameter("director");
        String genre = request.getParameter("genre");
        String star = request.getParameter("star");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("CALL add_movie(?, ?, ? , ? ,? , @result);");
            statement.setString(1, title);
            statement.setInt(2, year);
            statement.setString(3, director);
            statement.setString(4, genre);
            statement.setString(5, star);

            ResultSet rs = statement.executeQuery();
            String result = "4";

            while (rs.next()) {
                result = rs.getString("outResult");
            }
            /*
            0 = nothing existed,
            1 = star and genre existed already,
            2 = genre existed already,
            3 = star existed already
            4 = movie existed already
            */
            String message = "";
            switch(result) {
                case "0":
                    message = String.format("Added '%s' successfully. Star '%s' and Genre '%s' not found, both were created.", title, star, genre);
                    break;
                case "1":
                    message = String.format("Added '%s' successfully. Star '%s' and Genre '%s' already existed and linked to movie.", title, star, genre);
                    break;
                case "2":
                    message = String.format("Added '%s' successfully. Genre '%s' already existed and linked to movie, Star '%s' not found and created.", title, genre, star);
                    break;
                case "3":
                    message = String.format("Added '%s' successfully. Star '%s' already existed and linked to movie, Genre '%s' not found and created.", title, star, genre);
                    break;
                case "4":
                    message = String.format("Movie '%s' in %d with director '%s' already found. Movie not added.", title, year, director);
                    break;
            }

            rs.close();

            statement.close();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "success");
            jsonObject.addProperty("message", message);

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
