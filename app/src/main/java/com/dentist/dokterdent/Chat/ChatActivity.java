package com.dentist.dokterdent.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Message.MessageAdapter;
import com.dentist.dokterdent.Model.Konselors;
import com.dentist.dokterdent.Model.Messages;
import com.dentist.dokterdent.Utils.Constant;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.Utils.NodeNames;
import com.dentist.dokterdent.Utils.Preference;
import com.dentist.dokterdent.Utils.Util;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivSend, ivAttachment, ivProfile;
    private TextView tvUserName,tvUserStatus;
    private LinearLayout llProgress;
    private TextInputEditText etMessage;
    private String userName, photoName;
    private Toolbar toolbar;

    private BottomSheetDialog bottomSheetDialog;

    private DatabaseReference mRootRef;
    private FirebaseUser currentUser;
    private String chatUserId;

    private RecyclerView rvMessage;
    private SwipeRefreshLayout srlMessage;
    private MessageAdapter messagesAdapter;
    private ArrayList<Messages> messageList;

    //default page = 1
    private int currentPage = 1;
    //show 30 record in 1 page
    private static final int RECORD_PER_PAGE = 30;
    //permission constant
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    //permission to request
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;
    //permission to be requested
    private String[] cameraPermission;
    private String[] storagePermission;
    //uri of picked image
    private Uri imageUri=null;

    private DatabaseReference databaseReferenceMessages;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init required permission
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        llProgress = findViewById(R.id.llProgress);
        ivSend = findViewById(R.id.ivSend);
        ivAttachment = findViewById(R.id.ivAttachment);
        ivProfile = findViewById(R.id.iv_profile_action);
        tvUserName = findViewById(R.id.tv_userName_action);
        tvUserStatus = findViewById(R.id.tv_userStatus_action);
        etMessage = findViewById(R.id.etMessage);
        rvMessage = findViewById(R.id.rvMessages);
        srlMessage = findViewById(R.id.srlMessages);

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_chat_photo);

        //inisialisasi firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

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

        //check if user id is null
        if(chatUserId!=null) {
            //load data profile
            tvUserName.setText(userName);
            if (photoName != null) {
                Glide.with(this)
                        .load(photoName)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_user);
            }

            //set status online or typing
            setStatusOnline();

            //set if room is been made
            Util.checkChatRoom(ChatActivity.this,currentUser.getUid(),chatUserId);

            //load message data
            loadMessage();
            rvMessage.scrollToPosition(messageList.size() - 1);
            srlMessage.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    currentPage++;
                    loadMessage();
                }
            });
        }
    }

    private void setStatusOnline(){
        DatabaseReference databaseReferenceKonselors = mRootRef.child(NodeNames.KONSELORS).child(chatUserId);
        databaseReferenceKonselors.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Konselors konselors = dataSnapshot.getValue(Konselors.class);

                tvUserName.setText(konselors.getNama());
                if(konselors.getStatus().equals(Extras.STATUS_ONLINE))
                    tvUserStatus.setText(Extras.STATUS_ONLINE);
                else
                    tvUserStatus.setText(Extras.STATUS_OFFLINE);

                setStatusTyping(konselors.getStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setStatusTyping(String status) {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                DatabaseReference currentUserRef = mRootRef.child(NodeNames.CHATS).child(currentUser.getUid()).child(chatUserId);
                if (editable.toString().matches("")) {
                    currentUserRef.child(NodeNames.TYPING).setValue(Extras.TYPING_STOPPED);
                } else {
                    currentUserRef.child(NodeNames.TYPING).setValue(Extras.TYPING_STARTED);
                }

            }
        });


        DatabaseReference chatUserRef = mRootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUser.getUid());
        chatUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(NodeNames.TYPING).getValue() != null) {
                    String typingStatus = dataSnapshot.child(NodeNames.TYPING).getValue().toString();

                    if (typingStatus.equals(Extras.TYPING_STARTED)){
                        tvUserStatus.setText(Extras.STATUS_TYPING);
                    }else{
                        tvUserStatus.setText(status);
                    }
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
                String message = etMessage.getText().toString().trim();
                String timestamp = ""+System.currentTimeMillis();
                //validate if it's empty
                if(TextUtils.isEmpty(message)){
                    //empty? dont sent
                    Toast.makeText(ChatActivity.this,"Can't send empty message...",Toast.LENGTH_SHORT).show();
                }else{
                    //send message
                    sendMessage(message, Extras.MESSAGE_TYPE_TEXT, timestamp);
                }
                break;
            case R.id.ivAttachment:
                //pick image from gallery or gallery
                showImage();
                break;
            case R.id.camera:
                if(!checkCameraPermission()){
                    //not granted -- request
                    requestCameraPermission();
                }else{
                    pickCamera();
                }
                break;
            case R.id.gallery:
                if(!checkStoragePermission()){
                    //not granted -- request
                    requestStoragePermission();
                }else{
                    pickGallery();
                }
                break;
        }
    }

    private void showImage(){
        MaterialButton camera = bottomSheetDialog.findViewById(R.id.camera);
        MaterialButton gallery = bottomSheetDialog.findViewById(R.id.gallery);

        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);

        bottomSheetDialog.show();
    }

    private void pickGallery(){
        //intent pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "ChatImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "ChatImageDesc");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ChatActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(ChatActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ChatActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //got image from gallery
                imageUri = data.getData();
                try {
                    uploadFile(imageUri, Constant.MESSAGE_TYPE_IMAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //picked from camera
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                uploadBytes(bytes, Constant.MESSAGE_TYPE_IMAGE);
            }
        }
    }

    //upload foto dari
    private void uploadBytes(ByteArrayOutputStream bytes, String messageType) {
        //set storage file name
        String fileName = "message_images/"+""+System.currentTimeMillis();
        //set file in storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        UploadTask uploadTask = storageReference.putBytes(bytes.toByteArray());
        sendImageMessage(uploadTask,storageReference,messageType);
    }

    //upload foto dari gallery
    private void uploadFile(Uri uri, String messageType) throws IOException {
        //set storage file name
        String fileName = "message_images/"+""+System.currentTimeMillis();
        //set file in storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        //set image to bitmap
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,60,outputStream);
        //convert image to array
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = storageReference.putBytes(data);
        sendImageMessage(uploadTask, storageReference , messageType);
    }

    private void sendImageMessage(UploadTask task, StorageReference filePath, String messageType) {
        final View view = getLayoutInflater().inflate(R.layout.file_progress, null);
        final ProgressBar pbProgress = view.findViewById(R.id.pbProgress);
        final TextView tvProgress = view.findViewById(R.id.tvFileProgress);

        llProgress.addView(view);
        tvProgress.setText(getString(R.string.upload_progress, messageType, "0"));
        task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                bottomSheetDialog.dismiss();
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pbProgress.setProgress((int) progress);
                tvProgress.setText(getString(R.string.upload_progress, messageType, String.valueOf(pbProgress.getProgress())));
            }
        });

        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                llProgress.removeView(view);
                if (task.isSuccessful()) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String timestamp = ""+System.currentTimeMillis();
                            String url = uri.toString();
                            sendMessage(url,Constant.MESSAGE_TYPE_IMAGE,timestamp);
                        }
                    });
                }
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                llProgress.removeView(view);
                Toast.makeText(ChatActivity.this,getString(R.string.failed_to_send_message,e.getMessage()),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String msg, String msgType, String pushId) {
        try {
            if (!msg.equals("")) {
                Messages messages = new Messages(msg,currentUser.getUid(),System.currentTimeMillis(),msgType);

                String currentUserRef = NodeNames.MESSAGES + "/" + currentUser.getUid() + "/" + chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUser.getUid();

                HashMap messageUserMap = new HashMap();
                messageUserMap.put(currentUserRef + "/" + pushId, messages);
                messageUserMap.put(chatUserRef + "/" + pushId, messages);

                etMessage.setText("");

                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, error.getMessage()), Toast.LENGTH_SHORT).show();
                        } else {
                            String message="";
                            String image="";
                            String title = currentUser.getDisplayName();

                            if(msgType.equals(Extras.MESSAGE_TYPE_TEXT)){
                                message = msg;
                                image = " ";
                            }else if(msgType.equals(Extras.MESSAGE_TYPE_IMAGE)){
                                message = "New Image";
                                image = msg;
                            }

                            Util.sendNotificationChat(ChatActivity.this,title,message,image,currentUser.getUid(),chatUserId);
                            Util.updateChatDetails(ChatActivity.this,currentUser.getUid(),chatUserId,message,System.currentTimeMillis());
                        }
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessage() {
        //clear the list whenever load the new message
        messageList.clear();
        databaseReferenceMessages = mRootRef.child(NodeNames.MESSAGES).child(currentUser.getUid()).child(chatUserId);

        Query messageQuery = databaseReferenceMessages.limitToLast(currentPage * RECORD_PER_PAGE);

        if (childEventListener != null)
            messageQuery.removeEventListener(childEventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
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

}