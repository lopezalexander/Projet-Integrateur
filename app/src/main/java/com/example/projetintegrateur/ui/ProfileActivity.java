package com.example.projetintegrateur.ui;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.AppTheme;
import com.example.projetintegrateur.model.User;
import com.example.projetintegrateur.util.UserClient;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {


    ConstraintLayout profileLayout;


    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileLayout = findViewById(R.id.constraint_layout_profile);


        //HIDE DEFAULT ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();


        AppTheme theme = AppTheme.getInstance();
        profileLayout.setBackgroundColor(theme.getBackgroundColor());


        User user = ((UserClient) getApplicationContext()).getUser();
        ImageView profilePicture = findViewById(R.id.imageView_profile_picture);

        String photoURL = null;
        String name = null;
        String email = null;


        if (user != null) {
            photoURL = user.getPhotoUrl();
            name = user.getName();
            email = user.getEmail();
        }


        //SET/DISPLAY PROFILE INFORMATION
        //*****************************************************************************************************************************
        //Picasso.get().load(photoURL).into(profilePicture);

        TextView profileName = findViewById(R.id.textview_fullname);

        profileName.setText(name);


        TextView profileEmail = findViewById(R.id.textview_email);

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
            Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        TextView btn_infos = findViewById(R.id.textview_info);

        btn_infos.setOnClickListener(view -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(ProfileActivity.this);
            builder1.setMessage("Midway 1.0");
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "Ok",
                    (dialog, id) ->
                            dialog.cancel());

            AlertDialog alert11 = builder1.create();
            alert11.show();
        });

        TextView btn_parametre = findViewById(R.id.textview_parametres);

        btn_parametre.setOnClickListener(view -> {
            String[] themes = {"Muted Blue", "Midnight", "Black and White", "Ultra Light", "Blue Essence", "Default Map"};
            int[] colors = {getColor(R.color.blue1), getColor(R.color.black), getColor(R.color.white), getColor(R.color.grey), getColor(R.color.blueGreen), getColor(com.google.android.libraries.places.R.color.quantum_orange100)};
            int[] searchBar_colors = {getColor(R.color.blue1), getColor(R.color.black), getColor(R.color.white), getColor(R.color.grey), getColor(R.color.blueGreen), getColor(com.google.android.libraries.places.R.color.quantum_orange100)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            builder.setTitle("Choisissez un thème");
            builder.setItems(themes, (dialog, which) -> {
                //the user clicked on themes[which]
                MapsActivity.setMapStyle(themes[which], ProfileActivity.this);

                profileLayout.setBackgroundColor(colors[which]);
                //STORE COLOR IN SINGLETON
                AppTheme currentTheme = AppTheme.getInstance();
                currentTheme.setBackgroundColor(colors[which]);
                currentTheme.setSearchBar_backgroundColor(searchBar_colors[which]);
                Log.d(TAG, "onCreate: " + currentTheme.getSearchBar_backgroundColor());
            });
            builder.show();

        });


    }

}