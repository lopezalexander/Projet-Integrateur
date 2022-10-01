package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLng;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        Log.d(TAG, "2.A) getDeviceLocation: getting the devices current location FROM MAP GPS BUTTON");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task<Location> location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "3) onComplete: found location!");

                        //Get result to find currentLocation
                        Location currentLocation = task.getResult();

                        //Set LatLng
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                        //MoveCamera to LatLng
                        moveCamera(latLng, DEFAULT_ZOOM);
                    } else {
                        Log.d(TAG, "3) onComplete: current location is null");
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
        Log.d(TAG, "2.B) getDeviceCoordinates: getting the devices current location FROM SEARCH BAR GPS BUTTON");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task<Location> location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "3) onComplete: found location!");

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
                        Log.d(TAG, "3) onComplete: current location is null");
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
        Log.d(TAG, "4) moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
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
    //  DRAW THE POLYLINE BETWEEN TWO LOCATION/COORDINATE
    //*****************************************************************************************************************************
    private void drawPolyline() throws IOException {

        LatLng origin = locationArrayList.get(0);
        double originLat = origin.latitude;
        double originLng = origin.longitude;
        String originCoordinate = originLat + "," + originLng;

        LatLng destination = locationArrayList.get(1);
        double destinationLat = destination.latitude;
        double destinationLng = destination.longitude;
        String destinationCoordinate = destinationLat + "," + destinationLng;

        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("origin", originCoordinate)
                .appendQueryParameter("destination", destinationCoordinate)
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", getString(R.string.maps_key_alex))
                .toString();

        //LOGS+++++++++++++++++++++++++++++++++++++++++++++
//        for (LatLng unique : locationArrayList) {
//            Log.d("coord", "======COORDONNEE=====");
//            Log.d("coord", String.valueOf(unique.latitude));
//            Log.d("coord", String.valueOf(unique.longitude));
//        }
//        Log.d("coord", "origin ==> " + origin.toString());
//        Log.d("coord", "destination ==> " + destination.toString());
//        Log.d("coord", url);

        //LOGS+++++++++++++++++++++++++++++++++++++++++++++

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {

                    try {
                        //CREATE JSON OBJECT WITH RESPONSE
                        JSONObject resultJSON = new JSONObject(Objects.requireNonNull(response.body()).string());

                        //EXTRACT ROUTE OBJECT -- LEGS ARRAY -- STEPS ARRAY
                        JSONObject routeObject = resultJSON.getJSONArray("routes").getJSONObject(0);
                        JSONArray legsArray = resultJSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                        JSONArray stepssArray = resultJSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

                        //EXTRACT DISTANCES BETWEEN ORIGIN/DESTINATION AND POYLINE STRING
                        int distance_value = legsArray.getJSONObject(0).getJSONObject("distance").getInt("value");
                        String polyline = routeObject.getJSONObject("overview_polyline").getString("points");

//                        Log.d("test", "------******[  ROUTE  ]*****--------");
//                        Log.d("test", "POLYLINE ====> " + polyline);
//                        Log.d("test", "-");
//
//                        Log.d("test", "------******[  LEGS  ]*****--------");
//                        Log.d("test", "DISTANCE KM ====> " + distance);
//                        Log.d("test", "DISTANCE VALUE ====> " + String.valueOf(distance_value));
//                        Log.d("test", "DISTANCE MIDPOINT ====> " + String.valueOf(distance_value / 2));
//                        Log.d("test", "-");
//                        Log.d("test", "------******[  STEPS  ]*****--------");

                        int distanceCounter = 0;
//                        Log.d("test", "ARRAY LENGTH-------- " + String.valueOf(stepssArray.length()));

                        for (int i = 0; i < stepssArray.length(); i++) {
//                            Log.d("test", "===[ " + i + " ]===");
                            String step_distance_temp = stepssArray.getJSONObject(i).getJSONObject("distance").getString("text");
                            int step_distance_value_temp = stepssArray.getJSONObject(i).getJSONObject("distance").getInt("value");
                            distanceCounter += step_distance_value_temp;
//
//                            Log.d("test", "DISTANCE KM ====> " + step_distance_temp);
//                            Log.d("test", "DISTANCE VALUE ====> " + String.valueOf(distance_value));
//                            Log.d("test", "DISTANCE VALUE ====> " + String.valueOf(distanceCounter) + "  //  " + distance_value / 2);

                            if (distanceCounter >= distance_value / 2) {
//                                Log.d("test", "-------PASSED MID POINT-------");
//                                Log.d("test", "Index ==> " + i);
                                double lat = stepssArray.getJSONObject(i).getJSONObject("start_location").getInt("lat");
                                double lng = stepssArray.getJSONObject(i).getJSONObject("start_location").getInt("lng");

//                                Log.d("test", "DISTANCE VALUE ====> " + String.valueOf(distance_value));
//                                Log.d("test", "DISTANCE VALUE ====> " + String.valueOf(distanceCounter));
//                                Log.d("test", "LatLng         ====> " + String.valueOf(lat) + "," + String.valueOf(lng));

                            }
                        }

                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Handle UI here
                                List<LatLng> polylineList = PolyUtil.decode(polyline);
                                Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                                        .clickable(true)
                                        .width(10)
                                        .addAll(polylineList));
                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //mMap.addPolyline()
                }


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
            for (int i = 0; i < locationArrayList.size(); i++) {
                //REMOVE LatLng entry from locationArrayList && REMOVE Marker from markerArrayList
                locationArrayList.remove(marker.getPosition());
                markerArrayList.remove(marker);

                //CLEAR SearchBar text
                autocompleteFragment.setText("");

                //Set the SearchBar hint accordingly to
                setHints();
            }

            //REMOVE MARKER
            marker.remove();

            //REAFFICHER LA BARRE DE RECHERCHE APRES AVOIR EFFACE UNE ADDRESSE
            autocompleteFragment.requireView().setVisibility(View.VISIBLE);
            btn_SearchBar_GPS.setVisibility(View.VISIBLE);

            //IF THE locationArrayList is empty, recenter to the user current location
            if (locationArrayList.isEmpty()) {
                getCurrentLocation();
            }
            return false;
        });


