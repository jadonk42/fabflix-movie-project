package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
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

    Button prevButton;
    Button nextButton;
    Button searchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        prevButton = findViewById(R.id.prevPage);
        nextButton = findViewById(R.id.nextPage);
        searchButton = findViewById(R.id.search_back);
        ArrayList<Movie> movies;
        Bundle b = getIntent().getExtras();
        movies = b.getParcelableArrayList("movie_search_results");
        String movieQuery = b.getString("movie_query");
        int moviePage = b.getInt("movie_page");
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
                            movieList.putParcelableArrayListExtra("all_movies_list", finalMovies);
                            movieList.putExtra("query_search", movieQuery);
                            movieList.putExtra("query_page_number", moviePage);
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
        });
        prevButton.setOnClickListener(view -> getPrevResults(moviePage, movieQuery, finalMovies));
        nextButton.setOnClickListener(view -> getNextResults(moviePage, movieQuery));
        searchButton.setOnClickListener(view -> backToSearchPage());


    }

    public void backToSearchPage() {
        finish();
        Intent searchPage = new Intent(MovieListActivity.this, SearchActivity.class);
        startActivity(searchPage);
    }

    public void getNextResults(int pageNum, String query) {
        pageNum += 1;
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final ArrayList<Movie> movies = new ArrayList<>();

        // request type is GET
        int finalPageNum = pageNum;
        final StringRequest nextResults = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies" + "?method=fsSearch&full_text=" + query + "&sortBy=ratingDesc&limit=20&page=" + finalPageNum,
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
                    Intent movieList = new Intent(MovieListActivity.this, MovieListActivity.class);
                    movieList.putParcelableArrayListExtra("movie_search_results", movies);
                    movieList.putExtra("movie_query", query);
                    movieList.putExtra("movie_page", finalPageNum);
                    startActivity(movieList);
                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                });
        // important: queue.add is where the login request is actually sent
        queue.add(nextResults);


    }

    public void getPrevResults(int pageNum, String query, ArrayList<Movie> oriMovies) {
        if (pageNum < 2) {
            finish();
            Intent movieList = new Intent(MovieListActivity.this, MovieListActivity.class);
            movieList.putParcelableArrayListExtra("movie_search_results", oriMovies);
            movieList.putExtra("movie_query", query);
            movieList.putExtra("movie_page", 1);
            startActivity(movieList);
            return;
        }
        pageNum -= 1;

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final ArrayList<Movie> movies = new ArrayList<>();

        // request type is GET
        int finalPageNum = pageNum;
        final StringRequest prevResults = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies" + "?method=fsSearch&full_text=" + query + "&sortBy=ratingDesc&limit=20&page=" + finalPageNum,
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
                    Intent movieList = new Intent(MovieListActivity.this, MovieListActivity.class);
                    movieList.putParcelableArrayListExtra("movie_search_results", movies);
                    movieList.putExtra("movie_query", query);
                    movieList.putExtra("movie_page", finalPageNum);
                    startActivity(movieList);
                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                });
        // important: queue.add is where the login request is actually sent
        queue.add(prevResults);
    }
}