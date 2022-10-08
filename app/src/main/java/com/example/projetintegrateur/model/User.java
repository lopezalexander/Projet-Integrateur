package com.example.projetintegrateur.model;
import android.net.Uri;
public class User {

    private String email;
    private String username;
    private String user_id;
    private String name;
    private String photoUrl;
    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************
    public User() {
    }

    public User(String email, String username, String user_id, String name, String photoUrl) {
        this.email = email;
        this.username = username;
        this.user_id = user_id;
        this.name = name;
        this.photoUrl = photoUrl;
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

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
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

    public void setName(String name) { this.name = name; }

    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}