//        for (int i = 0; i < locationArrayList.size(); i++) {
//            // below line is use to add marker to each location of our array list.
//            Log.d(TAG, locationArrayList.get(1).toString());
//            mMap.addMarker(new MarkerOptions().position(locationArrayList.get(i)).title("Marker"));
//
////            // below lin is use to zoom our camera on map.
////            mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
////
////            // below line is use to move our camera to the specific location.
////            mMap.moveCamera(CameraUpdateFactory.newLatLng(locationArrayList.get(i)));
//        }

//        if (!locationArrayList.isEmpty()) {
//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            LatLng position;
//            for (int i = 0; i < locationArrayList.size(); i++) {
//                position = locationArrayList.get(i);
//                builder.include(new LatLng(position.latitude, position.longitude));
//            }
//            LatLngBounds bounds = builder.build();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
//        }


    }

    //
    //
    // Verify if Google Play Service are installed, if not, request to install Google Play Service
    //*****************************************************************************************************************************
    public boolean isServicesOK() {
        //Log.d("TAG", "isServicesOK : checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            //Log.d(TAG, "isServicesOK : Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //An error occured but we can resolve it
            //Log.d(TAG, "isServicesOK : an error occured but we can fix it");

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


        // SET OnClickListener to MAP_GPS Button to center on CurrentLocation
        //*************************************************************************************************
        btn_MapCurrentLocation_GPS.setOnClickListener(view -> {
            Log.d(TAG, "onClicked: clicked gps icon");
            //Center to CurrentLocation
            getCurrentLocation();
        });

        // SET OnClickListener to SearchBar_GPS Button to add a marker
        //*************************************************************************************************

        btn_SearchBar_GPS.setOnClickListener(view -> {
            Log.d(TAG, "onClicked: clicked Search Bar gps icon");

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
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());

                //AJOUTER LES COORDONNEES DANS LA LISTE DES LatLng
                locationArrayList.add(place.getLatLng());

                //CENTER CAMERA ON THE LOCATION ENTERED
                moveCamera(Objects.requireNonNull(place.getLatLng()), DEFAULT_ZOOM);
                addMarker(place.getLatLng(), place.getName());


                //CACHER LA BARRE DE RECHERCHE QUAND IL Y A 2 ADRESSES
                if (locationArrayList.size() == 2) {
                    autocompleteFragment.requireView().setVisibility(View.GONE);
                    btn_SearchBar_GPS.setVisibility(View.GONE);


                    //TODO:: GET COORDINATE AND DRAW A POLYLINE
                    try {
                        drawPolyline();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //TODO:: GET THE DISTANCE AND DIVIDE BY TWO


                }
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
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
        Log.d(TAG, "onRequestPermissionResults: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    //CHECK RESULTS --> IF FALSE, RETURN WITHOUT InitMap(), PERMISSION NOT GRANTED
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionResults: permission failed.");
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