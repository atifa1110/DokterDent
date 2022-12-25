package com.dentist.dokterdent.Group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Message.MessageAdapter;
import com.dentist.dokterdent.Model.Messages;
import com.dentist.dokterdent.Utils.NodeNames;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private ImageView ivSend, ivAttachment, ivProfile;
    private TextView tvUserName,tvUserStatus;
    private TextInputEditText etMessage;
    private Toolbar toolbar;
    private LinearLayout llProgress,llSendChat;
    private MaterialCardView llSnackbar;
    private RecyclerView rv_message;
    private SwipeRefreshLayout srlMessage;
    private List<Messages> messageList;
    private MessageAdapter messageAdapter;

    private FirebaseUser currentUser;
    private String groupId,groupName,groupPhoto;

    private DatabaseReference databaseReferenceGroups;
    private ChildEventListener childEventListener;

    //default page = 1
    private int currentPage = 1;
    //show 30 record in 1 page
    private static final int RECORD_PER_PAGE = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //inisialisasi semua view
        llSnackbar = findViewById(R.id.llSnackbar);
        llSendChat = findViewById(R.id.llSendChat);
        llSendChat.setVisibility(View.GONE);
        ivSend = findViewById(R.id.ivSend);
        ivAttachment = findViewById(R.id.ivAttachment);
        ivProfile = findViewById(R.id.iv_profile_action);
        tvUserName = findViewById(R.id.tv_userName_action);
        tvUserStatus = findViewById(R.id.tv_userStatus_action);
        tvUserStatus.setVisibility(View.GONE);
        etMessage = findViewById(R.id.etMessage);
        etMessage.setEnabled(false);
        srlMessage = findViewById(R.id.srlMessages);

        //inisialisasi firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //inisialisasi
        messageList = new ArrayList<>();
        rv_message = findViewById(R.id.rvMessages);
        rv_message.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(GroupActivity.this,messageList);
        rv_message.setAdapter(messageAdapter);

        //set database
        databaseReferenceGroups = FirebaseDatabase.getInstance().getReference().child(NodeNames.GROUPS);

        //get intent
        groupId = getIntent().getStringExtra(Extras.GROUP_KEY);
        groupName = getIntent().getStringExtra(Extras.GROUP_NAME);
        groupPhoto = getIntent().getStringExtra(Extras.GROUP_PHOTO);

        //cek id apakah ada atau tidak
        if(groupId!=null) {
            tvUserName.setText(groupName);
            if (groupPhoto != null) {
                Glide.with(this)
                        .load(groupPhoto)
                        .placeholder(R.drawable.ic_group)
                        .error(R.drawable.ic_group)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_group);
            }

            loadGroupMessages();
            rv_message.scrollToPosition(messageList.size() - 1);
            srlMessage.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    currentPage++;
                    loadGroupMessages();
                }
            });

            session();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        session();
    }

    private void session(){
        databaseReferenceGroups.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Groups group = snapshot.getValue(Groups.class);

                    if(group.getStatus().equals("selesai")){
                        llSnackbar.setVisibility(View.VISIBLE);
                    }else{
                        llSnackbar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupMessages(){
        messageList.clear();
        Query messageQuery = databaseReferenceGroups.child(groupId).child(NodeNames.MESSAGES).limitToLast(currentPage * RECORD_PER_PAGE);

        if (childEventListener != null)
            messageQuery.removeEventListener(childEventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable  String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                if (!messageList.contains(message)) {
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    rv_message.scrollToPosition(messageList.size() - 1);
                    srlMessage.setRefreshing(false);
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                loadGroupMessages();
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                srlMessage.setRefreshing(false);
            }
        };
        messageQuery.addChildEventListener(childEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_action_bar, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        search.setVisible(false);
        return true;
    }

    // this event will enable the back , function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_info:
                Intent intent = new Intent(GroupActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}