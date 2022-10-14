package com.example.projetintegrateur.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.CustomPagerAdapter;


public class TutorialDialog extends DialogFragment {

    //CAROUSEL
    int[] carousel_Images = {R.drawable.btns_tutorial, R.drawable.origin_destination_tutorial, R.drawable.midpoint_tutorial};

    ImageView btn_close_tutorial;

    //****************\\
    //  OnCreateView   \\
    //******************************************************************************************************************************************************************************
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myFormView = inflater.inflate(R.layout.tutorial_layout, container, false);




        //Setup Login Carousel
        //*******************************************************************************************
        ViewPager tutorial_carousel = myFormView.findViewById(R.id.pager_item);
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(myFormView.getContext(), carousel_Images);
        tutorial_carousel.setAdapter(mCustomPagerAdapter);


        btn_close_tutorial = myFormView.findViewById(R.id.ic_close);
        btn_close_tutorial.setOnClickListener(view -> {
            dismiss();
        });

        // END CREATE VIEW, RETURN IT TO THE CALLER --> MapsActivity
        //*******************************************************************************************
        return myFormView;
    }





}
