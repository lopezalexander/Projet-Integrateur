package com.example.projetintegrateur.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ActionBar;
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

import com.example.projetintegrateur.ui.ProfileActivity;
import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.CustomPagerAdapter;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "debug";

    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;

    //Facebook login
    CallbackManager callbackManager;

    //LOGIN PAGE VIEW ITEMS
    AlertDialog loginDialog;
    EditText email_input;
    EditText password_input;
    Button firebase_register_btn;

    ProgressBar login_progressBar;


    //COPIED FROM MAIN ACTIVITY***********************************************************************************************************************************************
    //GOOGLE LOGIN
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);

            //CONTINUER AVEC LE LOGIN AVEC FIREBASE
            loginUserGoogleFirebase(account.getIdToken());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    });





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


        //Facebook login
        loginUserFacebook();


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && accessToken.isExpired() == false) {
//            loginDialog.dismiss();
        }

        ImageView perso = findViewById(R.id.ic_perso);
        perso.setOnClickListener(view -> {
            Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);

            MapsActivity.this.startActivity(intent);
        });



        //GOOGLE CREER LE SIGNIN REQUEST ET LE LAUNCHER DE L'ACTIVITY POUR LE SignIn INTENT
        createSignInRequest();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Initializes the maps system and the view.
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        if (isServicesOK()) {
            init();
        }

        //CHECK IF User is already Connected
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            login_Dialog();
        }

    }

    private void loginUserFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                        loginDialog.dismiss();
                        Log.d(TAG,"Compte Facebook est connecté");
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
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
        ImageView facebook_signIn_btn = myFormView.findViewById(R.id.btn_facebook);
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
            signIn_CreateGoogleIntent();
        });



        //*****************************************************
        //Facebook SIGN IN LOGIC
        facebook_signIn_btn.setOnClickListener(view -> {
            LoginManager.getInstance().logInWithReadPermissions(MapsActivity.this, Arrays.asList("public_profile"));
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
                                    //[GET REFERENCE] FOR CURRENT_USER FROM DATABASE WITH currentUserKey
                                    String currentUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    DatabaseReference ref = mFirebaseDB.getReference("Users").child(currentUserKey);


                                    //Check if user exist
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists()) {
                                                //****************************************************
                                                //ADD USER TO USER TABLE IN FIREBASE REALTIME DATABASE
                                                //****************************************************

                                                //GET THE USER ID AND SET IT TO THE User Object THAT WE WILL INSERT IN THE DATABASE
                                                String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                                                User newUserData = new User(email, "", currentUserKey);

                                                //INSERT THE USER IN THE FIREBASE REALTIME DATABASE TABLE --> Users
                                                mFirebaseDB.getReference("Users")
                                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                        .setValue(newUserData).addOnCompleteListener(task1 -> {
                                                            //HANDLE ERROR, NOTHING ON SUCCESS...CONTINUE
                                                            if (!task1.isSuccessful()) {
                                                                Toast.makeText(MapsActivity.this, "Could not register User!", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            } //END INSERT USER IN DB TABLE 'Users'

                                            //************************
                                            //FAIRE LE SINGLETON USER
                                            //************************

                                            //[FETCH] THE USER IN DATABASE
                                            ref.get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    //[CREATE] SINGLETON
                                                    User currentUser = task2.getResult().getValue(User.class);
                                                    ((UserClient) getApplicationContext()).setUser(currentUser);

                                                    //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                                    loginDialog.dismiss();

                                                    // Toast
                                                    Toast.makeText(MapsActivity.this, "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                                } else {
                                                    //HANDLE ERROR HERE if we cannot retrieve the user data
                                                    Toast.makeText(MapsActivity.this, "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                                }
                                            }); //END CREATE SINGLETON
                                        }//END onDataChanged

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        } //END onCancelled
                                    }); //END DATABASE QUERY TO CHECK USER
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
                                            //[CREATE] SINGLETON
                                            User currentUser = task1.getResult().getValue(User.class);
                                            ((UserClient) getApplicationContext()).setUser(currentUser);

                                            //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                            loginDialog.dismiss();

                                            // Toast
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

    //
    //
    //  LOGIN GOOGLE
    //************************************
    private void loginUserGoogleFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        //AFTER VALIDATION ARE GOOD, SET THE PROGRESS BAR TO VISIBLE
        login_progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        //[GET REFERENCE] FOR CURRENT_USER FROM DATABASE WITH currentUserKey
                        String currentUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        DatabaseReference ref = mFirebaseDB.getReference("Users").child(currentUserKey);

                        //Check if user exist
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    //****************************************************
                                    //ADD USER TO USER TABLE IN FIREBASE REALTIME DATABASE
                                    //****************************************************

                                    //GET THE USER ID AND SET IT TO THE User Object THAT WE WILL INSERT IN THE DATABASE
                                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                                    User newUserData = new User(email, "", currentUserKey);

                                    //INSERT THE USER IN THE FIREBASE REALTIME DATABASE TABLE --> Users
                                    mFirebaseDB.getReference("Users")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                            .setValue(newUserData).addOnCompleteListener(task1 -> {
                                                //HANDLE ERROR, NOTHING ON SUCCESS...CONTINUE
                                                if (!task1.isSuccessful()) {
                                                    Toast.makeText(MapsActivity.this, "Could not register User!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                } //END INSERT USER IN DB TABLE 'Users'

                                //************************
                                //FAIRE LE SINGLETON USER
                                //************************

                                //[FETCH] THE USER IN DATABASE
                                ref.get().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        //[CREATE] SINGLETON
                                        User currentUser = task2.getResult().getValue(User.class);
                                        ((UserClient) getApplicationContext()).setUser(currentUser);

                                        //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                        loginDialog.dismiss();

                                        // Toast
                                        Toast.makeText(MapsActivity.this, "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                    } else {
                                        //HANDLE ERROR HERE if we cannot retrieve the user data
                                        Toast.makeText(MapsActivity.this, "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                    }
                                }); //END CREATE SINGLETON
                            }//END onDataChanged

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            } //END onCancelled
                        }); //END DATABASE QUERY TO CHECK USER


                    } else {
                        Toast.makeText(MapsActivity.this, "Google error to Login", Toast.LENGTH_SHORT).show();
                    }// END SUCCESS LOGIN
                });//END GOOGLE SIGN IN  -----  END LISTENER SUCCESS LOGIN

        //REMOVE-HIDE PROGRESS BAR
        login_progressBar.setVisibility(View.GONE);
    }

    // Configure Google Sign In Request [SETUP]
    //*********************************
    private void createSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    //CREER INTENT POUR GOOGLE SIGN IN ACTIVITY [Quand on click sur le logo Google, check LoginDialog code]
    //*****************************************************************************************************
    private void signIn_CreateGoogleIntent() {
        // CREER INTENT POUR GOOGLE SIGN IN ACTIVITY
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();


        //LANCER L'ACTIVITY DE GOOGLE SIGN IN
        activityResultLaunch.launch(signInIntent);
    }


    //********Facebook Login*********\\
    //*********************************\\
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

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