package com.dentist.dokterdent.Chat;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Preference;
import com.dentist.dokterdent.Model.Util;
import com.dentist.dokterdent.R;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private List<Chats> chatlist;

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

        holder.userName.setText(chats.getUserName());

        Glide.with(context).load(chats.getPhotoName())
                .placeholder(R.drawable.ic_user).fitCenter()
                .error(R.drawable.ic_user).into(holder.iv_profile);

        //show only 30 character only
        String lastMessage = chats.getLastMessage();
        lastMessage = lastMessage.length()>30?lastMessage.substring(0,30):lastMessage;
        holder.lastMessage.setText(lastMessage);

        //check last message Time
        String lastMessageTime = chats.getLastMessageTime();
        Log.d(lastMessageTime,"lastmessagetime");
        if(lastMessageTime==null){
            lastMessageTime="";
        }if(!TextUtils.isEmpty(lastMessageTime)){
            holder.lastMessageTime.setText(Util.getTimeAgo(Long.parseLong(lastMessageTime)));
        }

        //check if unread not 0
        //set visible and set text
//        if(!chats.getUnreadCount().equals("0")){
//            holder.unreadCount.setVisibility(View.VISIBLE);
//            holder.unreadCount.setText(chats.getUnreadCount());
//        }else{
            holder.unreadCount.setVisibility(View.GONE);
        //}

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                Preference.setKeyChatId(context, chats.getUserId());
                Preference.setKeyChatName(context, chats.getUserName());
                Preference.setKeyChatPhoto(context, chats.getPhotoName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder{

        private TextView userName,unreadCount,lastMessage, lastMessageTime;
        private ImageView iv_profile;

        public ChatViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            iv_profile = itemView.findViewById(R.id.iv_profile_chat);
            userName = itemView.findViewById(R.id.tv_name_chat);
            unreadCount = itemView.findViewById(R.id.tv_unread_count_chat);
            lastMessage = itemView.findViewById(R.id.tv_last_message_chat);
            lastMessageTime = itemView.findViewById(R.id.tv_last_message_time_chat);
        }
    }
}
