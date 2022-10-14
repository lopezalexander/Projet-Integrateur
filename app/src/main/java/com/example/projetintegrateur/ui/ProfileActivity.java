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
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {


    ConstraintLayout profileLayout;

    public FirebaseDatabase mFirebaseDB;


    @Override
    protected void onStart() {
        super.onStart();
        AppTheme theme = AppTheme.getInstance();
        Log.d("PROFILE", theme.getTheme().toString());
        Log.d("PROFILE", String.valueOf(theme.getBackgroundColor()));
        profileLayout.setBackgroundColor(theme.getBackgroundColor());
    }

    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileLayout = findViewById(R.id.constraint_layout_profile);

        //GET REALTIME DATABASE INSTANCE
        mFirebaseDB = FirebaseDatabase.getInstance();

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
        Picasso.get().load(photoURL).into(profilePicture);

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
            AlertDialog.Builder builder1 = new AlertDialog.Builder(ProfileActivity.this, R.style.AlertDialogCustom);
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
            int[] colors = {getColor(R.color.blue1), getColor(R.color.blue6), getColor(R.color.white), getColor(R.color.grey), getColor(R.color.blueGreen), getColor(com.google.android.libraries.places.R.color.quantum_orange100)};
            int[] searchBar_colors = {getColor(R.color.blue4), getColor(R.color.blue6), getColor(R.color.white), getColor(R.color.grey), getColor(R.color.blueGreen), getColor(com.google.android.libraries.places.R.color.quantum_orange100)};
            int[] buttons_Drawables = {R.drawable.icon_container_settings, R.drawable.icon_container_settings2, R.drawable.icon_container_settings3, R.drawable.icon_container_settings4, R.drawable.icon_container_settings5, R.drawable.icon_container_settings6};

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            builder.setTitle("Choisissez un thème");
            builder.setItems(themes, (dialog, which) -> {
                //the user clicked on themes[which]
                MapsActivity.setMapStyle(themes[which], getApplicationContext());
                profileLayout.setBackgroundColor(colors[which]);
                //STORE COLOR IN SINGLETON
                AppTheme currentTheme = AppTheme.getInstance();
                currentTheme.setTheme(themes[which]);
                currentTheme.setBackgroundColor(colors[which]);
                currentTheme.setSearchBar_backgroundColor(searchBar_colors[which]);
                currentTheme.setButtonBg(buttons_Drawables[which]);
                Log.d(TAG, "onCreate: " + currentTheme.getSearchBar_backgroundColor());

                //INSERT THE USER IN THE FIREBASE REALTIME DATABASE TABLE --> Users
                mFirebaseDB.getReference("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("theme")
                        .setValue(currentTheme).addOnCompleteListener(task1 -> {
                        });

            });
            builder.show();

        });


    }

}