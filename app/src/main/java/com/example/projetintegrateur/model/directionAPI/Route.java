package com.example.projetintegrateur.model.directionAPI;

import java.util.ArrayList;

public class Route {
    public Bounds bounds;
    public String copyrights;
    public ArrayList<Leg> legs;
    public OverviewPolyline overview_polyline;
    public String summary;
    public ArrayList<Object> warnings;
    public ArrayList<Object> waypoint_order;

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public ArrayList<Leg> getLegs() {
        return legs;
    }

    public void setLegs(ArrayList<Leg> legs) {
        this.legs = legs;
    }

    public OverviewPolyline getOverview_polyline() {
        return overview_polyline;
    }

    public void setOverview_polyline(OverviewPolyline overview_polyline) {
        this.overview_polyline = overview_polyline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ArrayList<Object> getWarnings() {
        return warnings;
    }

    public void setWarnings(ArrayList<Object> warnings) {
        this.warnings = warnings;
    }

    public ArrayList<Object> getWaypoint_order() {
        return waypoint_order;
    }

    public void setWaypoint_order(ArrayList<Object> waypoint_order) {
        this.waypoint_order = waypoint_order;
    }
}
