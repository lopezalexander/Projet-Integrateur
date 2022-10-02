package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
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
import com.example.projetintegrateur.adapter.LoginDialog;
import com.example.projetintegrateur.model.DirectionResponse;
import com.example.projetintegrateur.model.directionAPI.Leg;
import com.example.projetintegrateur.model.directionAPI.Route;
import com.example.projetintegrateur.model.directionAPI.Step;
import com.example.projetintegrateur.model.NearbyBusiness;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private final String TAG = "debug";

    //Dynamic List of LatLng from SearchBar
    private ArrayList<LatLng> locationArrayList;
    private ArrayList<Marker> markerArrayList;

    //Place API Autocomplete
    AutocompleteSupportFragment autocompleteFragment;

    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;

    //SEARCH VARIABLE, NEEDED TO STORE IN DB 
    LatLng midPointLatLng;
    LatLng origintLatLng;
    LatLng destinationLatLng;
    ArrayList<NearbyBusiness> allBusinessList;

    //GOOGLE MAPS SETUP
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private GoogleMap mMap;

    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 13.5f;

    //VIEW
    ImageView btn_SearchBar_GPS;
    ImageView btn_MapCurrentLocation_GPS;

    //UTILS
    ObjectMapper mapper;


    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Instantiate View Elements, needed for setUpPlacesAutocomplete and others steps afterward
        locationArrayList = new ArrayList<>();
        markerArrayList = new ArrayList<>();

        btn_SearchBar_GPS = findViewById(R.id.ic_gps2);
        btn_MapCurrentLocation_GPS = findViewById(R.id.ic_gps);

        if (isServicesOK()) {
            //GET PERMISSION
            getLocationPermission();

            //GET CURRENT LOCATION
            getCurrentLocation();

            //SETUP PLACES AUTOCOMPLETION
            setUpPlacesAutocomplete();

            //SET VIEW BUTTON, FIREBASE, etc
            initView();


            //CHECK IF User is already Connected or Display Login Dialog
//            checkUserAuth();

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
//                        Log.d(TAG, "3) onComplete: found location!");

                        //Get result to find currentLocation
                        Location currentLocation = task.getResult();

                        //Set LatLng
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                        //MoveCamera to LatLng
                        moveCamera(latLng, DEFAULT_ZOOM);
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

                        //Set LatLng
                        latLng[0] = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                        //Add to locationArrayList
                        locationArrayList.add(latLng[0]);

                        //MoveCamera to LatLng && Add Marker
                        moveCamera(latLng[0], DEFAULT_ZOOM);
                        addMarker(latLng[0], "Current Location");
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

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


        //Add the new marker to the markerArrayList
        markerArrayList.add(mMap.addMarker(markerOptions));

        //Clear the Search Bar text
        autocompleteFragment.setText("");


        setHints();
        hideSoftKeyboard();
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
                .appendQueryParameter("key", getString(R.string.maps_key_alex))
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
//                        JSONObject routeObject = resultJSON.getJSONArray("routes").getJSONObject(0);
                        Route routeObject = directionResponseObject.getRoutes().get(0);
//                        JSONArray legsArray = resultJSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                        Leg legsArray = routeObject.getLegs().get(0);
//                        JSONArray stepssArray = resultJSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                        ArrayList<Step> stepssArray = legsArray.getSteps();


                        //EXTRACT DISTANCES BETWEEN ORIGIN/DESTINATION AND POYLINE STRING
//                        int total_distance_value = legsArray.getJSONObject(0).getJSONObject("distance").getInt("value");
                        int total_distance_value = legsArray.getDistance().getValue();
//                        String polyline = routeObject.getJSONObject("overview_polyline").getString("points");
                        String polyline = routeObject.getOverview_polyline().getPoints();

                        //SET COUNTER TO IDENTIFY INDEX OF THE STEPS WE NEED THE DATA
                        int distanceCounter = 0;
                        boolean notPassed = true;


                        //ITERATE THROUGH
                        for (int i = 0; i < stepssArray.size(); i++) {
                            //GET DISTANCE OF STEPS, THIS WILL BE THE INDEX AT WHICH IT WILL BE OVER THE MIDDLE DISTANCE POINT
//                            int step_distance_value_over = stepssArray.getJSONObject(i).getJSONObject("distance").getInt("value");
                            int step_distance_value_over = stepssArray.get(i).getDistance().getValue();

                            //ADD DISTANCE TO THE COUNTER TO VERIFY AGAINST TOTAL DISTANCE OF ROUTE
                            distanceCounter += step_distance_value_over;


                            //CHECK IF WE WENT OVER THE MIDDLE DISTANCE POINT
                            if ((distanceCounter >= total_distance_value / 2) && notPassed) {
                                //GET LatLng FOR THE STEPS THAT STEPPED OVER THE MIDDLE DISTANCE POINT
//                                double latOver = stepssArray.getJSONObject(i).getJSONObject("start_location").getDouble("lat");
                                double latOver = stepssArray.get(i).getStart_location().getLat();
//                                double lngOver = stepssArray.getJSONObject(i).getJSONObject("start_location").getDouble("lng");
                                double lngOver = stepssArray.get(i).getStart_location().getLng();

                                //GET LatLng FOR THE STEPS RIGHT AFTER THE MIDDLE DISTANCE POINT
//                                double latAfter = stepssArray.getJSONObject(i + 1).getJSONObject("start_location").getDouble("lat");
                                double latAfter = stepssArray.get(i + 1).getStart_location().getLat();
//                                double lngAfter = stepssArray.getJSONObject(i + 1).getJSONObject("start_location").getDouble("lng");
                                double lngAfter = stepssArray.get(i + 1).getStart_location().getLng();

                                //DEFINE [START] AND [END] LatLng REFERENCE FOR THE MIDDLE DISTANCE POINT
                                LatLng start_mid_point = new LatLng(latOver, lngOver);
                                LatLng end_mid_point = new LatLng(latAfter, lngAfter);

                                //CALCULATE MIDDLE FROM THE REFERENCE ABOVE
                                midPointLatLng = LatLngBounds.builder().include(start_mid_point).include(end_mid_point).build().getCenter();

                                //WE FOUND THE MIDDLE POINT, SET TO FALSE TO NOT ENTER THE IF() AGAIN
                                notPassed = false;
                            }//END FORLOOP CHECK DISTANCE
                        }//END ITERATE THROUGH STEPS


                        //CREATE A Runnable, TO GET INTO THE MAIN THREAD TO HANDLE UI CHANGES 
                        MapsActivity.this.runOnUiThread(() -> {
                            //DECODE POLYLINE
                            List<LatLng> polylineList = PolyUtil.decode(polyline);

                            //DRAW POLYLINE ON MAP
                            mMap.addPolyline(new PolylineOptions()
                                    .clickable(true)
                                    .width(10)
                                    .addAll(polylineList));

                            //ADD MIDDLE DISTANCE POINT MARKER
                            addMarker(midPointLatLng, "MidPoint_Fine");

                            //ADD CIRCLE AROUND MIDPOINT
                            Circle circle = mMap.addCircle(new CircleOptions()
                                    .center(midPointLatLng)
                                    .radius(1000)
                                    .strokeColor(Color.CYAN));
                            circle.setVisible(true);

                            //MOVE THE CAMERA ONTO THE MIDPOINT
                            moveCamera(midPointLatLng, 14);
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
    //  FIND THE MIDDLE POINT PLACE_ID
    //*****************************************************************************************************************************
    private void getNearbyBusiness() {
        //BUILD URL STRING
        String midPointLatLng_String = midPointLatLng.latitude + "," + midPointLatLng.longitude;
        String url = Uri.parse("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .buildUpon()
                .appendQueryParameter("location", midPointLatLng_String)
                .appendQueryParameter("radius", "1000")
                .appendQueryParameter("type", "restaurant")
                .appendQueryParameter("key", getString(R.string.maps_key_alex))
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

                        //ONLY RETRIEVE THE Results Array in the response to convert to Model Class NearbySearch
                        JSONArray results = respJSON.getJSONArray("results");

                        //INSTANTIATE OUR ARRAYLIST OF Nearby Business
                        allBusinessList = new ArrayList<>();

                        //ADD ALL FOUND BUSINESS TO THE ARRAYLIST OF Nearby Business
                        for (int i = 0; i < results.length(); i++) {
                            NearbyBusiness uniqueBusinnes = mapper.readValue(results.getJSONObject(i).toString(), NearbyBusiness.class);
                            allBusinessList.add(uniqueBusinnes);
                        }
                        //TODO:: CREATE BUSINESS LISTVIEW DIALOG HERE

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

        //SET STYLE FOR THE MAP
        mMap.setMapStyle(new MapStyleOptions("[{\"featureType\":\"all\"," +
                "\"stylers\":[{\"saturation\":0},{\"hue\":\"#e7ecf0\"}]},{\"featureType" +
                "\":\"road\",\"stylers\":[{\"saturation\":-70}]},{\"featureType\":" +
                "\"transit\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType" +
                "\":\"poi\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":" +
                "\"water\",\"stylers\":[{\"visibility\":\"simplified\"},{\"saturation\":-60}]}]")
        );

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
            //EFFACER le LatLng de la liste
            for (int i = 0; i < locationArrayList.size(); i++) {
                System.out.println("---------" + marker.getPosition());
                System.out.println("---------" + locationArrayList.get(i).toString());
                locationArrayList.remove(marker.getPosition());
                markerArrayList.remove(marker);
                autocompleteFragment.setText("");
                setHints();
            }

            //EFFACER MARKER
            marker.remove();

            //REAFFICHER LA BARRE DE RECHERCHE APRES AVOIR EFFACE UNE ADDRESSE
            autocompleteFragment.requireView().setVisibility(View.VISIBLE);
            findViewById(R.id.ic_gps2).setVisibility(View.VISIBLE);

            if (locationArrayList.isEmpty()) {
                getCurrentLocation();
            }
            return false;
        });
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
        btn_MapCurrentLocation_GPS.setOnClickListener(view -> {
//            Log.d(TAG, "onClicked: clicked gps icon");
            //Center to CurrentLocation
            getCurrentLocation();
        });

        // SET OnClickListener to SearchBar_GPS Button to add a marker
        //*************************************************************************************************

        btn_SearchBar_GPS.setOnClickListener(view -> {
//            Log.d(TAG, "onClicked: clicked Search Bar gps icon");

            //Add marker on CurrentLocation
            getSearchBarCurrentLocation();

            //If the locationArrayList has 2 value in it( when the User enters a second address), Remove SearchBar
            if (locationArrayList.size() == 2) {
                autocompleteFragment.requireView().setVisibility(View.GONE);
                btn_SearchBar_GPS.setVisibility(View.GONE);
            }
        });


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
        Places.initialize(getApplicationContext(), "AIzaSyDR3NrmbrjstWl59Wwy23yjBS3nrp67kT4");

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        //SET SEARCH BAR HINT
        autocompleteFragment.setHint(getString(R.string.search_address_1));

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
                //Log.d(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());

                //AJOUTER LES COORDONNEES DANS LA LISTE DES LatLng
                locationArrayList.add(place.getLatLng());

                //CENTER CAMERA ON THE LOCATION ENTERED
                moveCamera(Objects.requireNonNull(place.getLatLng()), DEFAULT_ZOOM);
                addMarker(place.getLatLng(), place.getName());


                //CACHER LA BARRE DE RECHERCHE QUAND IL Y A 2 ADRESSES
                if (locationArrayList.size() == 2) {
                    autocompleteFragment.requireView().setVisibility(View.GONE);
                    btn_SearchBar_GPS.setVisibility(View.GONE);

                    //INITIATE LOGIC FOR SEARCH RESULTS
                    try {
                        //FIND MIDDLE DISTANCE POINT
                        findMiddleDistancePoint();


                        //SHOW RESULT IN LISTvIEW

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
//                Log.d(TAG, "An error occurred: " + status);
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
//        Log.d(TAG, "onRequestPermissionResults: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    //CHECK RESULTS --> IF FALSE, RETURN WITHOUT InitMap(), PERMISSION NOT GRANTED
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
//                            Log.d(TAG, "onRequestPermissionResults: permission failed.");
                            return;
                        }
                    }

                    //PERMISSION WERE GRANTED, INITIALIZE THE MAP
                    mLocationPermissionsGranted = true;
                    //Initialize the Map
                    initMap();
                }
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
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.show(getSupportFragmentManager(), "LoginDialogFragment");
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


}//END MAPACTIVITY  //==============================================================================================================================================================