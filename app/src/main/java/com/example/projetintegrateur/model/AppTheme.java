package com.example.projetintegrateur.model;

public class AppTheme {
    int backgroundColor = -16361597;
    private static AppTheme appTheme = new AppTheme();

    public static AppTheme getInstance() {
        return appTheme;
    }
    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************

    public AppTheme() { }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
