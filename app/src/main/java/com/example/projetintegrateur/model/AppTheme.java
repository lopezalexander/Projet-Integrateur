package com.example.projetintegrateur.model;

public class AppTheme {
    private int backgroundColor = -16361597;
    private int searchBar_backgroundColor = -16361597;
    private String theme = "Midnight";

    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************

    private AppTheme() { }

    private static AppTheme appTheme = new AppTheme();

    public static AppTheme getInstance() {
        return appTheme;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getSearchBar_backgroundColor() {
        return searchBar_backgroundColor;
    }

    public void setSearchBar_backgroundColor(int searchBar_backgroundColor) {
        this.searchBar_backgroundColor = searchBar_backgroundColor;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
