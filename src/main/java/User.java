package main.java;
public class User {
    private final String username;
    private final boolean is_employee;

    public User(String username, boolean isemployee) {
        this.username = username;
        this.is_employee =  isemployee;
    }

    public String getUsername(){
        return this.username;
    }

    public boolean isEmployee(){
        return this.is_employee;
    }
}