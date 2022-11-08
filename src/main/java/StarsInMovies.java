package main.java;

public class StarsInMovies {
    private String movieId;
    private String starName;

    public StarsInMovies() {

    }

    public StarsInMovies(String movieId, String starName) {
        this.movieId = movieId;
        this.starName = starName;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
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
        sb.append("Movie Id: ").append(getMovieId());
        sb.append("\n");
        sb.append("Star Name: ").append(getStarName());
        sb.append("\n");
        return sb.toString();
    }


}
