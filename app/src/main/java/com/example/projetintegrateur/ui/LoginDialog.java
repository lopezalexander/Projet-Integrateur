package com.example.projetintegrateur.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.facebook.AccessToken;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class LoginDialog extends DialogFragment {

    //CAROUSEL
//    int[] carousel_Images = {R.drawable.page_one, R.drawable.page_two, R.drawable.page_three};

    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDB;

    //LOGIN PAGE VIEW ITEMS
    EditText email_input;
    EditText password_input;
    EditText password2_input;

    EditText name_input;

    LinearLayout name_layout;
    LinearLayout password2_layout;
    LinearLayout linearLayout_register;
    TextView link_register;
    LinearLayout linearLayout_login;
    TextView link_login;

    View view_line;

    //GOOGLE LOGIN
    private GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> activityResultLaunch;

    //FACEBOOK LOGIN
    private CallbackManager mCallbackManager;


    //****************\\
    //  OnCreateView   \\
    //******************************************************************************************************************************************************************************
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myFormView = inflater.inflate(R.layout.login_layout, container, false);

        //FIREBASE INSTANTIATION
        //*******************************************************************************************
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();

        //FACEBOOK CREATE CALLBACKMANAGER AND SIGNIN REQUEST
        //*******************************************************************************************
        createCallBackManager();

        //GOOGLE CREER LE SIGNIN REQUEST ET LE LAUNCHER DE L'ACTIVITY POUR LE SignIn INTENT
        //*******************************************************************************************
        createSignInRequest();


        //Setup Login Carousel
        //*******************************************************************************************
//        ViewPager login_Carousel = myFormView.findViewById(R.id.pager);
//        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(myFormView.getContext(), carousel_Images);
//        login_Carousel.setAdapter(mCustomPagerAdapter);


        //GET VIEW ELEMENTS AND SETUP CLICKLISTENER AND HIDEKEYBOARD ON EDITTEXT
        //*******************************************************************************************
        email_input = myFormView.findViewById(R.id.input_email);
        password_input = myFormView.findViewById(R.id.input_password);

        password2_input = myFormView.findViewById(R.id.input_password2);

        name_input = myFormView.findViewById(R.id.input_name);

        ImageView google_signIn_btn = myFormView.findViewById(R.id.btn_google);
        Button firebase_signIn_btn = myFormView.findViewById(R.id.btn_signIn);

        ImageView facebook_btn = myFormView.findViewById(R.id.btn_facebook);
        Button firebase_register_btn = myFormView.findViewById(R.id.btn_register);

        name_layout = myFormView.findViewById(R.id.linearLayout_name);
        password2_layout = myFormView.findViewById(R.id.linearLayout_password2);

        linearLayout_register = myFormView.findViewById(R.id.linearLayout_link_register);
        link_register = myFormView.findViewById(R.id.weblink_register);
        linearLayout_login = myFormView.findViewById(R.id.linearLayout_link_login);
        link_login = myFormView.findViewById(R.id.weblink_login);
        view_line = myFormView.findViewById(R.id.view_line);


        //SET HideKeyBoard() to EditText
        //*******************************************************************************************
        ArrayList<EditText> editTextList = new ArrayList<>();
        editTextList.add(email_input);
        editTextList.add(password_input);
        editTextList.add(password2_input);

        for (int i = 0; i < editTextList.size(); i++) {
            editTextList.get(i).setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            });
        }

        //SET BUTTON LISTENER FOR LOGIN IN OR REGISTERING USER with GOOGLE/FACEBOOK/EMAILPASSWORD & REGISTER
        //*******************************************************************************************
        //
        //GOOGLE SIGN IN LOGIC
        google_signIn_btn.setOnClickListener(view -> {
            //INSERT GOOGLE SIGN IN LOGIC HERE
            signIn_CreateGoogleIntent();
        });

        //
        //FIREBASE LOGIN LOGIC
        firebase_signIn_btn.setOnClickListener(view -> {
            //FIREBASE LOGIN FUNCTION
            loginUserFirebase();
        });

        //
        //FACEBOOK
        facebook_btn.setOnClickListener(view -> {
            //LAUNCH THE REQUEST/INTENT TO CONNECT TO FACEBOOK
            LoginManager.getInstance().logInWithReadPermissions(this, mCallbackManager, Arrays.asList("email", "public_profile"));
        });

        //
        //FIREBASE REGISTER FUNCTION
        firebase_register_btn.setOnClickListener(view -> registerUserFirebase());

        //LIEN POUR PAGE CREER COMPTE
        link_register.setOnClickListener(view -> {
            name_layout.setVisibility(View.VISIBLE);
            password2_layout.setVisibility(View.VISIBLE);
            firebase_register_btn.setVisibility(View.VISIBLE);
            firebase_signIn_btn.setVisibility(View.GONE);

            linearLayout_register.setVisibility(View.GONE);
            linearLayout_login.setVisibility(View.VISIBLE);

            view_line.setVisibility(View.GONE);
            email_input.setText("");
            password_input.setText("");
        });

        link_login.setOnClickListener(view -> {
            name_layout.setVisibility(View.GONE);
            password2_layout.setVisibility(View.GONE);
            firebase_register_btn.setVisibility(View.GONE);
            firebase_signIn_btn.setVisibility(View.VISIBLE);
            linearLayout_register.setVisibility(View.VISIBLE);
            linearLayout_login.setVisibility(View.GONE);
            view_line.setVisibility(View.VISIBLE);
            name_input.setText("");
            email_input.setText("");
            password_input.setText("");
            password2_input.setText("");
        });


        // END CREATE VIEW, RETURN IT TO THE CALLER --> MapsActivity
        //*******************************************************************************************
        return myFormView;
    }


    //****************\\
    //  FIREBASE AUTH  \\
    //******************************************************************************************************************************************************************************
    //
    //
    //  REGISTRATION EMAIL/PASSWORD
    //*******************************************************************************************************
    private void registerUserFirebase() {
        //GET LOGIN INPUT DATA
        String name = name_input.getText().toString().trim();
        String email = email_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();
        String password2 = password2_input.getText().toString().trim();
        boolean valid = loginValidation(name, email, password, password2);


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
                                                User newUserData = new User(email, currentUserKey, name, null);


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
                                                    Toast.makeText(getActivity(), "Bienvenue " + Objects.requireNonNull(name) + "!", Toast.LENGTH_LONG).show();

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
    //*******************************************************************************************************
    private void loginUserFirebase() {
        //GET LOGIN INPUT DATA
        String name = name_input.getText().toString().trim();
        String email = email_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();
        String password2 = password2_input.getText().toString().trim();
        boolean valid = loginValidation(name, email, password, password2);


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
                                            Toast.makeText(getActivity(), "Bienvenue " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName() + "!", Toast.LENGTH_LONG).show();

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
    //*******************************************************************************************************
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
                                    String name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
                                    String photoUrl = String.valueOf(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl());
                                    User newUserData = new User(email, currentUserKey, name, photoUrl);

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
                                        Toast.makeText(getActivity(), "Bienvenue " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName() + "!", Toast.LENGTH_LONG).show();

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

    //
    //
    //  LOGIN FACEBOOK
    //*******************************************************************************************************
    private void loginUserFacebookFirebase(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());


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
                                    String name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
                                    String photoUrl = String.valueOf(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl());
                                    User newUserData = new User(email, currentUserKey, name, photoUrl);

                                    //INSERT THE USER IN THE FIREBASE REALTIME DATABASE TABLE --> Users
                                    mFirebaseDB.getReference("Users")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                            .setValue(newUserData).addOnCompleteListener(task1 -> {
                                                //HANDLE ERROR, NOTHING ON SUCCESS...CONTINUE
                                                if (!task1.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Could not register User!", Toast.LENGTH_LONG).show();
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
                                        ((UserClient) requireContext().getApplicationContext()).setUser(currentUser);

                                        //ALLOW ACCESS TO APP, DISMISS THE LOGIN DIALOG
                                        dismiss();

                                        // Toast
                                        Toast.makeText(getContext(), "Bienvenue " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName() + "!", Toast.LENGTH_LONG).show();

                                    } else {
                                        //HANDLE ERROR HERE if we cannot retrieve the user data
                                        Toast.makeText(getContext(), "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
                                    }
                                }); //END CREATE SINGLETON
                            }//END onDataChanged

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            } //END onCancelled
                        }); //END DATABASE QUERY TO CHECK USER

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FacebookActivity", "signInWithCredential:failure", task.getException());


                    }
                });
    }


    //********************\\
    //  UTILITY FUNCTIONS  \\
    //*****************************************************************************************************************************************************************************
    //
    //
    //  HIDE KEYBOARD
    //*******************************************************************************************************
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //
    //
    //  LOGIN/REGISTRATION VALIDATION
    //*******************************************************************************************************

    private boolean loginValidation(String name, String email, String password, String password2) {
        //VALIDATIONS
        if (name.isEmpty()) {
            name_input.setError("Entrez votre nom!");
            name_input.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            email_input.setError("Entrez votre courriel!");
            email_input.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Votre courriel est incorrect");
            email_input.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            password_input.setError("Un mot de passe est nécessaire!");
            password_input.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            password_input.setError("Le mot de passe requiert 6 lettres au minimum!");
            password_input.requestFocus();
            return false;
        }

        if (!password.equals(password2)) {
            password2_input.setError("Confirmation du mot de passe ne correspond pas. Resaisissez votre mot de passe.");
            password2_input.requestFocus();
            return false;
        }

        //If no error, return TRUE
        return true;
    }

    //
    //
    // Configure Google Sign In Request [SETUP]
    //*******************************************************************************************************
    private void createSignInRequest() {
        //CREATE THE SIGN IN REQUEST
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        //CREATE THE ACTIVITY RESULT FOR THE GOOGLE SIGN IN
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
    }

    //
    //
    //CREER INTENT POUR GOOGLE SIGN IN ACTIVITY [Quand on click sur le logo Google, check LoginDialog code]
    //*****************************************************************************************************
    private void signIn_CreateGoogleIntent() {
        // CREER INTENT POUR GOOGLE SIGN IN ACTIVITY
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        //LANCER L'ACTIVITY DE GOOGLE SIGN IN
        activityResultLaunch.launch(signInIntent);

    }

    //
    //
    // CONFIGURE CALLBACK MANAGER FOR FACEBOOK LOGIN AND HANDLE THE FACEBOOK LOGIN RESULT
    //*******************************************************************************************************
    private void createCallBackManager() {
        //CREATE A CALLBACKMANAGER FOR FACEBOOK LOGIN
        mCallbackManager = CallbackManager.Factory.create();

        //REGISTER THE CALLBACKMANAGER, THE CALLBACK WILL BE LAUNCH AFTER A REQUEST TO LOG TO FACEBOOK IS DONE
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //ONCE WE HAVE A SUCCESSFULL LOGIN AT FACEBOOK, TAKE THE TOKEN AND PROCEED WITH FIREBASE mAUTH SIGNIN
                loginUserFacebookFirebase(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(@NonNull FacebookException e) {
            }
        });
    }


}
