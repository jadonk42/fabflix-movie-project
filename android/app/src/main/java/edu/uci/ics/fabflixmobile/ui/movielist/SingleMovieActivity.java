package edu.uci.ics.fabflixmobile.ui.movielist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

public class SingleMovieActivity extends AppCompatActivity {

    Button backToMoviesPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlemovie);
        backToMoviesPage = findViewById(R.id.moviesListPage);
        Movie singleMovie;
        Bundle b = getIntent().getExtras();
        singleMovie = b.getParcelable("single_movie_results");
        ArrayList<Movie> allMovies = b.getParcelableArrayList("all_movies_list");
        String query = b.getString("query_search");
        int pageNum = b.getInt("query_page_number");

        ArrayList<Movie> movies = new ArrayList<>();
        movies.add(singleMovie);
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.singleMovieList);
        listView.setAdapter(adapter);
        backToMoviesPage.setOnClickListener(view -> returnToMoviesPage(allMovies, query, pageNum));
    }

    public void returnToMoviesPage(ArrayList<Movie> allMovies, String query, int pageNum) {
        finish();
        Intent moviesPages = new Intent(SingleMovieActivity.this, MovieListActivity.class);
        moviesPages.putParcelableArrayListExtra("movie_search_results", allMovies);
        moviesPages.putExtra("movie_query", query);
        moviesPages.putExtra("movie_page", pageNum);
        startActivity(moviesPages);
    }
}