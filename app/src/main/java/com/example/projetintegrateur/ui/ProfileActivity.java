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

        //HIDE DEFAULT ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageView photoProfil = findViewById(R.id.imageView_profile_picture);
        TextView titreProfil = findViewById(R.id.textview_fullname);
        TextView emailProfil = findViewById(R.id.textview_email);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    try {
                        //GET PROFILE INFOS
                        String nomComplet = Objects.requireNonNull(object).getString("name");
                        String email = Objects.requireNonNull(object).getString("email");
                        String imageURL = object.getJSONObject("picture").getJSONObject("data").getString("url");

                        //SET PROFILE INFOS
                        titreProfil.setText(nomComplet);
                        emailProfil.setText(email);
                        Picasso.get().load(imageURL).into(photoProfil);

                        Log.d("TAG9", "onCreate: " + nomComplet);
                        Log.d("TAG9", "onCreate: " + email);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
        );
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,email,name,link,picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
    }

}