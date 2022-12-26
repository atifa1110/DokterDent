package com.dentist.dokterdent.Group;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dentist.dokterdent.Model.Groups;
import com.dentist.dokterdent.Model.Messages;
import com.dentist.dokterdent.Utils.NodeNames;
import com.dentist.dokterdent.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class GroupFragment extends Fragment {

    private RecyclerView rvChat;
    private List<Groups> groupList;
    private GroupAdapter groupAdapter;
    private View emptyChat;
    private ShimmerFrameLayout shimmerFrameLayoutGroup;

    private DatabaseReference databaseReferenceGroups;
    private FirebaseUser currentUser;
    private Query query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //inisialisasi semua view
        rvChat = view.findViewById(R.id.rv_chats_groups);
        emptyChat = view.findViewById(R.id.ll_empty_chat);
        shimmerFrameLayoutGroup = view.findViewById(R.id.shimmer_group);
        shimmerFrameLayoutGroup.startShimmer();

        //set array list
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(getContext(), groupList);

        //set layout
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);

        //set group adapter
        rvChat.setLayoutManager(linearLayout);
        rvChat.setAdapter(groupAdapter);

        //inisialisasi database
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceGroups = FirebaseDatabase.getInstance().getReference().child(NodeNames.GROUPS);

        //set query dengan diurutkan dengan waktu kirim
        query = databaseReferenceGroups.orderByChild(NodeNames.TIME_STAMP);

        emptyChat.setVisibility(View.VISIBLE);
        loadGroupChat();
    }

    private void loadGroupChat(){
        emptyChat.setVisibility(View.GONE);
        shimmerFrameLayoutGroup.startShimmer();
        //set query dengan diurutkan dengan waktu kirim
        query = databaseReferenceGroups.orderByChild(NodeNames.TIME_STAMP);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                shimmerFrameLayoutGroup.stopShimmer();
                shimmerFrameLayoutGroup.setVisibility(View.GONE);
                if(snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (ds.child(NodeNames.PARTICIPANTS).child(currentUser.getUid()).child(NodeNames.ID).exists()) {
                            rvChat.setVisibility(View.VISIBLE);
                            emptyChat.setVisibility(View.GONE);
                            try {
                                Groups groups = ds.getValue(Groups.class);
                                loadLastMessage(groups);
                                groupList.add(groups);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        if(groupList.isEmpty()){
                            rvChat.setVisibility(View.GONE);
                            emptyChat.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    rvChat.setVisibility(View.GONE);
                    emptyChat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),R.string.tidak_ada,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLastMessage(Groups groups){
        //get last message from group
        databaseReferenceGroups.child(groups.getGroupId()).child(NodeNames.MESSAGES).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            Messages messages = ds.getValue(Messages.class);
                            groups.setLastMessage(messages.getMessage());
                            groups.setLastMessageTime(messages.getMessageTime());
                            groups.setMessageFrom(messages.getMessageFrom());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    groups.setLastMessage("");
                    groups.setLastMessageTime(null);
                    groups.setMessageFrom("");
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(),R.string.tidak_ada,Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
