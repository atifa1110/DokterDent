package com.dentist.dokterdent.Info;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Topiks;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.R;
import com.dentist.dokterdent.Utils.Util;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TopikAdapter extends RecyclerView.Adapter<TopikAdapter.TopikViewHolder>{

    private Context context;
    private List<Topiks> topiksList;

    public TopikAdapter(Context context, List<Topiks> topiksList) {
        this.context = context;
        this.topiksList = topiksList;
    }

    public TopikAdapter(Context context, List<Topiks> topiksList, int limit) {
        this.context = context;
        this.topiksList = topiksList;
    }

    @NonNull
    @NotNull
    @Override
    public TopikViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_topik_hangat,parent,false);
        return new TopikViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TopikViewHolder holder, int position) {
        Topiks topiks = topiksList.get(position);

        try{
            if(topiks.getJudul().isEmpty()){
                holder.topikName.setText("");
            }else {
                holder.topikName.setText(topiks.getJudul());
            }

            String narasi = "";
            narasi = topiks.getNarasi().length()>100?topiks.getNarasi().substring(0,100):topiks.getNarasi();

            if(topiks.getNarasi().isEmpty()){
                holder.topikNarasi.setText(" ");
            }else {
                holder.topikNarasi.setText(narasi + "...");
            }

            if(topiks.getTimestamp()==null){
                holder.time.setText("");
            }else {
                holder.time.setText(Util.getDay(topiks.getTimestamp()));
            }

            Glide.with(context)
                    .load(topiks.getPhoto())
                    .placeholder(R.drawable.ic_add_photo)
                    .error(R.drawable.ic_add_photo)
                    .into(holder.photoName);

        }catch (Exception e){
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailTopikActivity.class);
                intent.putExtra(Extras.TOPIK, new Topiks(topiks.getJudul(), topiks.getPhoto(), topiks.getNarasi(), topiks.getSumber(), topiks.getTimestamp(), topiks.getTipe()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topiksList.size();
    }

    public class TopikViewHolder extends RecyclerView.ViewHolder{

        private ImageView photoName;
        private TextView topikName,time,topikNarasi;

        public TopikViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            photoName = itemView.findViewById(R.id.iv_photo_topik);
            topikName = itemView.findViewById(R.id.tv_topik);
            topikNarasi = itemView.findViewById(R.id.tv_topik_narasi);
            time = itemView.findViewById(R.id.tv_time_topik);
        }
    }
}
