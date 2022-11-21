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
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
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

        searchResults.setOnClickListener(view -> searchMovie());
    }

    @SuppressLint("SetTextI18n")
    public void searchMovie() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies/fullTextSearch",
                response -> {
                    finish();
                    Intent movieListPage = new Intent(SearchActivity.this, MovieListActivity.class);
                    startActivity(movieListPage);
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                    searchMessage.setText("Unable to Search Query. Please try again");
                }) {
            @Override

            // Need to fix this
            protected Map<String, String> getParams() {
                // GET request form data
                final String params = new HashMap<>();
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}