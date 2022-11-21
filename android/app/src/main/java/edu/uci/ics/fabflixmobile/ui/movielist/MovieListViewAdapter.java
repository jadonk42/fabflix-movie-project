package edu.uci.ics.fabflixmobile.ui.movielist;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private final ArrayList<Movie> movies;

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView year;
        TextView director;
        TextView genres;
        TextView stars;
    }

    public MovieListViewAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.movielist_row, movies);
        this.movies = movies;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the movie item for this position
        Movie movie = movies.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.movielist_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.year = convertView.findViewById(R.id.year);
            viewHolder.director = convertView.findViewById(R.id.director);
            viewHolder.genres = convertView.findViewById(R.id.genres);
            viewHolder.stars = convertView.findViewById(R.id.stars);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        ArrayList<String> allGenres = movie.getGenres();
        ArrayList<String> allStars = movie.getStars();
        StringBuilder getTopStars = new StringBuilder();
        int minStars = Math.min(3, allStars.size());
        for (int i = 0; i < minStars; i++) {
            getTopStars.append(allStars.get(i));
            if (i < minStars - 1) {
                getTopStars.append(", ");
            }
        }

        StringBuilder getTopGenres = new StringBuilder();
        int minGenres = Math.min(3, allGenres.size());
        for (int i = 0; i < minGenres; i++) {
            getTopGenres.append(allGenres.get(i));
            if (i < minGenres - 1) {
                getTopGenres.append(", ");
            }
        }

        viewHolder.title.setText(movie.getName());
        viewHolder.year.setText(movie.getYear() + "");
        viewHolder.director.setText(movie.getDirector());
        viewHolder.genres.setText(getTopGenres);
        viewHolder.stars.setText(getTopStars);
        // Return the completed view to render on screen
        return convertView;
    }
}