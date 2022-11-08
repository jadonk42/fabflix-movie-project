package main.java;

public class Star {
    private String id;
    private String name;
    private int birthYear;

    public Star() {

    }

    public Star(String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details:\n");
        sb.append("Star Id: ").append(getId());
        sb.append("\n");
        sb.append("Star Name: ").append(getName());
        sb.append("\n");
        sb.append("Star Birth Year: ").append(getBirthYear());
        sb.append("\n");
        return sb.toString();
    }
}
