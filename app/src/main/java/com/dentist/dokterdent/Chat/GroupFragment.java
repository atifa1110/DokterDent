package com.dentist.dokterdent.Chat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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


public class GroupFragment extends Fragment {

    private RecyclerView rvChat;
    private List<GroupModel> groupList;
    private GroupAdapter groupAdapter;
    private View emptyChat;
    private CircularProgressIndicator progress;

    private DatabaseReference databaseReferenceGroups;
    private FirebaseUser currentUser;

    private ChildEventListener childEventListener;
    private Query query;

    private List<String> userIds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //inisialisasi semua view
        rvChat = view.findViewById(R.id.rv_chats_groups);
        emptyChat = view.findViewById(R.id.ll_empty_chat);
        progress = view.findViewById(R.id.progressBar);

        ////set array list
        userIds = new ArrayList<>();
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
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                loadGroupChat(snapshot, true, snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                loadGroupChat(snapshot, false, snapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        //ada query dengan child listener
        query.addChildEventListener(childEventListener);
        emptyChat.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    private void deleteChat(String chatId) {
        databaseReferenceGroups.child(currentUser.getUid()).child(chatId).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {

            }
        });
    }

    private void loadGroupChat(DataSnapshot snapshot, boolean isNew, String groupId) {
        emptyChat.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        if (snapshot.child("Participants").child(currentUser.getUid()).exists()) {
            progress.setVisibility(View.GONE);
            String groupTitle = snapshot.child("groupTitle").getValue().toString();
            String groupIcon = snapshot.child("groupIcon").getValue().toString();
            String timestamp = snapshot.child("timestamp").getValue().toString();

            GroupModel groupModel = new GroupModel(groupId, groupTitle, groupIcon, timestamp);
            if (isNew) {
                groupList.add(groupModel);
                userIds.add(groupId);
            } else {
                int indexofClickedUser = userIds.indexOf(groupId);
                groupList.set(indexofClickedUser, groupModel);
            }
            groupAdapter.notifyDataSetChanged();
        }
    }

    private void searchGroupChat(final String query) {
        emptyChat.setVisibility(View.GONE);
        databaseReferenceGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupId = ds.getKey();
                    if (ds.child("Participants").child(currentUser.getUid()).exists()) {
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            String groupTitle = ds.child("groupTitle").getValue().toString();
                            String groupIcon = ds.child("groupIcon").getValue().toString();
                            String timestamp = ds.child("timestamp").getValue().toString();

                            GroupModel groupModel = new GroupModel(groupId, groupTitle, groupIcon, timestamp);
                            groupList.add(groupModel);
                        }
                    }
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_action_bar, menu);
        MenuItem info = menu.findItem(R.id.menu_info);
        info.setVisible(false);
        MenuItem add = menu.findItem(R.id.menu_add);
        add.setVisible(false);
        MenuItem search = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when submit text
                if (!TextUtils.isEmpty(query.trim())) {
                    searchGroupChat(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when typing text
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchGroupChat(newText);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }

}
