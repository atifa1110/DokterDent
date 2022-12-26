package com.dentist.dokterdent.Group;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Groups;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.Model.Konselors;
import com.dentist.dokterdent.Utils.NodeNames;
import com.dentist.dokterdent.Model.Pasiens;
import com.dentist.dokterdent.Utils.Util;
import com.dentist.dokterdent.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{
    private Context context;
    private List<Groups> groupList;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public GroupAdapter(Context context, List<Groups> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @NonNull
    @NotNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_list,parent,false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupAdapter.GroupViewHolder holder, int position) {
        Groups groups = groupList.get(position);

        try{
            if(groups.getStatus().equals("selesai")){
                holder.divider.setVisibility(View.VISIBLE);
                holder.groupSelesai.setVisibility(View.VISIBLE);
            }else{
                holder.divider.setVisibility(View.GONE);
                holder.groupSelesai.setVisibility(View.GONE);
            }

            holder.groupName.setText(groups.getGroupTitle());

            String message = "";
            message = groups.getLastMessage().length() > 30 ? groups.getLastMessage().substring(0, 30) : groups.getLastMessage();

            //check message if empty and message type whether text or image
            if (message.isEmpty()) {
                holder.groupLastMessage.setText("");
            } else if (message.startsWith("https://firebasestorage")) {
                holder.groupLastMessage.setText(R.string.foto);
            } else{
                holder.groupLastMessage.setText(message);
            }

            if (groups.getLastMessageTime() == null) {
                holder.groupLastMessageTime.setText("");
            }else{
                holder.groupLastMessageTime.setText(Util.getTimeAgo(groups.getLastMessageTime()));
            }

            if(groups.getMessageFrom().isEmpty()){
                holder.groupSender.setText("");
            }else if (groups.getMessageFrom().equals(currentUser.getUid())) {
                holder.groupSender.setText("Anda : ");
            } else {
                setSenderName(groups.getMessageFrom(), holder);
            }

            Glide.with(context).load(R.drawable.ic_group).fitCenter()
                    .error(R.drawable.ic_group)
                    .into(holder.groupPhoto);

        }catch (Exception e){
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupActivity.class);
                intent.putExtra(Extras.GROUP_KEY,groups.getGroupId());
                intent.putExtra(Extras.GROUP_NAME, groups.getGroupTitle());
                intent.putExtra(Extras.GROUP_PHOTO, groups.getGroupIcon());
                context.startActivity(intent);
            }
        });
    }

//    private void loadLastMessage(Groups groups, GroupViewHolder holder){
//        //get last message from group
//        DatabaseReference message = FirebaseDatabase.getInstance().getReference().child(NodeNames.GROUPS);
//        message.child(groups.getGroupId()).child(NodeNames.MESSAGES).limitToLast(1).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                for (DataSnapshot ds :snapshot.getChildren()){
//                    Messages messages = ds.getValue(Messages.class);
//                    try {
//                        //set length message max 30 character
//                        String message = "";
//                        message = messages.getMessage().length() > 30 ? messages.getMessage().substring(0, 30) : messages.getMessage();
//
//                        //check message if empty and message type whether text or image
//                        if (message.isEmpty()) {
//                            holder.groupLastMessage.setText("");
//                        } else if (messages.getMessageType().equals("text")) {
//                            holder.groupLastMessage.setText(message);
//                        } else if (messages.getMessageType().equals("image")) {
//                            holder.groupLastMessage.setText("Foto");
//                        }
//
//                        if (messages.getMessageTime() == null) {
//                            holder.groupLastMessageTime.setText("");
//                        }else{
//                            holder.groupLastMessageTime.setText(Util.getTimeAgo(messages.getMessageTime()));
//                        }
//
//                        if(messages.getMessageFrom().isEmpty()){
//                            holder.groupSender.setText("");
//                        }else if (messages.getMessageFrom().equals(currentUser.getUid())) {
//                            holder.groupSender.setText("Anda : ");
//                        } else {
//                            setSenderName(messages.getMessageFrom(), holder);
//                        }
//
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//                Toast.makeText(context,R.string.tidak_ada,Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void setSenderName(String messageFrom,GroupViewHolder holder){
        //get sender info from uid model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(NodeNames.PASIENS);
        ref.child(messageFrom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Pasiens pasiens = snapshot.getValue(Pasiens.class);
                    holder.groupSender.setText(pasiens.getNama()+" : ");
                }else{
                    setKonselorName(messageFrom,holder);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context,R.string.tidak_ada,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setKonselorName(String messageFrom,GroupViewHolder holder) {
        //get sender info from uid model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(NodeNames.KONSELORS);
        ref.child(messageFrom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Konselors konselors = snapshot.getValue(Konselors.class);
                    holder.groupSender.setText(konselors.getNama()+" : ");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context,R.string.tidak_ada,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{

        private View divider;
        private ImageView groupPhoto;
        private TextView groupName, groupSender, groupLastMessage, groupLastMessageTime,groupSelesai;

        public GroupViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            divider = itemView.findViewById(R.id.divider);
            groupPhoto = itemView.findViewById(R.id.iv_group_chat);
            groupName = itemView.findViewById(R.id.tv_group_chat);
            groupSender = itemView.findViewById(R.id.tv_sender_message_chat);
            groupLastMessage = itemView.findViewById(R.id.tv_last_message);
            groupLastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
            groupSelesai = itemView.findViewById(R.id.tv_selesai);
        }
    }
}
