package com.example.projetintegrateur.model;

import com.google.android.gms.maps.model.LatLng;

public class BusinessModel {

    private String name;
    private String address;
    private String rating;
    private LatLng coordinatesLatlng;

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    private  String photoURL;

    public LatLng getCoordinatesLatlng() {
        return coordinatesLatlng;
    }

    public void setCoordinatesLatlng(LatLng coordinatesLatlng) {
        this.coordinatesLatlng = coordinatesLatlng;
    }


    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
