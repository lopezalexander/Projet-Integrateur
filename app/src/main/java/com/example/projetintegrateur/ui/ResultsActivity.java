package com.example.projetintegrateur.ui;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.AppTheme;
import com.example.projetintegrateur.model.DirectionResponse;
import com.example.projetintegrateur.model.ItineraryModel;
import com.example.projetintegrateur.model.directionAPI.Leg;
import com.example.projetintegrateur.model.directionAPI.Route;
import com.example.projetintegrateur.model.directionAPI.Step;
import com.example.projetintegrateur.util.CustomLatLng;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ResultsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    //THE ITINERARY OBJECT TO GET OUR DATA
    ItineraryModel itineraryData;

    //GOOGLE MAPS SETUP
    private static GoogleMap mMap;

    //LISTS//VARIABLES
    private ArrayList<Marker> markerArrayList;
    LatLng midPointLatLng;
    ImageView btn_MapCurrentLocation_GPS;
    DirectionResponse directionResponseAddressA;
    DirectionResponse directionResponseAddressB;

    //UTILS
    ObjectMapper mapper;


    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //INITIALIZE NECESSARY VARIABLE, LIST, ETC
        initView();

        //INITIALIZE THE MAP
        initMap();


    }

    //***********\\
    //  OnStart  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onStart() {
        super.onStart();



    }



    //
    //
    //  CALLED WHEN MAP IS READY TO USE, PROCESS ALL CODE HERE
    //*****************************************************************************************************************************
    @SuppressLint("PotentialBehaviorOverride")
    private void mapIsReady() {

        //ADD ORIGIN MARKER
        addMarker(new LatLng(itineraryData.getOrigintLatLng().getLatitude(), itineraryData.getOrigintLatLng().getLongitude()), "AddressA");

        //ADD DESTINATION MARKER
        addMarker(new LatLng(itineraryData.getDestinationLatLng().getLatitude(), itineraryData.getDestinationLatLng().getLongitude()), "AddressB");

        //ADD SELECTED BUSINESS MARKER
        addMarker(new LatLng(itineraryData.getSelectedBusiness().getLatitude(), itineraryData.getSelectedBusiness().getLongitude()), "selectedBusiness");

        //CENTER CAMERA ON ALL MARKER
        centerAllMarkers();

        //DRAW FIRST POLYLINE ORIGIN TO SELECTED BUSINESS
        drawPolyline(itineraryData.getOrigintLatLng(), itineraryData.getSelectedBusiness(), "AddressA");

        //DRAW SECOND POLYLINE DESTINATION TO SELECTED BUSINESS
        drawPolyline(itineraryData.getDestinationLatLng(), itineraryData.getSelectedBusiness(), "AddressB");


    }


    //
    //
    //
    //
    //
    //
    //
    //
    //
    //*****************************\\
    //      SETUP FUNCTIONS         \\
    //******************************************************************************************************************************************************************************

    //
    //
    // INITIATE THE VIEW
    //*****************************************************************************************************************************
    private void initView() {
        //RETRIEVE ITINERARY DATA
        Intent intent = getIntent();
        itineraryData = intent.getParcelableExtra("selectedHistory");

        //INITIALIZE LIST
        markerArrayList = new ArrayList<>();

        //UTILS --> ObjectMapper to Map JSON response to Model Class
        //*************************************************************************************************
        mapper = new ObjectMapper();

        // SET OnClickListener to MAP_GPS Button to center on markers
        //*************************************************************************************************
        btn_MapCurrentLocation_GPS = findViewById(R.id.ic_gps);
        btn_MapCurrentLocation_GPS.setOnClickListener(view -> {
            //CENTER ON MARKERS
            centerAllMarkers();
        });
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Initializes the maps system and the view.
        if (mapFragment != null) {
            mapFragment.getMapAsync(ResultsActivity.this);
        }
    }

    //
    //
    //  GETS CALLED WHEN MAP IS READY TO BE LOADED
    //*****************************************************************************************************************************
    @SuppressLint("PotentialBehaviorOverride")
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Instantiate GoogleMap
        mMap = googleMap;

        //SET STYLE FOR THE MAP
