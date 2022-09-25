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

        Objects.requireNonNull(getSupportActionBar()).hide();

        final Handler handler = new Handler();
        final Runnable r = this::goToHome;

        handler.postDelayed(r, 1000);
    }

    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
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