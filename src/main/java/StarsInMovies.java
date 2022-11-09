package main.java;

public class StarsInMovies {
    private String movieName;
    private String starName;

    public StarsInMovies() {

    }

    public StarsInMovies(String movieName, String starName) {
        this.movieName = movieName;
        this.starName = starName;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star in Movies Details:\n");
        sb.append("Movie Name: ").append(getMovieName());
        sb.append("\n");
        sb.append("Star Name: ").append(getStarName());
        sb.append("\n");
        return sb.toString();
    }


}
