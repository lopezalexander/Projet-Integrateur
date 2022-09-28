package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.LoginDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    //VIEW xml
    private EditText mSearchText;
    private ImageView mGps;

    private final String TAG = "debug";

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
    private static final float DEFAULT_ZOOM = 15f;


    //***********\\
    //  OnCREATE  \\
    //*****************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (isServicesOK()) {
            //GET PERMISSION
            getLocationPermission();

            //SET VIEW BUTTON, FIREBASE, etc
            initView();
            Places.initialize(getApplicationContext(), "AIzaSyDR3NrmbrjstWl59Wwy23yjBS3nrp67kT4");

            setUpPlacesAutocomplete();

            //CHECK IF User is already Connected or Display Login Dialog
            checkUserAuth();
        }
    }

    private void checkUserAuth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.show(getSupportFragmentManager(), "LoginDialogFragment");
        }
    }

    //********************\\
    //  Google Maps Setup  \\
    //*****************************************************************************************************************************

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //Style pour le Map
        mMap.setMapStyle(new MapStyleOptions("[{\"featureType\":\"all\"," +
                "\"stylers\":[{\"saturation\":0},{\"hue\":\"#e7ecf0\"}]},{\"featureType" +
                "\":\"road\",\"stylers\":[{\"saturation\":-70}]},{\"featureType\":" +
                "\"transit\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType" +
                "\":\"poi\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":" +
                "\"water\",\"stylers\":[{\"visibility\":\"simplified\"},{\"saturation\":-60}]}]")
        );

        //GET PERMISSION FOR FINE AND COARSE LOCATION --> USED FOR GEOLOCATION
        if (mLocationPermissionsGranted) {
            //GET CURRENT LOCATION
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //Disables the native button for getting current location, we will need to create
            //our own, because we need to add the seach bar
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().isCompassEnabled();
            mMap.getUiSettings().isRotateGesturesEnabled();
        }
    }

    // Verify if Google Play Service are installed, if not, request to install Google Play Service
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


    //*****************************\\
    //  SEARCH ADDRESSES FUNCTIONS  \\
    //*****************************************************************************************************************************
    private void geoLocate() {
        //Get Search Input
        String searchString = mSearchText.getText().toString();

        //Initiate GEOCODER
        Geocoder geocoder = new Geocoder(this);

        //Container for Address Results
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
        }


        if (!list.isEmpty()) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
//            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }

    }

    private void getDeviceLocation() {
        Log.d(TAG, "2) getDeviceLocation: getting the devices current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "3) onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                        } else {
                            Log.d(TAG, "3) onComplete: current location is null");
                            String str = "unable to get current location";
                            Toast.makeText(MapsActivity.this, str, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "4) moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);

        }

        hideSoftKeyboard();
    }


    private void getLocationPermission() {
        Log.d(TAG, "1) getLocationPermission: getting location permissions");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionResults: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionResults: permission failed.");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionResults: permissions granted.");
                    mLocationPermissionsGranted = true;
                    //initialize our Map
                    initMap();
                }
            }
        }
    }


    private void initView() {
        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();

        //VIEW
        mSearchText = findViewById(R.id.input_addresses);
        mSearchText.setHint(R.string.search_address_1);

        mSearchText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (keyEvent != null) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {


                    //execute our method for searching
                    geoLocate();
                    mSearchText.getText().clear();
                    mSearchText.clearFocus();
                }
            }
            return false;
        });


        mGps = (ImageView) findViewById(R.id.ic_gps);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClicked: clicked gps icon");
                getDeviceLocation();
            }
        });
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.newInstance(new GoogleMapOptions().mapId(getResources().getString(R.string.mapId)));

        // Initializes the maps system and the view.
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    private void setUpPlacesAutocomplete() {
        Places.initialize(getApplicationContext(), "AIzaSyDR3NrmbrjstWl59Wwy23yjBS3nrp67kT4");

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    //  HIDE KEYBOARD
    //************************************
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


}