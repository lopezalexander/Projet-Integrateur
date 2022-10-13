package com.example.projetintegrateur.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class BusinessModel {

    private String name;
    private String address;
    private String rating;
    private LatLng coordinatesLatlng;
    private String photoURL;
    private ArrayList<String> types;


    public BusinessModel() {
    }


    public BusinessModel(String name,
                         String address,
                         String rating,
                         LatLng coordinatesLatlng,
                         String photoURL,
                         ArrayList<String> types) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.coordinatesLatlng = coordinatesLatlng;
        this.photoURL = photoURL;
        this.types = types;

    }

    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getRating() {
        return rating;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public LatLng getCoordinatesLatlng() {
        return coordinatesLatlng;
    }


    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setCoordinatesLatlng(LatLng coordinatesLatlng) {
        this.coordinatesLatlng = coordinatesLatlng;
    }

}
