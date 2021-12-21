package com.dentist.dokterdent.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.Model.Extras;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivSend, ivAttachment, ivProfile;
    private TextView tvUserName,tvUserStatus;
    private TextInputEditText etMessage;
    private LinearLayout llProgress;

    private RecyclerView rv_message;
    private SwipeRefreshLayout srlMessage;
    private List<MessageModel> messageList;
    private MessageAdapter messageAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String groupId,groupName,groupPhoto;

    private DatabaseReference databaseReferenceGroups;
    //listen new message
    private ChildEventListener childEventListener;

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
    //permision to be requested
    private String[] cameraPermission;
    private String[] storagePermission;
    //uri of picked image
    private Uri image_uri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setActionBar();

        //init required permission
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        //inisialisasi semua view
        llProgress = findViewById(R.id.llProgress);
        ivSend = findViewById(R.id.ivSend);
        ivAttachment = findViewById(R.id.ivAttachment);
        ivProfile = findViewById(R.id.iv_profile_action);
        tvUserName = findViewById(R.id.tv_userName_action);
        tvUserStatus = findViewById(R.id.tv_userStatus_action);
        tvUserStatus.setVisibility(View.GONE);
        etMessage = findViewById(R.id.etMessage);
        srlMessage = findViewById(R.id.srlMessages);

        //inisialisasi firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //inisialisasi
        rv_message = findViewById(R.id.rvMessages);
        rv_message.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(GroupActivity.this,messageList);
        rv_message.setAdapter(messageAdapter);

        //click button
        ivSend.setOnClickListener(this);
        ivAttachment.setOnClickListener(this);

        //set database
        databaseReferenceGroups = FirebaseDatabase.getInstance().getReference().child(NodeNames.GROUPS);

        //get intent
        if(getIntent().hasExtra(Extras.GROUP_KEY)){
            groupId = getIntent().getStringExtra(Extras.GROUP_KEY);
        }if(getIntent().hasExtra(Extras.GROUP_NAME)){
            groupName = getIntent().getStringExtra(Extras.GROUP_NAME);
        }if(getIntent().hasExtra(Extras.GROUP_PHOTO)){
            groupPhoto = getIntent().getStringExtra(Extras.GROUP_PHOTO);
        }

        tvUserName.setText(groupName);
        if(groupPhoto!=null){
            Glide.with(this)
                    .load(groupPhoto)
                    .placeholder(R.drawable.ic_group)
                    .error(R.drawable.ic_group)
                    .into(ivProfile);
        }else{
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivSend:
                String message = etMessage.getText().toString().trim();
                //validate if it's empty
                if(TextUtils.isEmpty(message)){
                    //empty? dont sent
                    Toast.makeText(GroupActivity.this,"Can't send empty message...",Toast.LENGTH_SHORT).show();
                }else{
                    //send message
                    sendMessage(message,"text");
                }
                break;
            case R.id.ivAttachment:
                //pick image from gallery or gallery
                showImageImportDialog();
                break;
        }
    }

    private void showImageImportDialog(){
        //option to display
        String[] Options = {"Camera","Gallery"};

        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Foto")
                .setItems(Options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle click
                        if(which==0){
                            //clicked camera
                            if(!checkCameraPermission()){
                                //not granted -- request
                                requestCameraPermission();
                            }else{
                                pickCamera();
                            }
                        }else{
                            //clicked gallery
                            if(!checkStoragePermission()){
                                //not granted -- request
                                requestStoragePermission();
                            }else{
                                pickGallery();
                            }
                        }
                    }
                }).show();
    }

    private void pickGallery(){
        //intent pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDesc");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //got image from gallery
                image_uri = data.getData();
                try{
                    sendImageMessage(image_uri);
                }catch (IOException e){
                    Toast.makeText(GroupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //picked from camera
                try{
                    sendImageMessage(image_uri);
                }catch (IOException e){
                    Toast.makeText(GroupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(GroupActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(GroupActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(GroupActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GroupActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        //progress dialog
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Silahkan tunggu..");
        pd.setMessage("Mengirim Gambar...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        //set storage file name
        String fileName = "message_images/"+""+System.currentTimeMillis();

        //set image bitmap
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
        byte[] data = outputStream.toByteArray(); // convert image to array

        //set file in storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        //upload file
        storageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                while(!task.isSuccessful());
                String url = task.getResult().toString();

                if(task.isSuccessful()){
                    //image uri receiver , save in db
                    sendMessage(url,"image");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @org.jetbrains.annotations.NotNull Exception e) {
                Toast.makeText(GroupActivity.this,"Gagal mengirim",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message, String msgType){
        //timestamp
        String timestamp = ""+System.currentTimeMillis();

        //hashmap
        HashMap<String,Object> hashMap = new HashMap();
        hashMap.put("message",""+message);
        hashMap.put("messageFrom",""+currentUser.getUid());
        hashMap.put("messageTime",""+timestamp);
        hashMap.put("messageType",msgType);

        //add to db
        databaseReferenceGroups.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //message sent
                Toast.makeText(GroupActivity.this,"Pesan berhasil terkirim",Toast.LENGTH_SHORT).show();
                etMessage.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @org.jetbrains.annotations.NotNull Exception e) {
                //message sent failed
                Toast.makeText(GroupActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupMessages(){
        messageList.clear();
        Query messageQuery = databaseReferenceGroups.child(groupId).child("Messages").limitToLast(currentPage * RECORD_PER_PAGE);

        if (childEventListener != null)
            messageQuery.removeEventListener(childEventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot, @Nullable  String previousChildName) {
                MessageModel message = snapshot.getValue(MessageModel.class);
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

    private void setActionBar(){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_action_bar, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        search.setVisible(false);
        MenuItem add = menu.findItem(R.id.menu_add);
        add.setVisible(false);
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