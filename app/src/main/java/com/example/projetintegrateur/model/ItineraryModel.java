package com.example.projetintegrateur.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.projetintegrateur.util.CustomLatLng;

public class ItineraryModel implements Parcelable {

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


    String currentDate;


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
                          String userID,
                          String currentDate) {
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
        this.currentDate = currentDate;

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
                ", currentDate='" + currentDate + '\'' +
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

    public String getCurrentDate() {
        return currentDate;
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

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }


    //******************\\
    //  PARCELABLE       \\
    //*****************************************************************************************************************************
    protected ItineraryModel(Parcel in) {
        origintLatLng = in.readParcelable(CustomLatLng.class.getClassLoader());
        originAddressName = in.readString();
        destinationLatLng = in.readParcelable(CustomLatLng.class.getClassLoader());
        destinationAddressName = in.readString();
        start_mid_point = in.readParcelable(CustomLatLng.class.getClassLoader());
        end_mid_point = in.readParcelable(CustomLatLng.class.getClassLoader());
        midPointLatLng = in.readParcelable(CustomLatLng.class.getClassLoader());
        selectedBusiness = in.readParcelable(CustomLatLng.class.getClassLoader());
        selectedBusinessAddressName = in.readString();
        selectedBusinessName = in.readString();
        userID = in.readString();
        currentDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(origintLatLng, flags);
        dest.writeString(originAddressName);
        dest.writeParcelable(destinationLatLng, flags);
        dest.writeString(destinationAddressName);
        dest.writeParcelable(start_mid_point, flags);
        dest.writeParcelable(end_mid_point, flags);
        dest.writeParcelable(midPointLatLng, flags);
        dest.writeParcelable(selectedBusiness, flags);
        dest.writeString(selectedBusinessAddressName);
        dest.writeString(selectedBusinessName);
        dest.writeString(userID);
        dest.writeString(currentDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ItineraryModel> CREATOR = new Creator<ItineraryModel>() {
        @Override
        public ItineraryModel createFromParcel(Parcel in) {
            return new ItineraryModel(in);
        }

        @Override
        public ItineraryModel[] newArray(int size) {
            return new ItineraryModel[size];
        }
    };
}
