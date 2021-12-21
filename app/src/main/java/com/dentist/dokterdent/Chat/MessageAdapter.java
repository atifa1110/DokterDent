package com.dentist.dokterdent.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Constant;
import com.dentist.dokterdent.Model.Extras;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<MessageModel> messageList;
    private FirebaseAuth firebaseAuth;

    public MessageAdapter(Context context, List<MessageModel> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @NotNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.MessageViewHolder holder, int position) {
        MessageModel messageModel = messageList.get(position);

        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        String fromUserId = messageModel.getMessageFrom();

        Log.d(currentUserId, "currentID");
        Log.d(fromUserId, "fromUser");

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateTime = sfd.format(new Date(Long.parseLong(messageModel.getMessageTime())));
        String[] splitString = dateTime.split(" ");
        String messageTime = splitString[1];

        //check
        if(fromUserId.equals(currentUserId)){
            if(messageModel.getMessageType().equals(Constant.MESSAGE_TYPE_TEXT)){
                holder.card_llSent.setVisibility(View.VISIBLE);
                holder.tvSentMessageTime.setVisibility(View.VISIBLE);
                holder.ivSent.setVisibility(View.GONE);
                holder.tvImageSentTime.setVisibility(View.GONE);
            }else{
                holder.ivSent.setVisibility(View.VISIBLE);
                holder.tvImageSentTime.setVisibility(View.VISIBLE);
                holder.card_llSent.setVisibility(View.GONE);
                holder.tvSentMessageTime.setVisibility(View.GONE);
            }
            holder.card_llReceived.setVisibility(View.GONE);
            holder.tvReceivedMessageTime.setVisibility(View.GONE);
            holder.ivReceived.setVisibility(View.GONE);
            holder.tvImageReceivedTime.setVisibility(View.GONE);

            holder.tvSentMessage.setText(messageModel.getMessage());
            holder.tvSentMessageTime.setText(messageTime);
            holder.tvImageSentTime.setText(messageTime);

            try{
                Glide.with(context).load(messageModel.getMessage())
                        .placeholder(R.drawable.ic_add_photo)
                        .fitCenter()
                        .into(holder.ivSent);
            }catch (Exception e){
                holder.ivSent.setImageResource(R.drawable.ic_add_photo);
            }

        }else {
            if (messageModel.getMessageType().equals(Constant.MESSAGE_TYPE_TEXT)) {
                holder.card_llReceived.setVisibility(View.VISIBLE);
                holder.tvReceivedMessageTime.setVisibility(View.VISIBLE);
                holder.ivReceived.setVisibility(View.GONE);
                holder.tvImageReceivedTime.setVisibility(View.GONE);
                setKonselorName(messageModel, holder);
            } else {
                holder.ivReceived.setVisibility(View.VISIBLE);
                holder.tvImageReceivedTime.setVisibility(View.VISIBLE);
                holder.card_llReceived.setVisibility(View.GONE);
                holder.tvReceivedMessageTime.setVisibility(View.GONE);
            }
            holder.card_llSent.setVisibility(View.GONE);
            holder.tvSentMessageTime.setVisibility(View.GONE);
            holder.ivSent.setVisibility(View.GONE);
            holder.tvImageSentTime.setVisibility(View.GONE);

            holder.tvReceivedMessage.setText(messageModel.getMessage());
            holder.tvReceivedMessageTime.setText(messageTime);
            holder.tvImageReceivedTime.setText(messageTime);

            try {
                Glide.with(context).load(messageModel.getMessage())
                        .placeholder(R.drawable.ic_add_photo)
                        .fitCenter()
                        .into(holder.ivReceived);
            } catch (Exception e) {
                holder.ivReceived.setImageResource(R.drawable.ic_add_photo);
            }
        }

        //tag is a mechanism to make your views remember something,
        //that could be an object an integer a string or anything you like.
        holder.clMessage.setTag(R.id.TAG_MESSAGE,messageModel.getMessage());
        holder.clMessage.setTag(R.id.TAG_MESSAGE_ID,messageModel.getMessageId());
        holder.clMessage.setTag(R.id.TAG_MESSAGE_TYPE,messageModel.getMessageType());

        holder.clMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageType = v.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri uri = Uri.parse(v.getTag(R.id.TAG_MESSAGE).toString());
                if (messageType.equals(Constant.MESSAGE_TYPE_IMAGE)){
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"image/jpg");
                    context.startActivity(intent);
                }
            }
        });
    }

    private void setKonselorName(MessageModel messageModel,MessageViewHolder holder){
        //get sender info from uid model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(NodeNames.KONSELORS);
        ref.child(messageModel.getMessageFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nama = snapshot.child(NodeNames.NAME).getValue().toString();
                    holder.tvReceiveName.setText(nama);
                }else{
                    setUserName(messageModel,holder);
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    private void setUserName(MessageModel messageModel,MessageViewHolder holder){
        //get sender info from uid model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        ref.child(messageModel.getMessageFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                String nama = snapshot.child(NodeNames.NAME).getValue().toString();
                holder.tvReceiveName.setText(nama);
                holder.tvReceiveName.setTextColor(context.getResources().getColor(R.color.blue));
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private CardView card_llSent , card_llReceived;
        private TextView tvSentMessage, tvSentMessageTime, tvReceivedMessage, tvReceivedMessageTime,tvReceiveName;
        private ImageView ivSent, ivReceived;
        private TextView tvImageSentTime, tvImageReceivedTime;
        private ConstraintLayout clMessage;

        public MessageViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);

            clMessage = itemView.findViewById(R.id.clMessage);

            card_llSent = itemView.findViewById(R.id.card_llSent);
            tvSentMessage = itemView.findViewById(R.id.tvSentMessage);
            tvSentMessageTime = itemView.findViewById(R.id.tvSentMessageTime);

            card_llReceived = itemView.findViewById(R.id.card_llReceived);
            tvReceiveName = itemView.findViewById(R.id.tvReceivedName);
            tvReceivedMessage = itemView.findViewById(R.id.tvReceivedMessage);
            tvReceivedMessageTime = itemView.findViewById(R.id.tvReceivedMessageTime);

            ivSent = itemView.findViewById(R.id.ivSent);
            tvImageSentTime = itemView.findViewById(R.id.tvSentImageTime);

            ivReceived =itemView.findViewById(R.id.ivReceived);
            tvImageReceivedTime = itemView.findViewById(R.id.tvReceivedImageTime);
        }
    }
}
