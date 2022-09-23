package com.android2.googlemapapp.model;

public class User {

    private String email;
    private String username;
    private String user_id;


    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************
    public User() {
    }

    public User(String email, String username, String user_id) {
        this.email = email;
        this.username = username;
        this.user_id = user_id;
    }


    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getUser_id() {
        return user_id;
    }


    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
