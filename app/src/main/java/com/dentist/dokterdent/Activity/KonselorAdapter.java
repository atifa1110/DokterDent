package com.dentist.dokterdent.Activity;

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
import com.dentist.dokterdent.Chat.ChatActivity;
import com.dentist.dokterdent.Model.Extras;
import com.dentist.dokterdent.Model.KonselorModel;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.Model.Preference;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class KonselorAdapter extends RecyclerView.Adapter<KonselorAdapter.KonselorViewHolder> {

    private Context context;
    private List<KonselorModel> konselorList;

    private DatabaseReference databaseReferenceChats;
    private FirebaseUser currentUser;

    public KonselorAdapter(Context context, List<KonselorModel> konselorList) {
        this.context = context;
        this.konselorList = konselorList;
    }

    @NonNull
    @NotNull
    @Override
    public KonselorViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_konselor_list,parent,false);
        return new KonselorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull KonselorAdapter.KonselorViewHolder holder, int position) {
        KonselorModel konselor = konselorList.get(position);

        holder.tvNamaKonselor.setText(konselor.getNama());
        holder.tvOnline.setText(konselor.getStatus());

        if (konselor.getStatus().equals("Online")){
            holder.ivCircle.setImageDrawable(context.getDrawable(R.drawable.ic_circle_green));
        }else{
            holder.tvOnline.setTextColor(context.getResources().getColor(R.color.gray));
            holder.ivCircle.setImageDrawable(context.getDrawable(R.drawable.ic_circle_gray));
        }

        Glide.with(context)
                .load(konselor.getPhoto())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(holder.ivProfileKonselor);

        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ChatActivity.class);
                Preference.setKeyChatId(context,konselor.getId());
                Preference.setKeyChatName(context,konselor.getNama());
                Preference.setKeyChatPhoto(context,konselor.getPhoto());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return konselorList.size();
    }

    public class KonselorViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivProfileKonselor , ivCircle;
        private TextView tvNamaKonselor , tvOnline;

        public KonselorViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileKonselor = itemView.findViewById(R.id.iv_profile_konselor);
            ivCircle = itemView.findViewById(R.id.iv_circle);
            tvNamaKonselor = itemView.findViewById(R.id.tv_nama_konselor);
            tvOnline = itemView.findViewById(R.id.tv_online);
        }
    }
}

