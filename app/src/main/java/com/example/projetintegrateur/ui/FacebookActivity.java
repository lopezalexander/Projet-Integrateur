package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Objects;

public class FacebookActivity extends MapsActivity {

    //FACEBOOK LOGIN
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        //CREATE A CALLBACKMANAGER FOR FACEBOOK LOGIN
        mCallbackManager = CallbackManager.Factory.create();

        //REGISTER THE CALLBACKMANAGER, THE CALLBACK WILL BE LAUNCH AFTER A REQUEST TO LOG TO FACEBOOK IS DONE
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(@NonNull FacebookException e) {

            }
        });


        //LAUNCH THE REQUEST/INTENT TO CONNECT TO FACEBOOK
        LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("email"));


    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
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
                                                    Toast.makeText(FacebookActivity.this, "Could not register User!", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(FacebookActivity.this, "Welcome to MidWay!!", Toast.LENGTH_LONG).show();

                                    } else {
                                        //HANDLE ERROR HERE if we cannot retrieve the user data
                                        Toast.makeText(FacebookActivity.this, "Failed to query your data, please try again!", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


}