package com.example.projetintegrateur.model;

import androidx.annotation.NonNull;

import com.example.projetintegrateur.util.CustomLatLng;
import com.google.android.gms.maps.model.LatLng;

public class ItineraryModel {

    CustomLatLng origintLatLng; //address A ou 1
    String originAddressName; //address A ou 1

    CustomLatLng destinationLatLng; // Address B ou 2
    String destinationAddressName; // Address B ou 2


    CustomLatLng start_mid_point; //Testing purpopses for now
    CustomLatLng end_mid_point;   //Testing purpopses for now
    CustomLatLng midPointLatLng; // middle distance point

    CustomLatLng selectedBusiness;
    String selectedBusinessAddressName;
    String selectedBusinessName;

    String userID;


    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************
    public ItineraryModel() {
    }

    public ItineraryModel(CustomLatLng origintLatLng,
                          String originAddressName,
                          CustomLatLng destinationLatLng,
                          String destinationAddressName,
                          CustomLatLng start_mid_point,
                          CustomLatLng end_mid_point,
                          CustomLatLng midPointLatLng,
                          CustomLatLng selectedBusiness,
                          String selectedBusinessAddressName,
                          String selectedBusinessName,
                          String userID) {
        this.origintLatLng = origintLatLng;
        this.originAddressName = originAddressName;
        this.destinationLatLng = destinationLatLng;
        this.destinationAddressName = destinationAddressName;
        this.start_mid_point = start_mid_point;
        this.end_mid_point = end_mid_point;
        this.midPointLatLng = midPointLatLng;
        this.selectedBusiness = selectedBusiness;
        this.selectedBusinessAddressName = selectedBusinessAddressName;
        this.selectedBusinessName = selectedBusinessName;
        this.userID = userID;

    }


    @NonNull
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
    public CustomLatLng getOrigintLatLng() {
        return origintLatLng;
    }

    public String getOriginAddressName() {
        return originAddressName;
    }

    public CustomLatLng getDestinationLatLng() {
        return destinationLatLng;
    }

    public String getDestinationAddressName() {
        return destinationAddressName;
    }

    public CustomLatLng getStart_mid_point() {
        return start_mid_point;
    }

    public CustomLatLng getEnd_mid_point() {
        return end_mid_point;
    }

    public CustomLatLng getMidPointLatLng() {
        return midPointLatLng;
    }

    public CustomLatLng getSelectedBusiness() {
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
    public void setOrigintLatLng(CustomLatLng origintLatLng) {
        this.origintLatLng = origintLatLng;
    }

    public void setOriginAddressName(String originAddressName) {
        this.originAddressName = originAddressName;
    }

    public void setDestinationLatLng(CustomLatLng destinationLatLng) {
        this.destinationLatLng = destinationLatLng;
    }

    public void setDestinationAddressName(String destinationAddressName) {
        this.destinationAddressName = destinationAddressName;
    }

    public void setStart_mid_point(CustomLatLng start_mid_point) {
        this.start_mid_point = start_mid_point;
    }

    public void setEnd_mid_point(CustomLatLng end_mid_point) {
        this.end_mid_point = end_mid_point;
    }

    public void setMidPointLatLng(CustomLatLng midPointLatLng) {
        this.midPointLatLng = midPointLatLng;
    }

    public void setSelectedBusiness(CustomLatLng selectedBusiness) {
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
