package com.example.projetintegrateur.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.BusinessModel;
import com.example.projetintegrateur.ui.MapsActivity;

import java.util.ArrayList;

public class BusinessRecyclerViewAdapter extends RecyclerView.Adapter<BusinessRecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<BusinessModel> businessData;

    public BusinessRecyclerViewAdapter(Context context, ArrayList<BusinessModel> businessData) {
        this.context = context;
        this.businessData = businessData;
    }

    @NonNull
    @Override
    public BusinessRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.business_recycler_view_row, parent, false);

        return new BusinessRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.businesImage.setImageResource(R.drawable.ic_restaurant_foreground);
        holder.businessName.setText(businessData.get(position).getName());
        holder.businessAddress.setText(businessData.get(position).getAddress());
        holder.businessRating.setText(businessData.get(position).getRating());

        holder.btn_chooseBusiness.setOnClickListener(v -> {
            //test
            Toast.makeText(v.getContext(), "UNABLE TO FULFILL REQUEST, Try a shorter distance!!", Toast.LENGTH_LONG).show();



        });

    }

    @Override
    public int getItemCount() {
        return businessData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView businesImage, btn_chooseBusiness;
        TextView businessName, businessAddress, businessRating;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            businesImage = itemView.findViewById(R.id.businessImage);
            btn_chooseBusiness = itemView.findViewById(R.id.chooseBusiness);
            businessName = itemView.findViewById(R.id.businessName);
            businessAddress = itemView.findViewById(R.id.businessAddress);
            businessRating = itemView.findViewById(R.id.businessRating);


        }
    }
}