//        setMapStyle("Midnight", ResultsActivity.this);

        //Disables the native button for getting current location, we will need to create
        //our own, because we need to add the seach bar
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().isCompassEnabled();
        mMap.getUiSettings().isRotateGesturesEnabled();


        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnInfoWindowClickListener(this);

        //NEED THE MAP TO BE LOADED TO ADD MARKERS AND USE MAP FUNCTIONS
        mMap.setOnMapLoadedCallback(this::mapIsReady);

        //Get Theme Signleton
        AppTheme currentTheme = AppTheme.getInstance();
        //Set search bar background color
        setMapStyle(currentTheme.getTheme());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.508888,-73.561668),13.5f));
    }


    //
    //
    //SET MAP STYLE
    //*****************************************************************************************************************************
    public void setMapStyle(String mapStyle) {
        switch (mapStyle) {
            case "Muted Blue":
                mMap.setMapStyle(new MapStyleOptions(getString(R.string.mapThemeMutedBlue)));
                break;
            case "Midnight":
                mMap.setMapStyle(new MapStyleOptions(getString(R.string.mapThemeMidnight)));
                break;
            case "Black and White":
                mMap.setMapStyle(new MapStyleOptions(getString(R.string.mapThemeBlackAndWhite)));
                break;
            case "Ultra Light":
                mMap.setMapStyle(new MapStyleOptions(getString(R.string.mapThemeultraLight)));
                break;
            case "Blue Essence":
                mMap.setMapStyle(new MapStyleOptions(getString(R.string.mapThemeBlueEssence)));
                break;
            case "Default Map":
                mMap.setMapStyle(new MapStyleOptions("[]"));
                break;
        }
        //SET STYLE FOR THE MAP

    }


    //
    //
    //
    //
    //
    //
    //
    //
    //
    //*********************\\
    //  MAP FUNCTIONS       \\
    //******************************************************************************************************************************************************************************

    //
    //
    //  ADD MARKER TO A LOCATION/COORDINATE
    //*****************************************************************************************************************************
    private void addMarker(LatLng latLng, String title) {
        MarkerOptions markerOptions;

        if (!title.equals("selectedBusiness")) {
            if (markerArrayList.size() == 1) {
                markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person5));
            } else {
                markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person6));
            }
        } else {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_middle_point));
        }
        //Add the new marker to the map
        //Add the new marker to the markerArrayList
        markerArrayList.add(mMap.addMarker(markerOptions));

    }

    //
    //  CENTER THE MAP SO THE VIEW INCLUDE ALL MARKERS WITH A PADDING OF 300 px
    //*****************************************************************************************************************************
    private void centerAllMarkers() {
        //Create Latlng Bounds Builder
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //Add Markers Positions in Builder
        for (Marker marker : markerArrayList) {
            builder.include(marker.getPosition());
        }
        //Create Latlng Bounds
        LatLngBounds bounds = builder.build();

        // offset from edges of the map in pixels
        int padding = 300;

        CameraUpdate cameraUpdateu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cameraUpdateu);
    }

    //
    //  DRAW POLYLINES
    //*****************************************************************************************************************************
    private void drawPolyline(CustomLatLng addressLatLng, CustomLatLng selectedBusinessLatLng, String addressType) {
        //SET ORIGIN STRING
        String originCoordinate = addressLatLng.getLatitude() + "," + addressLatLng.getLongitude();

        //SET DESTINATION STRING
        String destinationCoordinate = selectedBusinessLatLng.getLatitude() + "," + selectedBusinessLatLng.getLongitude();

        //BUILD URL STRING
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("origin", originCoordinate)
                .appendQueryParameter("destination", destinationCoordinate)
                .appendQueryParameter("mode", "walking")
                .appendQueryParameter("key", getString(R.string.maps_key))
                .toString();


        //MAKE THE REQUEST TO DIRECTION API
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        //HANDLE THE RESPONSE IN THIS CALLBACK()
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        //CREATE JSON OBJECT WITH RESPONSE
                        JSONObject resultJSON = new JSONObject(Objects.requireNonNull(response.body()).string());


                        //TRANSFORM OUR JSON RESPONSE TO AN DirectionResponse Object, which we can use later if needed and easier to traverse
                        DirectionResponse directionResponseObject = mapper.readValue(resultJSON.toString(), DirectionResponse.class);


                        if (addressType.equals("AddressA")) {
                            directionResponseAddressA = directionResponseObject;
                        } else if (addressType.equals("AddressB")) {
                            directionResponseAddressB = directionResponseObject;
                        }

                        //EXTRACT ROUTE OBJECT -- LEGS ARRAY -- STEPS ARRAY
                        Route routeObject = directionResponseObject.getRoutes().get(0);
                        Leg legsArray = routeObject.getLegs().get(0);
                        ArrayList<Step> stepssArray = legsArray.getSteps();


                        //EXTRACT DISTANCES BETWEEN ORIGIN/DESTINATION AND POYLINE STRING
                        int total_distance_value = legsArray.getDistance().getValue();
                        String polyline = routeObject.getOverview_polyline().getPoints();

                        //SET COUNTER TO IDENTIFY INDEX OF THE STEPS WE NEED THE DATA
                        int distanceCounter = 0;
                        boolean notPassed = true;


                        //ITERATE THROUGH TO CALCULATE MIDDLE DISTANCE POINT
                        for (int i = 0; i < stepssArray.size(); i++) {
                            //GET DISTANCE OF STEPS, THIS WILL BE THE INDEX AT WHICH IT WILL BE OVER THE MIDDLE DISTANCE POINT
                            int step_distance_value_over = stepssArray.get(i).getDistance().getValue();

                            //ADD DISTANCE TO THE COUNTER TO VERIFY AGAINST TOTAL DISTANCE OF ROUTE
                            distanceCounter += step_distance_value_over;

                            //CHECK IF WE WENT OVER THE MIDDLE DISTANCE POINT
                            if ((distanceCounter >= total_distance_value / 2) && notPassed) {
                                //WE NEED TAKE INTO ACCOUNT THAT IF THE LAST STEP IS THE ONE GOING OVER THE MID DISTANCE POINT,
                                // THERE IS NO ENTRY OF .get(i + 1) IN THE ARRAYLIST. HENCE, WE TAKE THE LAST STEP AS THE MIDDLE POINT
                                if (i != (stepssArray.size() - 1)) {
                                    //GET LatLng FOR THE STEPS THAT STEPPED OVER THE MIDDLE DISTANCE POINT
                                    double latOver = stepssArray.get(i).getStart_location().getLat();
                                    double lngOver = stepssArray.get(i).getStart_location().getLng();

                                    //GET LatLng FOR THE STEPS RIGHT AFTER THE MIDDLE DISTANCE POINT
                                    double latAfter = stepssArray.get(i + 1).getStart_location().getLat();
                                    double lngAfter = stepssArray.get(i + 1).getStart_location().getLng();


                                    //DEFINE [START] AND [END] LatLng REFERENCE FOR THE MIDDLE DISTANCE POINT
                                    LatLng start_mid_point = new LatLng(latOver, lngOver);
                                    LatLng end_mid_point = new LatLng(latAfter, lngAfter);


                                    //CALCULATE MIDDLE FROM THE REFERENCE ABOVE
                                    midPointLatLng = LatLngBounds.builder().include(start_mid_point).include(end_mid_point).build().getCenter();
                                } else {
                                    double midLat = stepssArray.get(i).getStart_location().getLat();
                                    double midLng = stepssArray.get(i).getStart_location().getLng();
                                    midPointLatLng = new LatLng(midLat, midLng);
                                }
                                //WE FOUND THE MIDDLE POINT, SET TO FALSE TO NOT ENTER THE IF() AGAIN
                                notPassed = false;
                            }//END FORLOOP CHECK DISTANCE
                        }//END ITERATE THROUGH STEPS


                        //CREATE A Runnable, TO GET INTO THE MAIN THREAD TO HANDLE UI CHANGES
                        ResultsActivity.this.runOnUiThread(() -> {
                            //DECODE POLYLINE
                            List<LatLng> polylineList = PolyUtil.decode(polyline);

                            //DRAW POLYLINE ON MAP
                            if (addressType.equals("AddressA")) {
                                mMap.addPolyline(new PolylineOptions()
                                        .clickable(true)
                                        .width(15)
                                        .color(getColor(R.color.blue))
                                        .addAll(polylineList));
                            } else if (addressType.equals("AddressB")) {
                                mMap.addPolyline(new PolylineOptions()
                                        .clickable(true)
                                        .width(15)
                                        .color(getColor(R.color.green))
                                        .addAll(polylineList));
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }
        }); //END  [REQUEST-RESPONSE]
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

    }



    //
    //
    //
    //
    //
    //
    //
    //
    //
    //*******************************************\\
    //  CLASSES FOR MARKER CONTENT ON CLICK       \\
    //******************************************************************************************************************************************************************************
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;
        private final View mContents;

        @SuppressLint("InflateParams")
        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);


        }

        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            render(marker, mWindow);
            return mWindow;
//              return null;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
//            render(marker, mContents);
//            return mContents;
            return null;
        }

        private void render(Marker marker, View view) {
            // SET DATA HERE TO VIEW ELEMENTS
            TextView markerName = view.findViewById(R.id.markerName);
            TextView markerName1 = view.findViewById(R.id.markerName1);
            TextView markerName2 = view.findViewById(R.id.markerName2);

            if (Objects.equals(marker.getTitle(), "AddressA")) {
                markerName.setText(marker.getTitle());
                markerName1.setText(directionResponseAddressA.getRoutes().get(0).getLegs().get(0).getStart_address());
                markerName2.setText(directionResponseAddressA.getRoutes().get(0).getLegs().get(0).getStart_address());
            } else if (Objects.equals(marker.getTitle(), "AddressB")) {
                markerName.setText(marker.getTitle());
                markerName1.setText(directionResponseAddressB.getRoutes().get(0).getLegs().get(0).getStart_address());
                markerName2.setText(directionResponseAddressB.getRoutes().get(0).getLegs().get(0).getStart_address());
            } else {
                markerName.setText(marker.getTitle());
                markerName1.setText("");
                markerName2.setText("");

            }


        }
    }





}//END RESULTS ACTIVITY  //=========================================================================================================================================================

