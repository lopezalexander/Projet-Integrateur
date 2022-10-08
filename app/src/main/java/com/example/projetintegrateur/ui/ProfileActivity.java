package com.example.projetintegrateur.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        //HIDE DEFAULT ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

        User user = ((UserClient) getApplicationContext()).getUser();
        ImageView profilePicture = findViewById(R.id.imageView_profile_picture);
        String photoURL = user.getPhotoUrl();

        //Set profile picture
        Picasso.get().load(photoURL).into(profilePicture);

        TextView profileName = findViewById(R.id.textview_fullname);
        String name = user.getName();
        profileName.setText(name);

        TextView profileEmail = findViewById(R.id.textview_email);
        String email = user.getEmail();
        profileEmail.setText(email);

    }


}