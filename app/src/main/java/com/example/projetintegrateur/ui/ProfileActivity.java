package com.example.projetintegrateur.ui;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetintegrateur.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView photoProfil = findViewById(R.id.imageView_profile_picture);
        TextView titreProfil = findViewById(R.id.textview_fullname);


        //HIDE DEFAULT ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    try {
                        String fullname = Objects.requireNonNull(object).getString("name");
                        //Set name textview in profile activity
                        Log.d("TAG", "onCreate: "+fullname);
                        titreProfil.setText(fullname);

                        String pictureUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                        //Set profile picture
                        Picasso.get().load(pictureUrl).into(photoProfil);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }
}