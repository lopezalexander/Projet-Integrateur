package com.example.projetintegrateur.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {


    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //HIDE DEFAULT ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

        User user = ((UserClient) getApplicationContext()).getUser();
        ImageView profilePicture = findViewById(R.id.imageView_profile_picture);
        String photoURL = user.getPhotoUrl();

        //SET/DISPLAY PROFILE INFORMATION
        //*****************************************************************************************************************************
        Picasso.get().load(photoURL).into(profilePicture);

        TextView profileName = findViewById(R.id.textview_fullname);
        String name = user.getName();
        profileName.setText(name);

        TextView profileEmail = findViewById(R.id.textview_email);
        String email = user.getEmail();
        profileEmail.setText(email);


        //BUTTON HISTORY OnClickListener
        //*****************************************************************************************************************************
        TextView btn_history = findViewById(R.id.btn_history);

        btn_history.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HistoryListActivity.class);
            startActivity(intent);
        });

        TextView btn_logOut = findViewById(R.id.textview_log_out);

        btn_logOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
        });


    }


}