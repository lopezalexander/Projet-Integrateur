package com.example.projetintegrateur.model;

import androidx.annotation.NonNull;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class User {

    private String email;
    private String user_id;
    private String name;
    private String photoUrl;
    private AppTheme theme;


    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************
    public User() {
    }

    public User(String email, String user_id, String name, String photoUrl, AppTheme theme) {
        this.email = email;
        this.user_id = user_id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.theme = theme;
    }


    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public String getEmail() {
        return email;
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

    public AppTheme getTheme() {
        return this.theme;
    }


    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setEmail(String email) {
        this.email = email;
    }


    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setTheme(AppTheme theme) {
        this.theme = theme;
    }



    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }


}