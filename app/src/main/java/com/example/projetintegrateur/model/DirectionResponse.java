package com.example.projetintegrateur.model;

import com.example.projetintegrateur.model.directionAPI.GeocodedWaypoint;
import com.example.projetintegrateur.model.directionAPI.Route;

import java.util.ArrayList;

public class DirectionResponse {
    public ArrayList<GeocodedWaypoint> geocoded_waypoints;
    public ArrayList<Route> routes;
    public String status;


    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public ArrayList<GeocodedWaypoint> getGeocoded_waypoints() {
        return geocoded_waypoints;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public String getStatus() {
        return status;
    }


    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setGeocoded_waypoints(ArrayList<GeocodedWaypoint> geocoded_waypoints) {
        this.geocoded_waypoints = geocoded_waypoints;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }


    public void setStatus(String status) {
        this.status = status;
    }
}


