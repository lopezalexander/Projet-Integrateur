package com.example.projetintegrateur.util;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomLatLng implements Parcelable {


    double latitude;
    double longitude;


    //**************\\
    //  CONSTRUCTOR  \\
    //*****************************************************************************************************************************
    public CustomLatLng() {

    }

    public CustomLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

    }


    //**************\\
    //  GETTER       \\
    //*****************************************************************************************************************************
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    //**************\\
    //  SETTER       \\
    //*****************************************************************************************************************************
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    //******************\\
    //  PARCELABLE       \\
    //*****************************************************************************************************************************
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }


    protected CustomLatLng(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<CustomLatLng> CREATOR = new Creator<CustomLatLng>() {
        @Override
        public CustomLatLng createFromParcel(Parcel in) {
            return new CustomLatLng(in);
        }

        @Override
        public CustomLatLng[] newArray(int size) {
            return new CustomLatLng[size];
        }
    };

}
