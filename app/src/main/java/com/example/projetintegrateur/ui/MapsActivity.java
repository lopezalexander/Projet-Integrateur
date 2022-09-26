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
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.CustomPagerAdapter;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "debug";

    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;

    //LOGIN PAGE VIEW ITEMS
    AlertDialog loginDialog;
    EditText email_input;
    EditText password_input;
    Button firebase_register_btn;
    ProgressBar login_progressBar;


    //COPIED FROM MAIN ACTIVITY***********************************************************************************************************************************************
    private static final int ERROR_DIALOG_REQUEST = 9001;
    //    private static final int RC_SIGN_IN = 666;
    //    private GoogleSignInClient mGoogleSignInClient;

    //*************************************************************************************************************************************************************************


    //***********\\
    //  OnCREATE  \\
    //*****************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();

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
        login_Dialog();


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
            Objects.requireNonNull(dialog).show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void init() {
        //TODO:: CHECK DOCUMENTATION FOR FURTHER ACTION HERE
    }


    //*****************\\
    //  LOGIN DIALOG    \\
    //*****************************************************************************************************************************

    private void login_Dialog() {
        // Dialog Builder
        AlertDialog.Builder loginDialogBuilder = new AlertDialog.Builder(this, R.style.style_form_signIn);

        //Create View for Dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View myFormView = inflater.inflate(R.layout.login_layout, findViewById(R.id.rootContainer));

        //Setup Login Carousel
        //********************
        ViewPager login_Carousel = myFormView.findViewById(R.id.pager);
        int[] carousel_Images = {R.drawable.page_one, R.drawable.page_two, R.drawable.page_three};
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(this, carousel_Images);
        login_Carousel.setAdapter(mCustomPagerAdapter);


        //GET VIEW ELEMENTS AND SETUP CLICKLISTENER AND HIDEKEYBOARD ON EDITTEXT
        //**********************************************************************
        email_input = myFormView.findViewById(R.id.input_email);
        password_input = myFormView.findViewById(R.id.input_password);

        ImageView google_signIn_btn = myFormView.findViewById(R.id.btn_google);
        Button firebase_signIn_btn = myFormView.findViewById(R.id.btn_signIn);
        firebase_register_btn = myFormView.findViewById(R.id.btn_register);

        login_progressBar = myFormView.findViewById(R.id.login_progressbar);
        //TextView google_register_web = myFormView.findViewById(R.id.webLink_google_register);


        //SET HideKeyBoard() to EditText
        ArrayList<EditText> editTextList = new ArrayList<>();
        editTextList.add(email_input);
        editTextList.add(password_input);

        for (int i = 0; i < editTextList.size(); i++) {
            editTextList.get(i).setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            });
        }

        //SET BUTTON LISTENER FOR LOGIN IN OR REGISTERING USER
        //*****************************************************
        //GOOGLE SIGN IN LOGIC
        google_signIn_btn.setOnClickListener(view -> {
            //INSERT GOOGLE SIGN IN LOGIC HERE
        });


        //FIREBASE LOGIN LOGIC
        firebase_signIn_btn.setOnClickListener(view -> {
            //FIREBASE LOGIN FUNCTION
            loginUserFirebase();
        });

        //FIREBASE REGISTER FUNCTION
        firebase_register_btn.setOnClickListener(view -> registerUserFirebase());


        //Set View to Dialog Builder
        loginDialogBuilder.setView(myFormView);

        //Create Login Dialog
        loginDialog = loginDialogBuilder.create();

        //Show Login Dialog
        loginDialog.show();

    }


    //****************\\
    //  FIREBASE AUTH  \\
    //*****************************************************************************************************************************
    //
    //
    //  REGISTRATION EMAIL/PASSWORD
    //************************************
    private void registerUserFirebase() {
        //GET LOGIN INPUT DATA
        String email = email_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();
        boolean valid = loginValidation(email, password);

        if (valid) {
            //AFTER VALIDATION ARE GOOD, SET THE PROGRESS BAR TO VISIBLE
            login_progressBar.setVisibility(View.VISIBLE);

            //CREATE AUTH USER IN AUTHENTICATION IN FIREBASE
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    //GET THE USER ID AND SET IT TO THE User Object THAT WE WILL INSERT IN THE DATABASE
                                    String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    User user = new User(email, "", userID);

                                    //INSERT THE USER IN THE FIREBASE REALTIME DATABASE TABLE --> Users
                                    mFirebaseDB.getReference("Users")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                            .setValue(user).addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(this, "User is Registered!", Toast.LENGTH_LONG).show();

                                                    //RESET LOGIN DIALOG ELEMENTS/VIEWS
                                                    email_input.setText("");
                                                    password_input.setText("");
                                                } else {
                                                    Toast.makeText(this, "Could not register User!", Toast.LENGTH_LONG).show();
                                                }

                                            });
                                } else {
                                    Toast.makeText(this, "Could not register User!", Toast.LENGTH_LONG).show();
                                }
                                //REMOVE-HIDE PROGRESS BAR
                                login_progressBar.setVisibility(View.GONE);
                            }
                    );

        }


    }

    //
    //
    //  LOGIN EMAIL/PASSWORD
    //************************************
    private void loginUserFirebase() {
        //GET LOGIN INPUT DATA
        String email = email_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();
        boolean valid = loginValidation(email, password);

        if (valid) {
            //AFTER VALIDATION ARE GOOD, SET THE PROGRESS BAR TO VISIBLE
            login_progressBar.setVisibility(View.VISIBLE);

            //PERFORM LOGIN WITH CREDENTIAL SUPPLIED
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    //[RETRIEVE] CURRENT USER_ID FROM FIREBASE_AUTH ... TO FETCH IT FROM DATABASE IN NEXT STEPS
                                    String currentUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                                    //[GET REFERENCE] FOR CURRENT_USER FROM DATABASE WITH currentUserKey
                                    DatabaseReference ref = mFirebaseDB.getReference("Users").child(currentUserKey);

                                    //[FETCH] THE USER IN DATABASE
                                    ref.get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            User currentUser = task1.getResult().getValue(User.class);
                                            ((UserClient) getApplicationContext()).setUser(currentUser);

                                            //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                            loginDialog.dismiss();

                                            // Get the User Data
                                            Toast.makeText(this, "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                        } else {
                                            //HANDLE ERROR HERE if we cannot retrieve the user data
                                            Toast.makeText(this, "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(this, "Failed to login! Check your credentials.", Toast.LENGTH_LONG).show();
                                }
                                //REMOVE-HIDE PROGRESS BAR
                                login_progressBar.setVisibility(View.GONE);
                            }
                    );
        }
    }


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


    //********************\\
    //  UTILITY FUNCTIONS  \\
    //*******************************************************************************************************************************************
    //
    //
    //  HIDE KEYBOARD
    //************************************
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //
    //
    //  LOGIN/REGISTRATION VALIDATION
    //**************************************************************
    private boolean loginValidation(String email, String password) {
        //VALIDATIONS
        if (email.isEmpty()) {
            email_input.setError("Email Required!");
            email_input.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Please provide valid email!");
            email_input.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            password_input.setError("Password is Required!");
            password_input.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            password_input.setError("Password requires at least 6 characters!");
            password_input.requestFocus();
            return false;
        }

        //If no error, return TRUE
        return true;
    }


}