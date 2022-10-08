package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentResultListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
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
import com.example.projetintegrateur.model.NearbyBusiness;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.model.directionAPI.Leg;
import com.example.projetintegrateur.model.directionAPI.Route;
import com.example.projetintegrateur.model.directionAPI.Step;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
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

public class MapHistoryActivity extends FragmentActivity implements OnMapReadyCallback {
    private final String TAG = "debug";

    //Dynamic List of LatLng from SearchBar
    private ArrayList<LatLng> locationArrayList;
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
    LatLng selectedBusiness;

    ArrayList<NearbyBusiness> allNearbyBusinessList;

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
        setContentView(R.layout.activity_map_history);

        btn_MapCurrentLocation_GPS = findViewById(R.id.ic_gps);

        //SET VIEW BUTTON, FIREBASE, etc
        initView();
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
                Task<Location> location = fusedLocationProviderClient.getLastLocation();

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
                            Toast.makeText(MapHistoryActivity.this, err, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        String err = "unable to get current location";
                        Toast.makeText(MapHistoryActivity.this, err, Toast.LENGTH_SHORT).show();
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

        if (!title.equals("MidPoint_Fine")) {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person4));
        } else {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
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

        //Disables the native button for getting current location, we will need to create
        //our own, because we need to add the seach bar
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().isCompassEnabled();
        mMap.getUiSettings().isRotateGesturesEnabled();


    }

    //
    //
    // Verify if Google Play Service are installed, if not, request to install Google Play Service
    //*****************************************************************************************************************************
    public boolean isServicesOK() {
//        Log.d("TAG", "isServicesOK : checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapHistoryActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
//            Log.d(TAG, "isServicesOK : Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //An error occured but we can resolve it
//            Log.d(TAG, "isServicesOK : an error occured but we can fix it");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapHistoryActivity.this, available, ERROR_DIALOG_REQUEST);
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



}//END MAPACTIVITY  //==============================================================================================================================================================