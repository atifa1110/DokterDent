package com.dentist.dokterdent.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.Model.UserModel;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity {

    private TextView tv_group_name,tv_partisipan;
    private Button btn_keluar;
    private RecyclerView rv_participant;
    private List<UserModel> partisipantList;
    private ParticipantAdapter participantAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReferenceGroup;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        setActionBar();

        //inisialisasi view
        tv_group_name = findViewById(R.id.tv_group_name);
        tv_partisipan = findViewById(R.id.tv_partisipant_group);
        rv_participant = findViewById(R.id.rv_participant);
        btn_keluar = findViewById(R.id.btn_leave);

        //set recycler view
        rv_participant.setLayoutManager(new LinearLayoutManager(this));
        partisipantList = new ArrayList<>();
        participantAdapter = new ParticipantAdapter(GroupInfoActivity.this,partisipantList);
        rv_participant.setAdapter(participantAdapter);

        //set database
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReferenceGroup = FirebaseDatabase.getInstance().getReference().child(NodeNames.GROUPS);

        //get intent
        groupId = getIntent().getStringExtra("groupId");

        loadGroup(groupId);

        tv_group_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_keluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GroupInfoActivity.this);
                alertDialogBuilder.setMessage("Apakah Anda yakin ingin keluar?")
                        .setCancelable(false)
                        .setPositiveButton("iya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteChat(groupId);
                            }
                        }).setNegativeButton("tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void deleteChat(String groupId){
        databaseReferenceGroup.child(groupId).child("Participants").child(currentUser.getUid()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {

                finish();
            }
        });
    }

    private void loadGroup(String groupId){
        databaseReferenceGroup.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String groupTitle = snapshot.child("groupTitle").getValue().toString();
                tv_group_name.setText(groupTitle);

                databaseReferenceGroup.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        partisipantList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            String userId = ds.getKey();
                            String timestamp = ds.child(NodeNames.TIME_STAMP).getValue().toString();
                            String role = ds.child(NodeNames.ROLE).getValue().toString();
                            UserModel user = new UserModel(userId,timestamp,role);
                            partisipantList.add(user);
                        }
                        participantAdapter.notifyDataSetChanged();
                        //set tv partisipan sebanyak partisipan list
                        tv_partisipan.setText(participantAdapter.getItemCount()+" members");
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void setActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Group Info");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions());
        }
    }

    // this event will enable the back , function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}