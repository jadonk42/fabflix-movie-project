package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.BackendServer;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.search.SearchActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class MovieListActivity extends AppCompatActivity {

    BackendServer server = new BackendServer();
    private final String baseURL = server.getBaseURL();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        ArrayList<Movie> movies;
        Bundle b = getIntent().getExtras();
        movies = b.getParcelableArrayList("movie_search_results");
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        ArrayList<Movie> finalMovies = movies;
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = finalMovies.get(position);
            String movieId = movie.getId();
            String movieName = movie.getName();
            String director = movie.getDirector();
            int year = movie.getYear();
            final RequestQueue queue = NetworkManager.sharedManager(this).queue;

            final StringRequest singleMovieRequest = new StringRequest(
                    Request.Method.GET,
                    baseURL + "/api/single-movie?id=" + movieId,
                    response -> {
                        Log.d("search.success", response);
                        Movie singleMovie;
                        // create single movie
                        try {
                            JSONObject currMovie = new JSONObject(response);
                            String movieGenres = currMovie.getString("movie_genres");
                            String[] allGenres = movieGenres.split(",", 0);
                            String movieStars = currMovie.getString("movie_stars");
                            String[] allStars = movieStars.split(",", 0) ;
                            ArrayList<String> stars = new ArrayList<>(Arrays.asList(allStars));
                            ArrayList<String> genres = new ArrayList<>(Arrays.asList(allGenres));
                            singleMovie = new Movie(movieId, movieName, year, director, stars, genres);

                            finish();
                            Intent movieList = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            movieList.putExtra("single_movie_results", singleMovie);
                            startActivity(movieList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        // error
                        Log.d("singleMovie.error", error.toString());
                    });

            queue.add(singleMovieRequest);

            // This is where we transition to the Single Movie Page
//            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }
}