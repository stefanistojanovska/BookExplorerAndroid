package com.mpip.bookexplorer.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mpip.bookexplorer.Holders.CustomListViewHolder;
import com.mpip.bookexplorer.MainActivity;
import com.mpip.bookexplorer.Models.Book;
import com.mpip.bookexplorer.R;
import com.squareup.picasso.Picasso;


import java.util.List;

public class CustomListAdapter extends RecyclerView.Adapter {
    //type of data
    List<Book> data;


    public CustomListAdapter(List<Book> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,parent,false);
        return new CustomListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CustomListViewHolder)holder).setData(data.get(position));
        Picasso.get().load(data.get(position).getPoster()).into(((CustomListViewHolder) holder).imageView);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}