package com.example.projetintegrateur.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.projetintegrateur.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // CACHER LE ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

        //CREATION D'UN HANDLER DE SYSTEME POUR CREER UN DELAI AVANT LE PASSAGE VERS LA PAGE PRINCIPAL --> MapsActivity
        final Handler handler = new Handler();
        final Runnable r = this::goToHome;

        //CREATION DU DELAI AVEC LA FONCTION EN CALLBACK APRES 1000ms
        handler.postDelayed(r, 1000);
    }

    private void goToHome() {
        //GO TO MapsActivity
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);

        //FERMER MainActivity POUR PREVENIR LE RETOUR EN ARRIERE
        finish();
    }


// HOW TO CREATE THE RUNNABLE FUNCTION BEFORE MAKING IT SHORT
//**************************************************************\\
//
//
//================1================\\
//        final Runnable r = new Runnable() {
//            public void run() {
//                goToHome();
//            }
//        };
//
//
//================2================\\
//        final Runnable r = () -> goToHome();
//
//
//================3================\\
//        final Runnable r = this::goToHome;
}