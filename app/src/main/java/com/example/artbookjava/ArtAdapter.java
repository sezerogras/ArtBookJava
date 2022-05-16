package com.example.artbookjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artbookjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder>

{
    ArrayList<Art> artArrayList;
   public int position;

    public ArtAdapter(ArrayList<Art> artArrayList){
        this.artArrayList=artArrayList;

    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(binding);
    }

    @Override
    public void onBindViewHolder(  ArtAdapter.ArtHolder holder,  int  position) {
        this.position = holder.getAdapterPosition();
        holder.binding.recyclerViewTextView.setText(artArrayList.get(holder.getAdapterPosition()).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class );


                intent.putExtra("artId",artArrayList.get(holder.getAdapterPosition()).id);
                intent.putExtra("info","old");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size(); // kaç eleman var ise onu bana göstermiş olacak
    }

    public static class  ArtHolder extends RecyclerView.ViewHolder{

        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());  // diyerek görünümü alabilirim
            this.binding = binding;


        }
    }

}
