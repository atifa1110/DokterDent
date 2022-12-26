package com.dentist.dokterdent.Chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Chats;
import com.dentist.dokterdent.Utils.NodeNames;
import com.dentist.dokterdent.Utils.Preference;
import com.dentist.dokterdent.Utils.Util;
import com.dentist.dokterdent.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private List<Chats> chatlist;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();;

    public ChatAdapter(Context context, List<Chats> chatlist) {
        this.context = context;
        this.chatlist = chatlist;
    }

    @NonNull
    @NotNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list,parent,false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatAdapter.ChatViewHolder holder, int position) {
        Chats chats = chatlist.get(position);

        try{
            if(chats.getName().isEmpty()){
                holder.userName.setText("");
            }else{
                holder.userName.setText(chats.getName());
            }

            if(chats.getPhoto().isEmpty()) {
                holder.ivProfile.setImageResource(R.drawable.ic_user);
            }else{
                Glide.with(context)
                        .load(chats.getPhoto())
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(holder.ivProfile);
            }

            String message = "";
            message = chats.getLastMessage().length()>30?chats.getLastMessage().substring(0,30): chats.getLastMessage();

            if(message.isEmpty()) {
                holder.lastMessage.setText("");
            }else{
                if(message.startsWith("https://firebasestorage")){
                    holder.lastMessage.setText(R.string.foto);
                }else {
                    holder.lastMessage.setText(message);
                }
            }

            if(chats.getLastMessageTime()==null) {
                holder.lastMessageTime.setText("");
            }else{
                holder.lastMessageTime.setText(Util.getTimeAgo(chats.getLastMessageTime()));
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        holder.unreadCount.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                Preference.setKeyChatId(context, chats.getId());
                Preference.setKeyChatName(context, chats.getName());
                Preference.setKeyChatPhoto(context, chats.getPhoto());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("Apakah ingin menghapus chat ini?")
                        .setCancelable(false)
                        .setPositiveButton("iya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteChat(chats.getId());
                            }
                        }).setNegativeButton("tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    private void deleteChat(String id){
        DatabaseReference chats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS).child(currentUser.getUid());
        DatabaseReference messages = FirebaseDatabase.getInstance().getReference().child(NodeNames.MESSAGES).child(currentUser.getUid());

        chats.child(id).removeValue();
        messages.child(id).removeValue();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{

        private TextView userName,unreadCount,lastMessage, lastMessageTime;
        private ImageView ivProfile;

        public ChatViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.iv_profile_chat);
            userName = itemView.findViewById(R.id.tv_name_chat);
            unreadCount = itemView.findViewById(R.id.tv_unread_count_chat);
            lastMessage = itemView.findViewById(R.id.tv_last_message_chat);
            lastMessageTime = itemView.findViewById(R.id.tv_last_message_time_chat);
        }
    }
}
