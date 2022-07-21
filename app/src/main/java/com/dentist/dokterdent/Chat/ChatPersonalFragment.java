package com.dentist.dokterdent.Chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dentist.dokterdent.Model.Dokters;
import com.dentist.dokterdent.Model.Konselors;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class ChatPersonalFragment extends Fragment {

    private FloatingActionButton btn_add;
    private RecyclerView rvChat;
    private List<Chats> chatList;
    private ChatAdapter chatAdapter;
    private View emptyChat;
    private ProgressDialog progress;

    private DatabaseReference databaseReferenceKonselors, databaseReferenceChats;
    private FirebaseUser currentUser;

    private ChildEventListener childEventListener;
    private Query query;

    private List<String> userIds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_personal, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //inisialisasi semua view
        rvChat = view.findViewById(R.id.rv_chats_personel);
        emptyChat = view.findViewById(R.id.ll_empty_chat);
        btn_add = view.findViewById(R.id.floatingActionButton);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), KonselorActivity.class);
                startActivity(intent);
            }
        });

        progress = new ProgressDialog(getActivity());
        progress.setMessage("Silahkan Tunggu..");
        progress.show();

        ////set array list
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(),chatList);

        //set layout
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);

        //set group adapter
        rvChat.setLayoutManager(linearLayout);
        rvChat.setAdapter(chatAdapter);

        //inisialisasi database
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceKonselors = FirebaseDatabase.getInstance().getReference().child(NodeNames.KONSELORS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS).child(currentUser.getUid());

        //set query dengan diurutkan dengan waktu kirim
        query = databaseReferenceChats.orderByChild(NodeNames.LAST_MESSAGE_TIME);

        loadChats();
    }

    private void loadChats(){
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    chatList.clear();
                    progress.dismiss();
                    emptyChat.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()){
                        String id = ds.getKey();
                        final String lastMessage , lastMessageTime , unreadCount;

                        if(ds.child(NodeNames.LAST_MESSAGE).getValue()!=null)
                            lastMessage = ds.child(NodeNames.LAST_MESSAGE).getValue().toString();
                        else
                            lastMessage = "";

                        if(ds.child(NodeNames.LAST_MESSAGE_TIME).getValue()!=null)
                            lastMessageTime = ds.child(NodeNames.LAST_MESSAGE_TIME).getValue().toString();
                        else
                            lastMessageTime="";

                        unreadCount=snapshot.child(NodeNames.UNREAD_COUNT).getValue()==null?
                                "0":snapshot.child(NodeNames.UNREAD_COUNT).getValue().toString();

                        databaseReferenceKonselors.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Konselors konselors = snapshot.getValue(Konselors.class);
                                Chats chats = new Chats(id,konselors.getNama(),konselors.getPhoto(),unreadCount,lastMessage,lastMessageTime);
                                chatList.add(chats);
                                chatAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }else{
                    chatList.clear();
                    progress.dismiss();
                    emptyChat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
