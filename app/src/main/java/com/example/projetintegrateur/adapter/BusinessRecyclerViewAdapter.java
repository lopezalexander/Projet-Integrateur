package com.example.projetintegrateur.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.BusinessModel;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BusinessRecyclerViewAdapter extends RecyclerView.Adapter<BusinessRecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<BusinessModel> businessData;
    DataTransferInterfaceRecycler mListener;

    public BusinessRecyclerViewAdapter(Context context, ArrayList<BusinessModel> businessData, DataTransferInterfaceRecycler mListener) {
        this.context = context;
        this.businessData = businessData;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public BusinessRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.business_recycler_view_row, parent, false);

        return new BusinessRecyclerViewAdapter.MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessRecyclerViewAdapter.MyViewHolder holder, int position) {
        if (businessData.get(position).getPhotoURL() != null) {
            String photoURL = businessData.get(position).getPhotoURL();
            String photoURL2 = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=" + photoURL + "&key=" + context.getString(R.string.maps_key);
            Picasso.get().load(photoURL2).into(holder.businesImage);
        } else {
            holder.businesImage.setImageResource(R.drawable.ic_restaurant_foreground);
        }
        holder.businessName.setText(businessData.get(position).getName());
        holder.businessAddress.setText(businessData.get(position).getAddress());
        holder.businessRating.setText(businessData.get(position).getRating());


        holder.btn_chooseBusiness.setOnClickListener(v -> {
            if (!businessData.get(position).getName().equals("No Results")) {
                LatLng businessCoordinate = businessData.get(position).getCoordinatesLatlng();
                String businessAddressName = businessData.get(position).getAddress();
                String businessName = businessData.get(position).getName();
                String businessPhoto = businessData.get(position).getPhotoURL();
                mListener.getSelectedBusinnes(businessCoordinate, businessAddressName, businessName, businessPhoto);
            }

        });

    }

    @Override
    public int getItemCount() {
        return businessData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView businesImage, btn_chooseBusiness;
        TextView businessName, businessAddress, businessRating;
        DataTransferInterfaceRecycler mListener;

        public MyViewHolder(@NonNull View itemView, DataTransferInterfaceRecycler mListener) {
            super(itemView);
            businesImage = itemView.findViewById(R.id.businessImage);
            btn_chooseBusiness = itemView.findViewById(R.id.chooseBusiness);
            businessName = itemView.findViewById(R.id.businessName);
            businessAddress = itemView.findViewById(R.id.businessAddress);
            businessRating = itemView.findViewById(R.id.businessRating);

            this.mListener = mListener;


        }
    }

    public interface DataTransferInterfaceRecycler {
        void getSelectedBusinnes(LatLng businessCoordinate, String businessAddressName, String businessName, String businessPhoto);
    }
}
