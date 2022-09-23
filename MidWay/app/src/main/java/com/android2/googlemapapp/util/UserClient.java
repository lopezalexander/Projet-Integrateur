package com.android2.googlemapapp.util;


import android.app.Application;

import com.android2.googlemapapp.model.User;

public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
