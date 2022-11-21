package edu.uci.ics.fabflixmobile.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie implements Parcelable {
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

    protected Movie(Parcel in) {
        id = in.readString();
        name = in.readString();
        year = in.readInt();
        director = in.readString();
        genres = in.createStringArrayList();
        stars = in.createStringArrayList();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getDirector() { return director; }

    public int getYear() { return year; }

    public ArrayList<String> getGenres() { return genres; }

    public ArrayList<String> getStars() { return stars; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeInt(year);
        parcel.writeString(director);
        parcel.writeStringList(genres);
        parcel.writeStringList(stars);
    }
}