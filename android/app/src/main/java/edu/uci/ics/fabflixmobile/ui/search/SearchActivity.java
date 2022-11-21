package edu.uci.ics.fabflixmobile.ui.search;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class SearchActivity extends AppCompatActivity {

    private TextView searchMessage;
    private EditText searchQuery;
    Button searchResults;

    private final String host = "52.12.152.71";
    private final String port = "8080";
    private final String domain = "cs-122b-group-37";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchMessage = findViewById(R.id.search_message);
        searchQuery = findViewById(R.id.search_entry);
        searchResults = findViewById(R.id.search_button);

        searchResults.setOnClickListener(view -> {
            try {
                searchMovie();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void searchMovie() throws UnsupportedEncodingException {

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final String query = searchQuery.getText().toString();
        final String movieSearch = java.net.URLEncoder.encode(query, "utf-8");
        // final String url = baseURL + "/api/movies/fullTextSearch?method=fsSearch&full_text=" + movieSearch + "&sortBy=ratingDesc&page=1";

        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies" + "?method=fsSearch&full_text=" + query + "&sortBy=ratingDesc&limit=20&page=1",
                response -> {
                    Log.d("search.success", response);
                    finish();
                    Intent movieList = new Intent(SearchActivity.this, MovieListActivity.class);
                    startActivity(movieList);
                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                    searchMessage.setText(error.toString());
                });
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}