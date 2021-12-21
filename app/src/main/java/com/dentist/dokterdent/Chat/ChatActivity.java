package com.dentist.dokterdent.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Extras;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.Model.Preference;
import com.dentist.dokterdent.Model.Util;
import com.dentist.dokterdent.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivSend, ivAttachment, ivProfile;
    private TextView tvUserName,tvUserStatus;
    private TextInputEditText etMessage;
    private LinearLayout llProgress;
    private String userName, photoName;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String currentUserId, chatUserId;

    private RecyclerView rvMessage;
    private SwipeRefreshLayout srlMessage;
    private MessageAdapter messagesAdapter;
    private ArrayList<MessageModel> messageList;

    //default page = 1
    private int currentPage = 1;
    //show 30 record in 1 page
    private static final int RECORD_PER_PAGE = 30;

    private DatabaseReference databaseReferenceMessages;
    //listen new message
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setActionBar();

        llProgress = findViewById(R.id.llProgress);
        ivSend = findViewById(R.id.ivSend);
        ivAttachment = findViewById(R.id.ivAttachment);
        ivProfile = findViewById(R.id.iv_profile_action);
        tvUserName = findViewById(R.id.tv_userName_action);
        tvUserStatus = findViewById(R.id.tv_userStatus_action);
        etMessage = findViewById(R.id.etMessage);
        rvMessage = findViewById(R.id.rvMessages);
        srlMessage = findViewById(R.id.srlMessages);

        //inisialisasi firebase
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        //inisialisasi array list dan adapter
        messageList = new ArrayList<>();
        messagesAdapter = new MessageAdapter(this, messageList);

        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        rvMessage.setAdapter(messagesAdapter);

        ivSend.setOnClickListener(this);
        ivAttachment.setOnClickListener(this);

        chatUserId = Preference.getKeyChatId(getApplicationContext());
        userName = Preference.getKeyChatName(getApplicationContext());
        photoName = Preference.getKeyChatPhoto(getApplicationContext());

        tvUserName.setText(userName);
        if (photoName!=null) {
            Glide.with(this)
                    .load(photoName)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(ivProfile);
        }else{
            ivProfile.setImageResource(R.drawable.ic_user);
        }

        loadMessage();
        rvMessage.scrollToPosition(messageList.size() - 1);
        srlMessage.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                loadMessage();
            }
        });

        mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId).child(NodeNames.UNREAD_COUNT).setValue(0);

        setStatusOnline();
        setStatusTyping();

    }

    private void setStatusOnline(){
        DatabaseReference databaseReferenceUsers = mRootRef.child(NodeNames.KONSELORS).child(chatUserId);
        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String status="";
                if(dataSnapshot.child(NodeNames.ONLINE).getValue()!=null)
                    status = dataSnapshot.child(NodeNames.ONLINE).getValue().toString();

                if(status.equals("Online"))
                    tvUserStatus.setText(Extras.STATUS_ONLINE);
                else
                    tvUserStatus.setText(Extras.STATUS_OFFLINE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setStatusTyping() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                DatabaseReference currentUserRef = mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId);
                if (editable.toString().matches("")) {
                    currentUserRef.child(NodeNames.TYPING).setValue(Extras.TYPING_STOPPED);
                } else {
                    currentUserRef.child(NodeNames.TYPING).setValue(Extras.TYPING_STARTED);
                }

            }
        });


        DatabaseReference chatUserRef = mRootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId);
        chatUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(NodeNames.TYPING).getValue() != null) {
                    String typingStatus = dataSnapshot.child(NodeNames.TYPING).getValue().toString();

                    if (typingStatus.equals(Extras.TYPING_STARTED))
                        tvUserStatus.setText(Extras.STATUS_TYPING);
                    else
                        tvUserStatus.setText(Extras.STATUS_ONLINE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSend:
                DatabaseReference userMessagePush = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
                String pushId = userMessagePush.getKey();
                sendMessage(etMessage.getText().toString().trim(), Extras.MESSAGE_TYPE_TEXT, pushId);
                break;
        }
    }

    private void sendMessage(String msg, String msgType, String pushId) {
        try {
            if (!msg.equals("")) {
                HashMap messageMap = new HashMap();
                messageMap.put(NodeNames.MESSAGE_ID, pushId);
                messageMap.put(NodeNames.MESSAGE, msg);
                messageMap.put(NodeNames.MESSAGE_TYPE, msgType);
                messageMap.put(NodeNames.MESSAGE_FROM, currentUserId);
                messageMap.put(NodeNames.MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserRef = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId;

                HashMap messageUserMap = new HashMap();
                messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                etMessage.setText("");

                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, error.getMessage()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, R.string.message_sent_successfully, Toast.LENGTH_SHORT).show();

                            String title="";

                            if(msgType.equals(Extras.MESSAGE_TYPE_TEXT)){
                                title = "New Message";
                            }else if(msgType.equals(Extras.MESSAGE_TYPE_IMAGE)){
                                title = "New Image";
                            }
                            Util.sendNotification(ChatActivity.this,title,msg,chatUserId);

                            String lastMessage = !title.equals("New Message")? title:msg;
                            Util.updateChatDetails(ChatActivity.this,currentUserId,chatUserId,lastMessage);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, ex.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessage() {
        //clear the list whenever load the new message
        messageList.clear();
        databaseReferenceMessages = mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);

        Query messageQuery = databaseReferenceMessages.limitToLast(currentPage * RECORD_PER_PAGE);

        if (childEventListener != null)
            messageQuery.removeEventListener(childEventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String msg = snapshot.child("message").getValue().toString();
                String messageFrom = snapshot.child("messageFrom").getValue().toString();
                String messageId = snapshot.child("messageId").getValue().toString();
                String messageTime= snapshot.child("messageTime").getValue().toString();
                String messageType = snapshot.child("messageType").getValue().toString();

                MessageModel message = new MessageModel(msg,messageFrom,messageId,messageTime,messageType);

                if (!messageList.contains(message)) {
                    messageList.add(message);
                    messagesAdapter.notifyDataSetChanged();
                    rvMessage.scrollToPosition(messageList.size() - 1);
                    srlMessage.setRefreshing(false);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                loadMessage();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                srlMessage.setRefreshing(false);
            }
        };
        messageQuery.addChildEventListener(childEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar, null);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);
            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        }
    }


    @Override
    public void onBackPressed() {
        mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId).child(NodeNames.UNREAD_COUNT).setValue(0);
        super.onBackPressed();
    }
}