package com.example.projetintegrateur.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.BusinessRecyclerViewAdapter;
import com.example.projetintegrateur.model.BusinessModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class BusinessDialog extends BottomSheetDialogFragment {

    ArrayList<BusinessModel> businessData;

    public BusinessDialog(ArrayList<BusinessModel> businessData) {
        this.businessData = businessData;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myFormView = inflater.inflate(R.layout.business_dialog_layout, container, false);


        //CREATE RECYCLERVIEW
        RecyclerView recyclerView = myFormView.findViewById(R.id.recycleView_business);

        //CREATE ADAPTER FOR RECYCLERVIEW CONTENT
        BusinessRecyclerViewAdapter adapter = new BusinessRecyclerViewAdapter(myFormView.getContext(), businessData);

        //SET ADAPTER AND SET THE LAYOUTMANAGER
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(myFormView.getContext()));

        return myFormView;
    }
}
