package com.example.projetintegrateur.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.HistoryRecyclerViewAdapter;
import com.example.projetintegrateur.model.ItineraryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Objects;

public class HistoryListActivity extends AppCompatActivity {

    //FIREBASE
    public FirebaseAuth mAuth;
    public FirebaseDatabase mFirebaseDB;

    //LIST VARIABLES
    ArrayList<ItineraryModel> historyList;


    //***********\\
    //  OnCREATE  \\
    //******************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);

        //HIDE DEFAULT ACTION BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

        //DECLASE VIEW ITEM AND ETC.
        initView();

        //GET CURRENT USER HISTORY LIST OF ITINERARY
        getHistoryList();


    }


    //
    //
    //
    //
    //
    //
    //
    //
    //
    //*****************\\
    //  FUNCTIONS       \\
    //******************************************************************************************************************************************************************************

    //
    //
    //  GET HISTORY LIST OF ITINERARY FROM FIREBASE
    //*****************************************************************************************************************************
    public void getHistoryList() {
        //GET CURRENT USER KEY
        String currentUserkey = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        //QUERY THE HISTORY LIST OF THE CURRENT USER FROM FIREBASE
        DatabaseReference ref = mFirebaseDB.getReference("Itinerary");
        ref.child(currentUserkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot UniqueItinerary : snapshot.getChildren()) {
                    historyList.add(UniqueItinerary.getValue(ItineraryModel.class));
                }

                //SET RECyCLERVIEW
                setRecyclerView();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    //
    //
    //  SET RECYCLER VIEW
    //*****************************************************************************************************************************
    private void setRecyclerView() {
        //CREATE RECYCLERVIEW
        RecyclerView recyclerView = findViewById(R.id.recyclerView_history);

        //CREATE ADAPTER FOR RECYCLERVIEW CONTENT
        HistoryRecyclerViewAdapter adapter = new HistoryRecyclerViewAdapter(this, historyList);

        //SET ADAPTER AND SET THE LAYOUTMANAGER
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //
    //
    //
    //
    //
    //
    //
    //
    //
    //*****************************\\
    //      SETUP FUNCTIONS         \\
    //******************************************************************************************************************************************************************************

    //
    //
    //  SETUP VIEW
    //*****************************************************************************************************************************
    public void initView() {
        //INSTANSTIATE ARRAYLIST OF ITINERARY
        historyList = new ArrayList<>();

        //INSTANSTIATE FIREBASE AUTH & DB
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();


    }


}