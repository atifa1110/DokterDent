package com.dentist.dokterdent.Model;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dentist.dokterdent.Notification.Api;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Util {

    public static void updateDeviceToken(Context context, String token) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(currentUser.getUid());

            databaseReference.child(NodeNames.DEVICE_TOKEN).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        //Toast.makeText(context,"Gagal menyimpan Token Device",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static void updateChatDetails(Context context, String currentUserId, String chatUserId, String lastMessage, String lastMessageTime){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        //asking the chat user whenever we sent the message unread count will
        //increament
        DatabaseReference chatRef = rootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String currentCount = "0" ;
                //jika sudah ada di database
                if(snapshot.child(NodeNames.UNREAD_COUNT).getValue()!=null){
                    currentCount = snapshot.child(NodeNames.UNREAD_COUNT).getValue().toString();
                }

                Map chatUser = new HashMap();
                chatUser.put(NodeNames.UNREAD_COUNT,Integer.parseInt(currentCount)+1);
                chatUser.put(NodeNames.LAST_MESSAGE,lastMessage);
                chatUser.put(NodeNames.LAST_MESSAGE_TIME,lastMessageTime);

                Map chatCurrent = new HashMap();
                chatCurrent.put(NodeNames.UNREAD_COUNT,0);
                chatCurrent.put(NodeNames.LAST_MESSAGE,lastMessage);
                chatCurrent.put(NodeNames.LAST_MESSAGE_TIME,lastMessageTime);

                HashMap <String,Object> messageUserMap = new HashMap();
                messageUserMap.put(NodeNames.CHATS + "/" + currentUserId + "/" + chatUserId, chatCurrent);
                messageUserMap.put(NodeNames.CHATS + "/" + chatUserId + "/" + currentUserId, chatUser);

                rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                        if(error!=null){
                            //Toast.makeText(context,context.getString(R.string.something_wrong,error.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public static void sendNotificationChat(Context context,String title,String message,String image,String userId){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if(snapshot.child(NodeNames.DEVICE_TOKEN).getValue()!=null){
                    String deviceToken = snapshot.child(NodeNames.DEVICE_TOKEN).getValue().toString();

                    String Url = "https://halo-dent.web.app/api/";

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Url)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    Api api = retrofit.create(Api.class);
                    Call<ResponseBody> call = api.sendNotificationChat(deviceToken,title,message,image);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            try{
//                                Toast.makeText(context.getApplicationContext(), response.body().string(),Toast.LENGTH_SHORT).show();
//                            }catch (IOException e){
//                                e.printStackTrace();
//                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //Toast.makeText(context.getApplicationContext(), t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    public static String getTimeAgo(long time) {
        //set date format
        SimpleDateFormat sfd = new SimpleDateFormat("EEE dd MMM yyyy HH:mm");
        String dateTime = sfd.format(time);

        //split date format
        String [] splitString = dateTime.split(" ");
        String day = splitString[0];
        String date = splitString[1]+" "+splitString[2]+" "+splitString[3];
        String lastMessageTime = splitString[4];

        long now = System.currentTimeMillis();

        // Get msec from each, and subtract.
        final long diff = now - time;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours   = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        String text = null;
        if (minutes < 1) {
            return text = "Sekarang";
        } else if (hours < 24) {
            return text = lastMessageTime;
        } else if (hours < 48) {
            return text = "Kemarin";
        }else if(days < 7) {
            return text = day;
        }else{
            return text = date;
        }
    }
}
