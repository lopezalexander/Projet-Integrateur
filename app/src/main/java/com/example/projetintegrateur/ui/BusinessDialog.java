package com.example.projetintegrateur.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.adapter.BusinessRecyclerViewAdapter;
import com.example.projetintegrateur.model.BusinessModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class BusinessDialog extends BottomSheetDialogFragment implements BusinessRecyclerViewAdapter.DataTransferInterfaceRecycler {

    ArrayList<BusinessModel> businessData;
    LatLng selectedBusinnesCoordinates;

    DataTransferInterfaceDialog mListener;

    public BusinessDialog(ArrayList<BusinessModel> businessData) {

        this.businessData = businessData;
//        mListener

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myFormView = inflater.inflate(R.layout.business_dialog_layout, container, false);


        //CREATE RECYCLERVIEW
        RecyclerView recyclerView = myFormView.findViewById(R.id.recycleView_business);

        //CREATE ADAPTER FOR RECYCLERVIEW CONTENT
        BusinessRecyclerViewAdapter adapter = new BusinessRecyclerViewAdapter(myFormView.getContext(), businessData, this);

        //SET ADAPTER AND SET THE LAYOUTMANAGER
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(myFormView.getContext()));


        return myFormView;
    }

    @Override
    public void getSelectedBusinnes(LatLng businessCoordinate) {
        selectedBusinnesCoordinates = businessCoordinate;
        Log.d("TEST", String.valueOf(selectedBusinnesCoordinates.latitude) + ", " + String.valueOf(selectedBusinnesCoordinates.longitude));

    }

    public interface DataTransferInterfaceDialog {
        public void getSelectedBusinnes(LatLng businessCoordinate);
    }
}
