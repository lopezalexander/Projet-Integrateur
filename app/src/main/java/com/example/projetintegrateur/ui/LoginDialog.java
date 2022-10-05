package com.example.projetintegrateur.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.CustomPagerAdapter;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class LoginDialog extends DialogFragment {


    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;

    //LOGIN PAGE VIEW ITEMS
    EditText email_input;
    EditText password_input;


    //GOOGLE LOGIN
    private GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> activityResultLaunch;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myFormView = inflater.inflate(R.layout.login_layout, container, false);


        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();


        //GOOGLE CREER LE SIGNIN REQUEST ET LE LAUNCHER DE L'ACTIVITY POUR LE SignIn INTENT
        createSignInRequest();

        activityResultLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
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


        //Setup Login Carousel
        //********************
        ViewPager login_Carousel = myFormView.findViewById(R.id.pager);
        int[] carousel_Images = {R.drawable.page_one, R.drawable.page_two, R.drawable.page_three};
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(myFormView.getContext(), carousel_Images);
        login_Carousel.setAdapter(mCustomPagerAdapter);


        //GET VIEW ELEMENTS AND SETUP CLICKLISTENER AND HIDEKEYBOARD ON EDITTEXT
        //**********************************************************************
        email_input = myFormView.findViewById(R.id.input_email);
        password_input = myFormView.findViewById(R.id.input_password);

        ImageView google_signIn_btn = myFormView.findViewById(R.id.btn_google);
        Button firebase_signIn_btn = myFormView.findViewById(R.id.btn_signIn);

        ImageView facebook_btn = myFormView.findViewById(R.id.btn_facebook);
        Button firebase_register_btn = myFormView.findViewById(R.id.btn_register);


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


        //FIREBASE LOGIN LOGIC
        firebase_signIn_btn.setOnClickListener(view -> {
            //FIREBASE LOGIN FUNCTION
            loginUserFirebase();
        });

        //FACEBOOK
        facebook_btn.setOnClickListener(view -> {
            Intent facebookIntent = new Intent(myFormView.getContext(), FacebookActivity.class);
            facebookIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(facebookIntent);
        });


        //FIREBASE REGISTER FUNCTION
        //*****************************************************
        firebase_register_btn.setOnClickListener(view -> registerUserFirebase());


        return myFormView;
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
                                                                Toast.makeText(getActivity(), "Could not register User!", Toast.LENGTH_LONG).show();
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
                                                    ((UserClient) requireActivity().getApplicationContext()).setUser(currentUser);

                                                    //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                                    dismiss();

                                                    // Toast
                                                    Toast.makeText(getActivity(), "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                                } else {
                                                    //HANDLE ERROR HERE if we cannot retrieve the user data
                                                    Toast.makeText(getActivity(), "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                                }
                                            }); //END CREATE SINGLETON
                                        }//END onDataChanged

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        } //END onCancelled
                                    }); //END DATABASE QUERY TO CHECK USER
                                } else {
                                    Toast.makeText(getActivity(), "Could not register User!", Toast.LENGTH_LONG).show();
                                }

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
                                            ((UserClient) requireActivity().getApplicationContext()).setUser(currentUser);

                                            //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                            dismiss();

                                            // Toast
                                            Toast.makeText(getActivity(), "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                        } else {
                                            //HANDLE ERROR HERE if we cannot retrieve the user data
                                            Toast.makeText(getActivity(), "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(), "Failed to login! Check your credentials.", Toast.LENGTH_LONG).show();
                                }

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


        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
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
                                                    Toast.makeText(getActivity(), "Could not register User!", Toast.LENGTH_LONG).show();
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
                                        ((UserClient) requireActivity().getApplicationContext()).setUser(currentUser);

                                        //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG

                                        dismiss();

                                        // Toast
                                        Toast.makeText(getActivity(), "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                    } else {
                                        //HANDLE ERROR HERE if we cannot retrieve the user data
                                        Toast.makeText(getActivity(), "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                    }
                                }); //END CREATE SINGLETON
                            }//END onDataChanged

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            } //END onCancelled
                        }); //END DATABASE QUERY TO CHECK USER


                    } else {
                        Toast.makeText(getActivity(), "Google error to Login", Toast.LENGTH_SHORT).show();
                    }// END SUCCESS LOGIN
                });//END GOOGLE SIGN IN  -----  END LISTENER SUCCESS LOGIN


    }

    // Configure Google Sign In Request [SETUP]
    //*********************************
    private void createSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }


    //CREER INTENT POUR GOOGLE SIGN IN ACTIVITY [Quand on click sur le logo Google, check LoginDialog code]
    //*****************************************************************************************************
    private void signIn_CreateGoogleIntent() {
        // CREER INTENT POUR GOOGLE SIGN IN ACTIVITY
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();


        //LANCER L'ACTIVITY DE GOOGLE SIGN IN
        activityResultLaunch.launch(signInIntent);
    }


    //********************\\
    //  UTILITY FUNCTIONS  \\
    //*******************************************************************************************************************************************
    //
    //
    //  HIDE KEYBOARD
    //************************************
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
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
