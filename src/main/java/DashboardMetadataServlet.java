import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import javax.sql.DataSource;
import java.net.HttpURLConnection;
import java.sql.*;

@WebServlet(name = "DashboardMetadataServlet", urlPatterns = "/api/metadata")
public class DashboardMetadataServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private static String[] TABLES= new String[]{"movies", "stars", "genres", "ratings",
            "stars_in_movies", "genres_in_movies", "customers", "creditcards",
            "sales", "employees"};

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < TABLES.length; ++i) {
                PreparedStatement statement;
                statement = conn.prepareStatement("SHOW COLUMNS FROM " + TABLES[i]);

                ResultSet rs = statement.executeQuery();

                JsonObject tableColumns = new JsonObject();

                tableColumns.add(TABLES[i], getColumnsAsJsonArray(rs)); //tables: columns

                jsonArray.add(tableColumns);
                rs.close();
                statement.close();
            }

            out.write(jsonArray.toString());

            response.setStatus(HttpURLConnection.HTTP_OK);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            out.close();
        }
    }

    private JsonArray getColumnsAsJsonArray(ResultSet rs) throws SQLException {
        JsonArray jsonArray = new JsonArray();

        while (rs.next()) {
            String field = rs.getString("Field");
            String type = rs.getString("Type");
            String nullOrNot = rs.getString("Null");
            String key = rs.getString("Key");
            String defaultVal = rs.getString("Default");
            String extra = rs.getString("Extra");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("field", field);
            jsonObject.addProperty("type", type);
            jsonObject.addProperty("nullOrNot", nullOrNot);
            jsonObject.addProperty("key", key);
            jsonObject.addProperty("defaultVal", defaultVal);
            jsonObject.addProperty("extra", extra);

            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }
}