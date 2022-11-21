package edu.uci.ics.fabflixmobile.ui.movielist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

public class SingleMovieActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        Movie singleMovie;
        Bundle b = getIntent().getExtras();
        singleMovie = b.getParcelable("single_movie_results");
        ArrayList<Movie> movies = new ArrayList<>();
        movies.add(singleMovie);
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
    }
}