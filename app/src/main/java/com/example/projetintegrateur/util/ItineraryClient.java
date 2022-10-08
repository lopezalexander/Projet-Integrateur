package com.example.projetintegrateur.util;

import android.app.Application;

import com.example.projetintegrateur.model.ItineraryModel;

public class ItineraryClient extends Application {

    ItineraryModel itinerary = null;

    public ItineraryModel getItinerary() {
        return itinerary;
    }

    public void setItinerary(ItineraryModel itinerary) {
        this.itinerary = itinerary;
    }
}
