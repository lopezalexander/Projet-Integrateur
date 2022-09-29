package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.LoginDialog;
import com.example.projetintegrateur.adapter.PlaceAutoCompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {


    //VIEW xml
    private EditText mSearchText;
    private ImageView mGps;

    private final String TAG = "debug";

    //Dynamic List of LatLng from SearchBar
    private ArrayList<LatLng> locationArrayList;

    private ArrayList<Marker> markerArrayList;

    //Place API Autocomplete
    AutocompleteSupportFragment autocompleteFragment;

    PlaceAutoCompleteAdapter placeAutoCompleteAdapter;

//    private GoogleApiClient mGoogleApiClient;

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

//    private static final  LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168), new LatLng(71,136));


    //***********\\
    //  OnCREATE  \\
    //*****************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationArrayList = new ArrayList<>();
        markerArrayList = new ArrayList<Marker>();

        if (isServicesOK()) {
            //GET PERMISSION
            getLocationPermission();

            //SET VIEW BUTTON, FIREBASE, etc
            initView();

            //GET CURRENT LOCATION
            getDeviceLocation();

            setUpPlacesAutocomplete();

            //CHECK IF User is already Connected or Display Login Dialog
//            checkUserAuth();

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

        for (int i = 0; i < locationArrayList.size(); i++) {

            // below line is use to add marker to each location of our array list.
            mMap.addMarker(new MarkerOptions().position(locationArrayList.get(i)).title("Marker"));

//            // below lin is use to zoom our camera on map.
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
//
//            // below line is use to move our camera to the specific location.
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(locationArrayList.get(i)));
        }

        if (!locationArrayList.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            LatLng position;
            for (int i = 0; i < locationArrayList.size(); i++) {
                position = locationArrayList.get(i);
                builder.include(new LatLng(position.latitude, position.longitude));
            }
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
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


//    //*****************************\\
//    //  SEARCH ADDRESSES FUNCTIONS  \\
//    //*****************************************************************************************************************************
//    private void geoLocate() {
//        //Get Search Input
//        String searchString = mSearchText.getText().toString();
//
//
//        //Initiate GEOCODER
//        Geocoder geocoder = new Geocoder(this);
//
//        //Container for Address Results
//        List<Address> list = new ArrayList<>();
//
//        try {
//            list = geocoder.getFromLocationName(searchString, 1);
//        } catch (IOException e) {
//            Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
//        }
//
//
//        if (!list.isEmpty()) {
//            Address address = list.get(0);
//
//            Log.d(TAG, "geoLocate: found a location: " + address.toString());
//            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
//            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
//                    address.getAddressLine(0));
//        }
//
//    }

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
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(latLng, DEFAULT_ZOOM);
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

    private void getDeviceCoordinates() {
        final LatLng[] latLng = new LatLng[1];
        Log.d(TAG, "2) getDeviceCoordinates: getting the devices current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "3) onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        latLng[0] = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        locationArrayList.add(latLng[0]);
                        moveCamera(latLng[0], DEFAULT_ZOOM);
                        addMarker(latLng[0],"Current Location");
                    } else {
                        Log.d(TAG, "3) onComplete: current location is null");
                        String str = "unable to get current location";
                        Toast.makeText(MapsActivity.this, str, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "4) moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void addMarker(LatLng latLng ,String title) {

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


        markerArrayList.add(mMap.addMarker(markerOptions));

        autocompleteFragment.setText("");

        //onClick Listener pour Supprimer Marker
        mMap.setOnMarkerClickListener(marker -> {
            //EFFACER le LatLng de la liste
            for (int i=0; i<locationArrayList.size();i++) {
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
            autocompleteFragment.getView().setVisibility(View.VISIBLE);
            findViewById(R.id.ic_gps2).setVisibility(View.VISIBLE);

            if (locationArrayList.isEmpty()) {
                getDeviceLocation();
            }
            return false;
        });


        setHints();
        hideSoftKeyboard();
    }

    //SET HINTS
    private void setHints() {
        if (locationArrayList.size() == 1) {
            autocompleteFragment.setHint("Entrez la 2ème addresse");
        } else if (locationArrayList.size() == 0) {
            autocompleteFragment.setHint("Entrez votre addresse");
        } else if (locationArrayList.size() == 2) {
            autocompleteFragment.getView().setVisibility(View.GONE);
            findViewById(R.id.ic_gps2).setVisibility(View.GONE);
        }
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

        //INITIALIZE PLACES API
        Places.initialize(getApplicationContext(), "AIzaSyCt0NIr9jL92fUTQEco4ZykynMCgR0JLMY");

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();

        //VIEW
//        mSearchText = findViewById(R.id.input_addresses);
//        mSearchText.setHint(R.string.search_address_1)
//        mSearchText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
//            if (keyEvent != null) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH
//                        || actionId == EditorInfo.IME_ACTION_DONE
//                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
//
//
//                    //execute our method for searching
//                    geoLocate();
//                    mSearchText.getText().clear();
//                    mSearchText.clearFocus();
//                }
//            }
//            return false;
//        });


        mGps = (ImageView) findViewById(R.id.ic_gps);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClicked: clicked gps icon");
                getDeviceLocation();
            }
        });




//        mGoogleApiClient = Places.getGeoDataClient(this, null);

//        mGoogleApiClient = new GoogleApiClient
//                .Builder(this)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .enableAutoManage(this, this)
//                .build();
//
//        placeAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this, mGoogleApiClient ,LAT_LNG_BOUNDS,null);

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
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        //SET SEARCH BAR HINT
        autocompleteFragment.setHint(getString(R.string.search_address_1));

        //SET SPECIFIC COUNTRY BASED SEARCH
        autocompleteFragment.setCountries("CA","US");

        //SET LOCATION BOUNDS FOR BETTER SEARCH RESULTS
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(45.508888,-73.561668),
                new LatLng(45.508888,-73.561668)
        ));

        ImageView mGps2 = findViewById(R.id.ic_gps2);
        mGps2.setOnClickListener(view -> {
            Log.d(TAG, "onClicked: clicked Search Bar gps icon");
            getDeviceCoordinates();
            if (locationArrayList.size() == 2) {
                autocompleteFragment.getView().setVisibility(View.GONE);
                findViewById(R.id.ic_gps2).setVisibility(View.GONE);
            }
        });


        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());

                //AJOUTER LES COORDONNEES DANS LA LISTE DES LatLng
                locationArrayList.add(place.getLatLng());

                //CENTRER LA VUE????????
                moveCamera(place.getLatLng(), DEFAULT_ZOOM);
                addMarker(place.getLatLng(), place.getName());
//                CACHER LA BARRE DE RECHERCHE QUAND IL Y A 2 ADRESSES
                if (locationArrayList.size() == 2) {
                    autocompleteFragment.getView().setVisibility(View.GONE);
                    findViewById(R.id.ic_gps2).setVisibility(View.GONE);
                }
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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}