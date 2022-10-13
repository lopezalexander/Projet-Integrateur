package com.example.projetintegrateur.model;

import com.example.projetintegrateur.R;

public class AppTheme {
    private int backgroundColor = -16361597;
    private int searchBar_backgroundColor = -16361597;
    private String theme = "Midnight";
    private int buttonBg = R.drawable.icon_container_settings2;

    private static final AppTheme appTheme = new AppTheme();

    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************

    private AppTheme() {
    }


    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public static AppTheme getInstance() {
        return appTheme;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getSearchBar_backgroundColor() {
        return searchBar_backgroundColor;
    }

    public String getTheme() {
        return theme;
    }

    public int getButtonBg() {
        return buttonBg;
    }


    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setSearchBar_backgroundColor(int searchBar_backgroundColor) {
        this.searchBar_backgroundColor = searchBar_backgroundColor;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setButtonBg(int buttonBg) {
        this.buttonBg = buttonBg;
    }

}
