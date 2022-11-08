package main.java;

public class GenresInMovies {
    private String movieId;
    private String genreName;

    public GenresInMovies() {

    }

    public GenresInMovies(String movieId, String genreName) {
        this.movieId = movieId;
        this.genreName = genreName;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Genres in Movies Details:\n");
        sb.append("Movie Id: ").append(getMovieId());
        sb.append("\n");
        sb.append("Genre Id: ").append(getGenreName());
        sb.append("\n");
        return sb.toString();
    }
}
