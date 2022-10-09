package com.example.projetintegrateur.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetintegrateur.R;
import com.example.projetintegrateur.model.ItineraryModel;
import com.example.projetintegrateur.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        View view = inflater.inflate(R.layout.history_recycler_view_row, parent, false);

        return new HistoryRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewAdapter.MyViewHolder holder, int position) {
        //BIND DATA HERE TO VIEW ELEMENTS
        holder.listNumber.setText(String.valueOf(position + 1));
        holder.address1.setText(historyList.get(position).getOriginAddressName());
        holder.address2.setText(historyList.get(position).getDestinationAddressName());


        List<String> selectedBusinessAddressNameSplit = Arrays.asList(historyList.get(position).getSelectedBusinessAddressName().split(","));
        holder.selectedBusiness.setText(selectedBusinessAddressNameSplit.get(0));
        holder.historyDate.setText(historyList.get(position).getCurrentDate());
        holder.btn_chooseHistoryItem.setOnClickListener(v -> {
            //GET THE CHOSEN ITINERARY
            ItineraryModel ItineraryItemToSend = historyList.get(position);

            //START THE ACTIVITY
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("selectedHistory", ItineraryItemToSend);
            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        //DECLARE VIEW ELEMENT HERE
        ImageView btn_chooseHistoryItem;
        TextView address1, address2, selectedBusiness, historyDate, listNumber;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //FIND ID IN VIEW OF ELEMENT DECLARED ABOVE
            address1 = itemView.findViewById(R.id.address1);
            address2 = itemView.findViewById(R.id.address2);
            selectedBusiness = itemView.findViewById(R.id.selectedBusiness);
            historyDate = itemView.findViewById(R.id.historyDate);
            btn_chooseHistoryItem = itemView.findViewById(R.id.chooseHistoryItem);
            listNumber = itemView.findViewById(R.id.listNumber);

        }
    }
}
