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
import com.example.projetintegrateur.model.ItineraryModel;

import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.MyViewHolder> {
    Context context;
    ArrayList<ItineraryModel> historyList;

    public HistoryRecyclerViewAdapter(Context context, ArrayList<ItineraryModel> historyList) {
        this.context = context;
        this.historyList = historyList;
    }


    @NonNull
    @Override
    public HistoryRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        //INSERT ROW LAYOUT OF THE RECYCLER HERE
        View view = inflater.inflate(R.layout.business_recycler_view_row, parent, false);

        return new HistoryRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewAdapter.MyViewHolder holder, int position) {

        //BIND DATA HERE TO VIEW ELEMENTS

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        //DECLARE VIEW ELEMENT HERE
        ImageView businesImage, btn_chooseBusiness;
        TextView businessName, businessAddress, businessRating;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //FIND ID IN VIEW OF ELEMENT DECLARED ABOVE
            businesImage = itemView.findViewById(R.id.businessImage);
            btn_chooseBusiness = itemView.findViewById(R.id.chooseBusiness);
            businessName = itemView.findViewById(R.id.businessName);
            businessAddress = itemView.findViewById(R.id.businessAddress);
            businessRating = itemView.findViewById(R.id.businessRating);


        }
    }
}
