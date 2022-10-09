package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.BusinessModel;
import com.example.projetintegrateur.model.DirectionResponse;
import com.example.projetintegrateur.model.ItineraryModel;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.model.directionAPI.Leg;
import com.example.projetintegrateur.model.directionAPI.Route;
import com.example.projetintegrateur.model.directionAPI.Step;
import com.example.projetintegrateur.model.NearbyBusinessResponse;
import com.example.projetintegrateur.util.CustomLatLng;
import com.example.projetintegrateur.util.UserClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, BusinessDialog.DataTransferInterfaceDialog {
    private final String TAG = "debug";

    //Dynamic List of LatLng from SearchBar
    private ArrayList<LatLng> locationArrayList;
    private ArrayList<String> locationAddressName;
    private ArrayList<Marker> markerArrayList;

    //Place API Autocomplete
    AutocompleteSupportFragment autocompleteFragment;

    //FIREBASE
    public FirebaseAuth mAuth;
    public FirebaseDatabase mFirebaseDB;

    //SEARCH VARIABLE, NEEDED TO STORE IN DB 
    LatLng midPointLatLng; // middle distance point
    LatLng origintLatLng; //address A ou 1
    LatLng destinationLatLng; // Address B ou 2
    LatLng start_mid_point; //Testing purpopses for now
    LatLng end_mid_point;   //Testing purpopses for now
    LatLng selectedBusinessCoordinate;
    String selectedBusinessAddressName;
    String selectedBusinessName;
    Polyline mPolyline;
    Circle mCircle;
    Marker mMarker;
    ItineraryModel ItineraryToAdd;
    ArrayList<NearbyBusinessResponse> allNearbyBusinessResponseList; //Not added yet to Firebase

    //GOOGLE MAPS SETUP
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static GoogleMap mMap;

    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 13.5f;

    //VIEW
    ImageView profil;
    ImageView btn_SearchBar_GPS;
    ImageView btn_MapCurrentLocation_GPS;
    ImageView btn_resetSearchBar;
    ImageView btn_showBusinessList;
    BusinessDialog businessDialog;


    //UTILS
    ObjectMapper mapper;
    LoginDialog loginDialog;


    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Instantiate View Elements, needed for setUpPlacesAutocomplete and others steps afterward
        locationArrayList = new ArrayList<>();
        locationAddressName = new ArrayList<>();
        markerArrayList = new ArrayList<>();


        if (isServicesOK()) {
            //GET PERMISSION
            getLocationPermission();

            //SETUP PLACES AUTOCOMPLETION
            setUpPlacesAutocomplete();

            //SET VIEW BUTTON, FIREBASE, etc
            initView();

            //CHECK IF User is already Connected or Display Login Dialog
            checkUserAuth();
        }
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
    //  GET CURRENT LOCATION AND MOVE CAMERA TO LOCATION
    //*****************************************************************************************************************************
    private void getCurrentLocation() {


//        Log.d(TAG, "2.A) getDeviceLocation: getting the devices current location FROM MAP GPS BUTTON");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task<Location> location = fusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        //Get result to find currentLocation
                        Location currentLocation = task.getResult();

                        if (currentLocation != null) {
                            //Set LatLng
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            //MoveCamera to LatLng
                            moveCamera(latLng, DEFAULT_ZOOM);
                        } else {
                            String err = "unable to get current location";
                            Toast.makeText(MapsActivity.this, err, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        String err = "unable to get current location";
                        Toast.makeText(MapsActivity.this, err, Toast.LENGTH_SHORT).show();
                    }
                });


            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    //
    //
    //  GET CURRENT LOCATION && ADD MARKER && MOVE CAMERA TO IT
    //*****************************************************************************************************************************
    private void getSearchBarCurrentLocation() {
        final LatLng[] latLng = new LatLng[1];
//        Log.d(TAG, "2.B) getDeviceCoordinates: getting the devices current location FROM SEARCH BAR GPS BUTTON");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task<Location> location = fusedLocationProviderClient.getLastLocation();


                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
//                        Log.d(TAG, "3) onComplete: found location!");

                        //Get result to find currentLocation
                        Location currentLocation = task.getResult();
                        if (currentLocation != null) {
                            //Set LatLng
                            latLng[0] = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            //Add to locationArrayList
                            locationArrayList.add(latLng[0]);

                            //TODO:: ADD THE PHYSICAL ADDRESS NAME HERE , NEED TO MAKE A SEARCH TO PLACE API
                            locationAddressName.add("");

                            //CACHER LA BARRE DE RECHERCHE QUAND IL Y A 2 ADRESSES
                            if (locationArrayList.size() == 2) {

                                //INITIATE LOGIC FOR SEARCH RESULTS
                                try {
                                    //FIND MIDDLE DISTANCE POINT
                                    findMiddleDistancePoint();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            //MoveCamera to LatLng && Add Marker
                            moveCamera(latLng[0], DEFAULT_ZOOM);
                            addMarker(latLng[0], "Current Location");
                        } else {
                            String err = "unable to get current location";
                            Toast.makeText(MapsActivity.this, err, Toast.LENGTH_SHORT).show();
                        }
                    } else {
//                        Log.d(TAG, "3) onComplete: current location is null");
                        String err = "unable to get current location";
                        Toast.makeText(MapsActivity.this, err, Toast.LENGTH_SHORT).show();
                    }
                });

            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    //
    //
    //  MOVE CAMERO TO A LOCATION/COORDINATE
    //*****************************************************************************************************************************
    private void moveCamera(LatLng latLng, float zoom) {
//        Log.d(TAG, "4) moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    //
    //
    //  ADD MARKER TO A LOCATION/COORDINATE
    //*****************************************************************************************************************************
    private void addMarker(LatLng latLng, String title) {
        MarkerOptions markerOptions;
        Marker marker;

        if (!title.equals("MidPoint_Fine")) {
            if (markerArrayList.size() == 1) {
                markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person5));
                marker = mMap.addMarker(markerOptions);
                //Add the new marker to the markerArrayList
                markerArrayList.add(marker);
            } else {
                markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person6));
                marker = mMap.addMarker(markerOptions);
                //Add the new marker to the markerArrayList
                markerArrayList.add(marker);
            }
        } else {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_middle_point));
            mMarker = mMap.addMarker(markerOptions);
            //Add the new marker to the markerArrayList
            markerArrayList.add(mMarker);
        }


        //Clear the Search Bar text
        autocompleteFragment.setText("");


        setHints();
        hideSoftKeyboard();
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

        int padding = 300; // offset from edges of the map in pixels

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
    }

    //
    //
    //  FIND THE MIDDLE DISTANCE POINT FOR THE SEARCH ADDRESSES
    //*****************************************************************************************************************************
    private void findMiddleDistancePoint() throws IOException {

        //SET ORIGIN STRING
        origintLatLng = locationArrayList.get(0);
        String originCoordinate = origintLatLng.latitude + "," + origintLatLng.longitude;

        //SET DESTINATION STRING
        destinationLatLng = locationArrayList.get(1);
        String destinationCoordinate = destinationLatLng.latitude + "," + destinationLatLng.longitude;

        //BUILD URL STRING
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("origin", originCoordinate)
                .appendQueryParameter("destination", destinationCoordinate)
                .appendQueryParameter("mode", "driving")
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
                                    start_mid_point = new LatLng(latOver, lngOver);
                                    end_mid_point = new LatLng(latAfter, lngAfter);


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
                        MapsActivity.this.runOnUiThread(() -> {
                            //DECODE POLYLINE
                            List<LatLng> polylineList = PolyUtil.decode(polyline);

                            //DRAW POLYLINE ON MAP
                            mPolyline = mMap.addPolyline(new PolylineOptions()
                                    .clickable(true)
                                    .width(15)
                                    .color(getColor(R.color.blue6))
                                    .addAll(polylineList));

                            //ADD MIDDLE DISTANCE POINT MARKER
                            addMarker(midPointLatLng, "MidPoint_Fine");

                            //ADD CIRCLE AROUND MIDPOINT
                            mCircle = mMap.addCircle(new CircleOptions()
                                    .center(midPointLatLng)
                                    .radius(1000)
                                    .strokeColor(getColor(R.color.red)));
                            mCircle.setVisible(true);

                            //MOVE THE CAMERA ONTO THE MIDPOINT
                            moveCamera(midPointLatLng, 13);
                            centerAllMarkers();

                        });

                        //QUERY LIST OF AVENUES AROUND MIDDLE DISTANCE POINT
                        getNearbyBusiness();

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

    //
    //
    //  GET NEARBY BUSINESS AROUND MIDDLE POINT
    //*****************************************************************************************************************************
    private void getNearbyBusiness() {
        //BUILD URL STRING
        String midPointLatLng_String = midPointLatLng.latitude + "," + midPointLatLng.longitude;

        String url = Uri.parse("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .buildUpon()
                .appendQueryParameter("location", midPointLatLng_String)
                .appendQueryParameter("radius", "1000")
                .appendQueryParameter("type", "restaurant")
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
                        JSONObject respJSON = new JSONObject(Objects.requireNonNull(response.body()).string());

                        String resultStatus = respJSON.getString("status");
                        if (!resultStatus.equals("ZERO_RESULTS")) {
                            //ONLY RETRIEVE THE Results Array in the response to convert to Model Class NearbySearch
                            JSONArray results = respJSON.getJSONArray("results");

                            //INSTANTIATE OUR ARRAYLIST OF Nearby Business
                            allNearbyBusinessResponseList = new ArrayList<>();

                            //ADD ALL FOUND BUSINESS TO THE ARRAYLIST OF Nearby Business
                            for (int i = 0; i < results.length(); i++) {
                                NearbyBusinessResponse uniqueBusinnes = mapper.readValue(results.getJSONObject(i).toString(), NearbyBusinessResponse.class);
                                allNearbyBusinessResponseList.add(uniqueBusinnes);
                            }


                            // DISPLAY THE BUSINESS LIST ON A DIALOG FOR USER TO CHOOSE FROM
                            //******************************************************************

                            //CREATE ARRAYLIST OF BusinessModel TO SUPPLY TO THE RECYCLERVIEW
                            ArrayList<BusinessModel> recyclerBusinessList = new ArrayList<>();
                            for (NearbyBusinessResponse uniqueBusiness : allNearbyBusinessResponseList) {
                                //CREATE UNIQUE BusinessModel OBJECT
                                BusinessModel business = new BusinessModel();

                                //SET NAME AND ADDRESS
                                business.setName(uniqueBusiness.getName());
                                business.setAddress(uniqueBusiness.getVicinity());
                                business.setRating(String.valueOf(uniqueBusiness.getUser_ratings_total()));
                                business.setCoordinatesLatlng(new LatLng(uniqueBusiness.getGeometry().getLocation().getLat(), uniqueBusiness.getGeometry().getLocation().getLng()));
                                Log.d(TAG, "onResponse: "+uniqueBusiness);
                                if (uniqueBusiness.getPhotos() != null) {
                                    business.setPhotoURL(uniqueBusiness.getPhotos().get(0).photo_reference);
                                }
                                //ADD BUSINESS TO THE LIST
                                recyclerBusinessList.add(business);
                            }

                            MapsActivity.this.runOnUiThread(() -> {


                                //CREATE THE DIALOG AND SHOW ON THE UI
                                businessDialog = new BusinessDialog(recyclerBusinessList, MapsActivity.this);
                                businessDialog.show(getSupportFragmentManager(), "BusinessDialogFragment");
                                btn_showBusinessList.setVisibility(View.VISIBLE);
                            });


                        } else {
                            MapsActivity.this.runOnUiThread(() -> {
                                //TODO:: RESET EVERYTHING HERE
                                Toast.makeText(MapsActivity.this, "UNABLE TO FULFILL REQUEST, Try a shorter distance!!", Toast.LENGTH_LONG).show();
                            });
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });

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
    //  GETS CALLED WHEN MAP IS READY TO BE LOADED
    //*****************************************************************************************************************************
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Instantiate GoogleMap
        mMap = googleMap;

        setMapStyle("Muted Blue");

        //GET PERMISSION FOR FINE AND COARSE LOCATION --> USED FOR GEOLOCATION
        if (mLocationPermissionsGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            //SET THE BLUE DOT AT THE CURRENT LOCATION
            mMap.setMyLocationEnabled(true);

            //Disables the native button for getting current location, we will need to create
            //our own, because we need to add the seach bar
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().isCompassEnabled();
            mMap.getUiSettings().isRotateGesturesEnabled();

        }

        //SET onClickListener on Map Markers
        //*************************************************************************************************
        mMap.setOnMarkerClickListener(marker -> {

            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        //EFFACER le LatLng de la liste
                        for (int i = 0; i < locationArrayList.size(); i++) {
                            if (locationArrayList.get(i).equals(marker.getPosition())) {
                                locationArrayList.remove(marker.getPosition());
                                String locationAddressToRemove = locationAddressName.get(i);
                                locationAddressName.remove(locationAddressToRemove);
                                markerArrayList.remove(marker);
                                autocompleteFragment.setText("");
                                setHints();
                            }

                        }

                        if (Objects.equals(marker.getTitle(), "MidPoint_Fine")) {
                            mCircle.remove();
                            mPolyline.remove();
                            mMarker = null;
                        } else {
                            //REAFFICHER LA BARRE DE RECHERCHE APRES AVOIR EFFACE UNE ADDRESSE
                            autocompleteFragment.requireView().setVisibility(View.VISIBLE);
                            findViewById(R.id.ic_gps2).setVisibility(View.VISIBLE);
                        }

                        //EFFACER MARKER
                        marker.remove();


                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("êtes-vous sur de supprimer cette adresse?").setPositiveButton("Oui", dialogClickListener)
                    .setNegativeButton("Non", dialogClickListener).show();

            return false;
        });



        mMap.setOnPolylineClickListener(polyline1 -> {


            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //resetRoute();
                        polyline1.remove();
                        mCircle.remove();

                        if (businessDialog.isVisible()) {
                            businessDialog.dismiss();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("êtes-vous sur de supprimer cette itinéraire?").setPositiveButton("Oui", dialogClickListener)
                    .setNegativeButton("Non", dialogClickListener).show();



        });
    }

    public static void setMapStyle(String mapStyle) {
        switch (mapStyle) {
            case "Muted Blue":
                mMap.setMapStyle(new MapStyleOptions("[{\"featureType\":\"all\"," +
                        "\"stylers\":[{\"saturation\":0},{\"hue\":\"#e7ecf0\"}]},{\"featureType" +
                        "\":\"road\",\"stylers\":[{\"saturation\":-70}]},{\"featureType\":" +
                        "\"transit\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType" +
                        "\":\"poi\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":" +
                        "\"water\",\"stylers\":[{\"visibility\":\"simplified\"},{\"saturation\":-60}]}]")
                );
                break;
            case "Midnight":
                mMap.setMapStyle(new MapStyleOptions("[{\"featureType\":\"all\",\n\"elementType\": \"labels.text.fill\",\n" +
                        "\"stylers\": [\n{\n\"color\": \"#ffffff\"\n}\n]\n},\n{\n\"featureType\": \"all\",\n\"elementType\": \"labels.text.stroke\",\n" +
                        "\"stylers\": [\n{\n\"color\": \"#000000\"\n},\n{\n\"lightness\": 13\n}\n]\n},\n{\n\"featureType\": \"administrative\",\n" +
                        "\"elementType\": \"geometry.fill\",\n\"stylers\": [\n{\n\"color\": \"#000000\"\n}\n]\n},\n{\n\"featureType\": \"administrative\",\n" +
                        "\"elementType\": \"geometry.stroke\",\n\"stylers\": [\n{\n    \"color\": \"#144b53\"\n},\n{\n    \"lightness\": 14\n},\n{\n    \"weight\": 1.4\n}\n" +
                        "]\n},\n{\n    \"featureType\": \"landscape\",\n    \"elementType\": \"all\",\n    \"stylers\": [\n{\n    \"color\": \"#08304b\"\n}\n    ]\n},\n{\n    \"featureType\": \"poi\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n{\n    \"color\": \"#0c4152\"\n},\n{\n    \"lightness\": 5\n}\n    ]\n},\n{\n    \"featureType\": \"road.highway\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n{\n    \"color\": \"#000000\"\n}\n    ]\n},\n{\n    \"featureType\": \"road.highway\",\n    \"elementType\": \"geometry.stroke\",\n    \"stylers\": [\n{\n    \"color\": \"#0b434f\"\n},\n{\n    \"lightness\": 25\n}\n    ]\n},\n{\n    \"featureType\": \"road.arterial\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n{\n    \"color\": \"#000000\"\n}\n    ]\n},\n{\n    \"featureType\": \"road.arterial\",\n    \"elementType\": \"geometry.stroke\",\n    \"stylers\": [\n{\n    \"color\": \"#0b3d51\"\n},\n{\n    \"lightness\": 16\n}\n    ]\n},\n{\n    \"featureType\": \"road.local\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n{\n    \"color\": \"#000000\"\n}\n    ]\n},\n{\n    \"featureType\": \"transit\",\n    \"elementType\": \"all\",\n    \"stylers\": [\n{\n    \"color\": \"#146474\"\n}\n    ]\n},\n{\n    \"featureType\": \"water\",\n    \"elementType\": \"all\",\n    \"stylers\": [\n{\n    \"color\": \"#021019\"\n}\n    ]\n}\n" +
                        "]"));
                break;
            case "Black and White":
                mMap.setMapStyle(new MapStyleOptions("[\n{\n    \"featureType\": \"road\",\n    \"elementType\": \"labels\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        }\n    ]\n},\n{\n    \"featureType\": \"poi\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"featureType\": \"administrative\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"featureType\": \"road\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n        {\n            \"color\": \"#000000\"\n        },\n        {\n            \"weight\": 1\n        }\n    ]\n},\n{\n    \"featureType\": \"road\",\n    \"elementType\": \"geometry.stroke\",\n    \"stylers\": [\n        {\n            \"color\": \"#000000\"\n        },\n        {\n            \"weight\": 0.8\n        }\n    ]\n},\n{\n    \"featureType\": \"landscape\",\n    \"stylers\": [\n        {\n            \"color\": \"#ffffff\"\n        }\n    ]\n},\n{\n    \"featureType\": \"water\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"featureType\": \"transit\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"elementType\": \"labels\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.text\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.text.stroke\",\n    \"stylers\": [\n        {\n            \"color\": \"#ffffff\"\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.text.fill\",\n    \"stylers\": [\n        {\n            \"color\": \"#000000\"\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.icon\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        }\n    ]\n}\n" +
                        "]"));
                break;
            case "Ultra Light":
                mMap.setMapStyle(new MapStyleOptions("[\n{\n    \"featureType\": \"water\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#e9e9e9\"\n        },\n        {\n            \"lightness\": 17\n        }\n    ]\n},\n{\n    \"featureType\": \"landscape\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#f5f5f5\"\n        },\n        {\n            \"lightness\": 20\n        }\n    ]\n},\n{\n    \"featureType\": \"road.highway\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n        {\n            \"color\": \"#ffffff\"\n        },\n        {\n            \"lightness\": 17\n        }\n    ]\n},\n{\n    \"featureType\": \"road.highway\",\n    \"elementType\": \"geometry.stroke\",\n    \"stylers\": [\n        {\n            \"color\": \"#ffffff\"\n        },\n        {\n            \"lightness\": 29\n        },\n        {\n            \"weight\": 0.2\n        }\n    ]\n},\n{\n    \"featureType\": \"road.arterial\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#ffffff\"\n        },\n        {\n            \"lightness\": 18\n        }\n    ]\n},\n{\n    \"featureType\": \"road.local\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#ffffff\"\n        },\n        {\n            \"lightness\": 16\n        }\n    ]\n},\n{\n    \"featureType\": \"poi\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#f5f5f5\"\n        },\n        {\n            \"lightness\": 21\n        }\n    ]\n},\n{\n    \"featureType\": \"poi.park\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#dedede\"\n        },\n        {\n            \"lightness\": 21\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.text.stroke\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        },\n        {\n            \"color\": \"#ffffff\"\n        },\n        {\n            \"lightness\": 16\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.text.fill\",\n    \"stylers\": [\n        {\n            \"saturation\": 36\n        },\n        {\n            \"color\": \"#333333\"\n        },\n        {\n            \"lightness\": 40\n        }\n    ]\n},\n{\n    \"elementType\": \"labels.icon\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"featureType\": \"transit\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"color\": \"#f2f2f2\"\n        },\n        {\n            \"lightness\": 19\n        }\n    ]\n},\n{\n    \"featureType\": \"administrative\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n        {\n            \"color\": \"#fefefe\"\n        },\n        {\n            \"lightness\": 20\n        }\n    ]\n},\n{\n    \"featureType\": \"administrative\",\n    \"elementType\": \"geometry.stroke\",\n    \"stylers\": [\n        {\n            \"color\": \"#fefefe\"\n        },\n        {\n            \"lightness\": 17\n        },\n        {\n            \"weight\": 1.2\n        }\n    ]\n}\n" +
                        "]"));
                break;
            case "Blue Essence":
                mMap.setMapStyle(new MapStyleOptions("[\n{\n    \"featureType\": \"landscape.natural\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        },\n        {\n            \"color\": \"#e0efef\"\n        }\n    ]\n},\n{\n    \"featureType\": \"poi\",\n    \"elementType\": \"geometry.fill\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        },\n        {\n            \"hue\": \"#1900ff\"\n        },\n        {\n            \"color\": \"#c0e8e8\"\n        }\n    ]\n},\n{\n    \"featureType\": \"road\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"lightness\": 100\n        },\n        {\n            \"visibility\": \"simplified\"\n        }\n    ]\n},\n{\n    \"featureType\": \"road\",\n    \"elementType\": \"labels\",\n    \"stylers\": [\n        {\n            \"visibility\": \"off\"\n        }\n    ]\n},\n{\n    \"featureType\": \"transit.line\",\n    \"elementType\": \"geometry\",\n    \"stylers\": [\n        {\n            \"visibility\": \"on\"\n        },\n        {\n            \"lightness\": 700\n        }\n    ]\n},\n{\n    \"featureType\": \"water\",\n    \"elementType\": \"all\",\n    \"stylers\": [\n        {\n            \"color\": \"#7dcdcd\"\n        }\n    ]\n}\n" +
                        "]"));
        }
        //SET STYLE FOR THE MAP

    }

    //
    //
    // Verify if Google Play Service are installed, if not, request to install Google Play Service
    //*****************************************************************************************************************************
    public boolean isServicesOK() {
//        Log.d("TAG", "isServicesOK : checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
//            Log.d(TAG, "isServicesOK : Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //An error occured but we can resolve it
//            Log.d(TAG, "isServicesOK : an error occured but we can fix it");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            Objects.requireNonNull(dialog).show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //
    //
    //  LAUNCH AT OnCREATE TO INSTANTIATE VIEW VARIABLES
    //*****************************************************************************************************************************
    private void initView() {


        //INITIALIZE PLACES API
        //*************************************************************************************************
        Places.initialize(getApplicationContext(), "AIzaSyDR3NrmbrjstWl59Wwy23yjBS3nrp67kT4");

        //FIREBASE
        //*************************************************************************************************
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();

        //UTILS --> ObjectMapper to Map JSON response to Model Class
        //*************************************************************************************************
        mapper = new ObjectMapper();


        // SET OnClickListener to MAP_GPS Button to center on CurrentLocation
        //*************************************************************************************************

        btn_MapCurrentLocation_GPS = findViewById(R.id.ic_gps);
        btn_MapCurrentLocation_GPS.setOnClickListener(view -> {
            //Log.d(TAG, "onClicked: clicked gps icon");

            //Center to CurrentLocation
            getCurrentLocation();
        });


        // SET OnClickListener to SearchBar_GPS Button to add a marker
        //*************************************************************************************************

        btn_SearchBar_GPS = findViewById(R.id.ic_gps2);
        btn_SearchBar_GPS.setOnClickListener(view -> {
            //Log.d(TAG, "onClicked: clicked Search Bar gps icon");

            //Add marker on CurrentLocation
            getSearchBarCurrentLocation();

            //HIDE WHEN USED ONCE
            btn_SearchBar_GPS.setVisibility(View.INVISIBLE);

            //If the locationArrayList has 2 value in it( when the User enters a second address), Remove SearchBar
            if (locationArrayList.size() == 2) {
                autocompleteFragment.requireView().setVisibility(View.GONE);
                btn_SearchBar_GPS.setVisibility(View.GONE);
            }
        });

        // SET OnClickListener TO DISPLAY THE BUSINESS DIALOG AFTER ITS BEEN CLOSED (BTN APPEARS WHEN SEARCH IS DONE)
        //*************************************************************************************************
        btn_showBusinessList = findViewById(R.id.ic_show_listview_btn);
        btn_showBusinessList.setOnClickListener(view -> businessDialog.show(getSupportFragmentManager(), "BusinessDialogFragment"));


        // SET OnClickListener for the PROFILE BUTTON
        //*************************************************************************************************
        profil = findViewById(R.id.ic_perso);
        profil.setOnClickListener(view -> {
            Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // SET OnClickListener to reset search bar and to empty addresses
        //*************************************************************************************************

        btn_resetSearchBar = findViewById(R.id.ic_reset);
        btn_resetSearchBar.setOnClickListener(view -> resetView());


    }

    //
    //
    //  WHEN PERMISSION ARE ALL GRANTED, INITIATE THE MAP
    //*****************************************************************************************************************************
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Initializes the maps system and the view.
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    //
    //
    //  SET THE AUTOCOMPLETE FOR THE SEARCHBAR
    //*****************************************************************************************************************************
    private void setUpPlacesAutocomplete() {
        Places.initialize(getApplicationContext(), getString(R.string.maps_key));

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        //SET SEARCH BAR HINT
        autocompleteFragment.setHint(getString(R.string.search_address_1));

        autocompleteFragment.requireView().setBackgroundColor(getColor(R.color.red3));


        //SET SPECIFIC COUNTRY BASED SEARCH
        autocompleteFragment.setCountries("CA", "US");

        //SET LOCATION BOUNDS FOR BETTER SEARCH RESULTS
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-40, -168),
                new LatLng(71, 136)
        ));


        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Log.d("TEST1", "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());

                //AJOUTER LES COORDONNEES DANS LA LISTE DES LatLng
                locationArrayList.add(place.getLatLng());
                locationAddressName.add(place.getName());

                //CENTER CAMERA ON THE LOCATION ENTERED
                moveCamera(Objects.requireNonNull(place.getLatLng()), DEFAULT_ZOOM);
                addMarker(place.getLatLng(), Objects.requireNonNull(place.getName()));
                centerAllMarkers();

                //CACHER LA BARRE DE RECHERCHE QUAND IL Y A 2 ADRESSES
                if (locationArrayList.size() == 2) {
                    autocompleteFragment.requireView().setVisibility(View.GONE);
                    btn_SearchBar_GPS.setVisibility(View.GONE);

                    //INITIATE LOGIC FOR SEARCH RESULTS
                    try {
                        //FIND MIDDLE DISTANCE POINT
                        findMiddleDistancePoint();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                //Log.d(TAG, "An error occurred: " + status);
            }
        });
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
    //      PERMISSIONS CODE        \\
    //******************************************************************************************************************************************************************************

    //
    //
    //  CHECK IF DEVICE HAVE FINE/COARSE LOCATION PERMISSIONS
    //*****************************************************************************************************************************
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();

                //GET CURRENT LOCATION
                getCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //
    //
    //  CALLED IF PERMISSIONS ARE NOT FOUND
    //*****************************************************************************************************************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.d(TAG, "onRequestPermissionResults: called.");
        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                //CHECK RESULTS --> IF FALSE, RETURN WITHOUT InitMap(), PERMISSION NOT GRANTED
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false;
                        //Log.d(TAG, "onRequestPermissionResults: permission failed.");
                        return;
                    }
                }

                //PERMISSION WERE GRANTED, INITIALIZE THE MAP
                mLocationPermissionsGranted = true;
                //Initialize the Map
                initMap();

                //GET CURRENT LOCATION
                getCurrentLocation();
            }
        }
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
    //      UTILITY FUNCTIONS         \\
    //******************************************************************************************************************************************************************************

    //
    //
    //  CHECK IF USER IS CONNECTED ALREADY, IF NOT, SHOW LOGIN DIALOG
    //*****************************************************************************************************************************
    private void checkUserAuth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            loginDialog = new LoginDialog();
            loginDialog.show(getSupportFragmentManager(), "LoginDialogFragment");
        } else {
            //TODO:: RETRIEVE USER DATA FROM FIREBASE AND RESTORE IT INTO USER SINGLETON
            //THIS IS NEEDED FOR WHEN THE USER CLOSES THE APP AND OPENS IT.. THE SINGLETON USER IS NOT KEPT WHEN THIS FLOW OCCURS,
            // HENCE WE NEED TO QUERY IT BACK FROM THE DATABASE
            //[GET REFERENCE] FOR CURRENT_USER FROM DATABASE WITH currentUserKey
            String currentUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


            //[FETCH] THE USER IN DATABASE
            DatabaseReference ref = mFirebaseDB.getReference("Users").child(currentUserKey);
            ref.get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful()) {
                    //[CREATE] SINGLETON
                    User currentUser2 = task2.getResult().getValue(User.class);
                    ((UserClient) getApplicationContext()).setUser(currentUser2);
                    // Toast
                    Toast.makeText(this, "Welcome to MidWay!!", Toast.LENGTH_LONG).show();
                } else {
                    //HANDLE ERROR HERE if we cannot retrieve the user data
                    Toast.makeText(this, "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                }
            }); //END CREATE SINGLETON
        }


    }

    //
    //
    //  HIDE KEYBOARD ON INPUT TEXT FIELDS
    //*****************************************************************************************************************************
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //
    //
    //SET HINTS FOR SEARCH BAR
    //*****************************************************************************************************************************
    private void setHints() {
        //BASED on the number of location we have in locationArrayList, display the correct SearchBar text or Hide SearchBar
        if (locationArrayList.size() == 0) {
            autocompleteFragment.setHint("Entrez votre addresse");
        } else if (locationArrayList.size() == 1) {
            autocompleteFragment.setHint("Entrez la 2ème addresse");
        } else if (locationArrayList.size() == 2) {
            autocompleteFragment.requireView().setVisibility(View.GONE);
            btn_SearchBar_GPS.setVisibility(View.GONE);
        }
    }

    //
    //
    //RESET THE VIEW FOR ANOTHER SEARCH
    //*****************************************************************************************************************************
    public void resetView() {
        locationArrayList.clear();
        locationAddressName.clear();
        markerArrayList.clear();
        mMap.clear();
        autocompleteFragment.requireView().setVisibility(View.VISIBLE);
        btn_SearchBar_GPS.setVisibility(View.VISIBLE);
        btn_showBusinessList.setVisibility(View.GONE);
        autocompleteFragment.setText("");
        setHints();
    }

    //
    //
    //DATA TRANSFER FROM BUSINESS DIALOG TO MAPSACTIVITY
    //*****************************************************************************************************************************
    @Override
    public void getSelectedBusinnes(LatLng businessCoordinate, String businessAddressName, String businessName) {
        //RETRIEVE BUSINESS DATA INFORMATION FROM RECYCLERVIEW --> BUSINESS DIALOG --> MAPSACTIVITY
        this.selectedBusinessCoordinate = businessCoordinate;
        this.selectedBusinessAddressName = businessAddressName;
        this.selectedBusinessName = businessName;

        //CREATE ITINERARY OBJECT TO SAVE INTO THE DATABASE
        createItineraryModelObject();

        showResult();
    }


    //
    //
    //CREATE THE FINAL ITINERARY OBJECT
    //*****************************************************************************************************************************
    public void createItineraryModelObject() {
        //CREATE ITINERARY OBJECT
        ItineraryToAdd = new ItineraryModel(
                new CustomLatLng(origintLatLng.latitude, origintLatLng.longitude),
                locationAddressName.get(0),
                new CustomLatLng(destinationLatLng.latitude, destinationLatLng.longitude),
                locationAddressName.get(1),
                new CustomLatLng(start_mid_point.latitude, start_mid_point.longitude),
                new CustomLatLng(end_mid_point.latitude, end_mid_point.longitude),
                new CustomLatLng(midPointLatLng.latitude, midPointLatLng.longitude),
                new CustomLatLng(selectedBusinessCoordinate.latitude, selectedBusinessCoordinate.longitude),
                selectedBusinessAddressName,
                selectedBusinessName,
                Objects.requireNonNull(mAuth.getCurrentUser()).getUid()
        );

        //SAVE INTO THE DATABASE
        addItineraryToFirebase(ItineraryToAdd);

    }


    //
    //
    //DATA TRANSFER FROM BUSINESS DIALOG TO MAPSACTIVITY
    //*****************************************************************************************************************************
    public void addItineraryToFirebase(ItineraryModel itineraryToAdd) {

        //GET CURRENT USER KEY
        String currentUserkey = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        //ADD ITINERARY TO FIREBASE
        DatabaseReference newItineraryPush = mFirebaseDB.getReference("Itinerary").child(currentUserkey).push();
        newItineraryPush.setValue(itineraryToAdd, (error, ref) -> Toast.makeText(MapsActivity.this, "Itinerary Saved!", Toast.LENGTH_LONG).show());
        
    }


    //
    //
    //SHOW RESULTS AFTER THE SEARCH AND SELECTED BUSINESS IS DONE
    //*****************************************************************************************************************************
    public void showResult() {
        //REMOVE THE BUSINESS LIST DIALOG
        businessDialog.dismiss();

        //DISABLE BUTTONS FOR RESULT
        btn_showBusinessList.setVisibility(View.GONE);

        //DISPLAY RESULTS
        //TODO:: SHOW POLYLINE FROM A TO MID , B TO MID
        //TODO:: DISPLAY PERTINENT DATA

    }


}//END MAPACTIVITY  //==============================================================================================================================================================