package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.CustomPagerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private final String TAG = "debug";

    //COPIED FROM MAIN ACTIVITY***********************************************************************************************************************************************
    private static final int ERROR_DIALOG_REQUEST = 9001;
    //    private static final int RC_SIGN_IN = 666;
    //    private GoogleSignInClient mGoogleSignInClient;
    //    private FirebaseAuth mAuth;

    //*************************************************************************************************************************************************************************

    //COPIED FROM MAPS ACTIVITY***********************************************************************************************************************************************
    //    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    //    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    //    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //*************************************************************************************************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Initializes the maps system and the view.
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        if (isServicesOK()) {
            init();
        }

        //SHOW LOGIN WHEN THE APP STARTS
        signIn_Dialog();


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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }

    // Verify if Google Play Service are installed, if not, request to install Google Play Service
    public boolean isServicesOK() {
        Log.d("TAG", "isServicesOK : checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK : Google Play Services is working");

            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //An error occured but we can resolve it
            Log.d(TAG, "isServicesOK : an error occured but we can fix it");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void init() {
        //TODO:: CHECK DOCUMENTATION FOR FURTHER ACTION HERE
    }


    //*****************\\
    //  Sign-In DIALOG  \\
    //*****************************************************************************************************************************

    private void signIn_Dialog() {
        // Dialog Builder
        AlertDialog.Builder loginDialogBuilder = new AlertDialog.Builder(this, R.style.style_form_signIn);

        //Create View for Dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View myFormView = inflater.inflate(R.layout.login_layout2, findViewById(R.id.rootContainer));

        //Setup Login Carousel
        ViewPager login_Carousel = (ViewPager) myFormView.findViewById(R.id.pager);
        int[] carousel_Images = {R.drawable.page_one, R.drawable.page_two, R.drawable.page_three};
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(this, carousel_Images);
        login_Carousel.setAdapter(mCustomPagerAdapter);

        //Set View to Dialog Builder
        loginDialogBuilder.setView(myFormView);

        //Create Login Dialog
        AlertDialog loginDialog = loginDialogBuilder.create();

        //Show Login Dialog
        loginDialog.show();


//        // After some action
//        alertDialog.dismiss();


        //INSTANTIATE THE FORM AND INFLATE IT________________________________________________________________________________________________________________
//        AlertDialog.Builder myForm = new AlertDialog.Builder(this, R.style.style_form_signIn);
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View myFormView = inflater.inflate(R.layout.signup_layout, findViewById(R.id.rootContainer));
//


        //FORM ACTION ====CREATE / CANCEL======_______________________________________________________________________________________________________________
//        myForm.setView(myFormView)
//                .setPositiveButton("Create", (dialog, id) -> {
//
//                    Toast.makeText(this, "Toast!", Toast.LENGTH_LONG).show();
//                })
//
//                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

//        //CREATE FORM
//        myForm.create();
//
//        //DISPLAY THE FORM
//        myForm.show();
    }


    //****************\\
    //  Firebase Auth  \\
    //*****************************************************************************************************************************

//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("TAG", "signInWithCredential:success");
//                            // Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            //  Intent intent = new Intent(getApplicationContext(), MapActivity.class);
//                            //   startActivity(intent);
////                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("TAG", "signInWithCredential:failure", task.getException());
//                            // Toast.makeText(MainActivity.this, "Sign In failed!", Toast.LENGTH_SHORT).show();
////                            updateUI(null);
//                        }
//                    }
//                });
//    }


    //*****************\\
    //  Google Sign-In  \\
    //*****************************************************************************************************************************
//
//    // Configure Google Sign In
//    private void createSignInRequest() {
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//    }
//
//    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }


    //******************\\
    //  INTENT RESULTS   \\
    //*******************************************************************************************************************************************
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Log.w("TAG", "Google sign in failed", e);
//            }
//        }
    }


    //****************\\
    //  HIDE KEYBOARD   \\
    //*******************************************************************************************************************************************
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}