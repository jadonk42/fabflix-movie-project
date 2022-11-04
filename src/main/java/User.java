package main.java;
public class User {
    private final String username;
    private final boolean is_employee;

    public User(String username) {
        this.username = username;
        this.is_employee =  false;
    }

    public String getUsername(){
        return this.username;
    }

    public boolean isEmployee(){
        return this.is_employee;
    }
}