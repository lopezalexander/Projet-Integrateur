package com.example.projetintegrateur.model;

import com.google.android.gms.maps.model.LatLng;

public class ItineraryModel {

    private static final ItineraryModel singleton = new ItineraryModel();

    LatLng origintLatLng; //address A ou 1
    String originAddressName; //address A ou 1

    LatLng destinationLatLng; // Address B ou 2
    String destinationAddressName; // Address B ou 2


    LatLng start_mid_point; //Testing purpopses for now
    LatLng end_mid_point;   //Testing purpopses for now
    LatLng midPointLatLng; // middle distance point

    LatLng selectedBusiness;
    String selectedBusinessAddressName;
    String selectedBusinessName;

    String userID;


    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************
    private ItineraryModel() {
    }

    public static ItineraryModel getInstance() {
        return singleton;
    }

    @Override
    public String toString() {
        return "ItineraryModel{" +
                "origintLatLng=" + origintLatLng +
                ", originAddressName='" + originAddressName + '\'' +
                ", destinationLatLng=" + destinationLatLng +
                ", destinationAddressName='" + destinationAddressName + '\'' +
                ", start_mid_point=" + start_mid_point +
                ", end_mid_point=" + end_mid_point +
                ", midPointLatLng=" + midPointLatLng +
                ", selectedBusiness=" + selectedBusiness +
                ", selectedBusinessAddressName='" + selectedBusinessAddressName + '\'' +
                ", selectedBusinessName='" + selectedBusinessName + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public LatLng getOrigintLatLng() {
        return origintLatLng;
    }

    public String getOriginAddressName() {
        return originAddressName;
    }

    public LatLng getDestinationLatLng() {
        return destinationLatLng;
    }

    public String getDestinationAddressName() {
        return destinationAddressName;
    }

    public LatLng getStart_mid_point() {
        return start_mid_point;
    }

    public LatLng getEnd_mid_point() {
        return end_mid_point;
    }

    public LatLng getMidPointLatLng() {
        return midPointLatLng;
    }

    public LatLng getSelectedBusiness() {
        return selectedBusiness;
    }

    public String getSelectedBusinessAddressName() {
        return selectedBusinessAddressName;
    }

    public String getSelectedBusinessName() {
        return selectedBusinessName;
    }

    public String getUserID() {
        return userID;
    }

    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setOrigintLatLng(LatLng origintLatLng) {
        this.origintLatLng = origintLatLng;
    }

    public void setOriginAddressName(String originAddressName) {
        this.originAddressName = originAddressName;
    }

    public void setDestinationLatLng(LatLng destinationLatLng) {
        this.destinationLatLng = destinationLatLng;
    }

    public void setDestinationAddressName(String destinationAddressName) {
        this.destinationAddressName = destinationAddressName;
    }

    public void setStart_mid_point(LatLng start_mid_point) {
        this.start_mid_point = start_mid_point;
    }

    public void setEnd_mid_point(LatLng end_mid_point) {
        this.end_mid_point = end_mid_point;
    }

    public void setMidPointLatLng(LatLng midPointLatLng) {
        this.midPointLatLng = midPointLatLng;
    }

    public void setSelectedBusiness(LatLng selectedBusiness) {
        this.selectedBusiness = selectedBusiness;
    }

    public void setSelectedBusinessAddressName(String selectedBusinessAddressName) {
        this.selectedBusinessAddressName = selectedBusinessAddressName;
    }

    public void setSelectedBusinessName(String selectedBusinessName) {
        this.selectedBusinessName = selectedBusinessName;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
