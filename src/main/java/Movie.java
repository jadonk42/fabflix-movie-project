package main.java;

public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;

    public Movie() {

    }
    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details:\n");
        sb.append("Movie Id: ").append(getId());
        sb.append("\n");
        sb.append("Movie Title: ").append(getTitle());
        sb.append("\n");
        sb.append("Movie Year: ").append(getYear());
        sb.append("\n");
        sb.append("Movie Director: ").append(getDirector());
        sb.append("\n");
        return sb.toString();
    }
}
