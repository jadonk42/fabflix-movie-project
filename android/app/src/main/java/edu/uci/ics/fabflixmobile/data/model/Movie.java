package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String name;
    private final int year;
    private final String director;
    private final ArrayList<String> genres;
    private final ArrayList<String> stars;

    public Movie(String id, String name, int year, String director, ArrayList<String> genres, ArrayList<String> stars) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getDirector() { return director; }

    public int getYear() { return year; }

    public ArrayList<String> getGenres() { return genres; }

    public ArrayList<String> getStars() { return stars; }
}