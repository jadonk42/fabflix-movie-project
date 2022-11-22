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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.BackendServer;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class SearchActivity extends AppCompatActivity {

    private TextView searchMessage;
    private EditText searchQuery;
    Button searchResults;

    BackendServer server = new BackendServer();
    private final String baseURL = server.getBaseURL();

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
        final String query = searchQuery.getText().toString();

        final ArrayList<Movie> movies = new ArrayList<>();

        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies" + "?method=fsSearch&sortBy=ratingDesc&limit=20&page=1&full_text=" + query,
                response -> {
                    Log.d("search.success", response);

                    try {
                        JSONArray allMovies = new JSONArray(response);
                        for (int i = 0; i < allMovies.length(); i++) {
                            JSONObject singleMovie = new JSONObject(allMovies.get(i).toString());
                            String movieId = singleMovie.getString("movie_id");
                            String movieName = singleMovie.getString("movie_title");
                            String movieDirector = singleMovie.getString("movie_director");
                            String movieYear = singleMovie.getString("movie_year");
                            int getYear = Integer.parseInt(movieYear);
                            String movieGenres = singleMovie.getString("movie_genres");
                            String[] allGenres = movieGenres.split(",", 0);
                            String movieStars = singleMovie.getString("movie_stars");
                            String[] allStars = movieStars.split(",", 0) ;
                            ArrayList<String> stars = new ArrayList<>(Arrays.asList(allStars));
                            ArrayList<String> genres = new ArrayList<>(Arrays.asList(allGenres));

                            Movie currMovie = new Movie(movieId, movieName, getYear, movieDirector, genres, stars);

                            movies.add(currMovie);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    finish();
                    Intent movieList = new Intent(SearchActivity.this, MovieListActivity.class);
                    movieList.putParcelableArrayListExtra("movie_search_results", movies);
                    movieList.putExtra("movie_query", query);
                    movieList.putExtra("movie_page", 1);
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