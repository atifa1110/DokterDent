package com.dentist.dokterdent.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dentist.dokterdent.Notification.Api;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
                        Toast.makeText(context,context.getString(R.string.failed_get_token,task.getException()),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static void checkChatRoom(Context context, String currentUserId, String chatUserId){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRef1 = rootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId);
        DatabaseReference chatRef2 = rootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId);

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    chatRef1.child("id").setValue(currentUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(chatUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void updateChatDetails(Context context, String currentUserId, String chatUserId, String message, Long time){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRef1 = rootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId);
        DatabaseReference chatRef2 = rootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId);

        chatRef1.child("lastMessageTime").setValue(time);
        chatRef2.child("lastMessageTime").setValue(time);
    }

    public static void sendNotificationChat(Context context,String title,String message,String image,String currentuser,String userId){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
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
                    Call<ResponseBody> call = api.sendNotificationChat(deviceToken,title,message,image,currentuser);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.isSuccessful()){
                                Log.d("Response Body" , "Response Body Success: "+ response.body());
                            }else{
                                Log.d("Response Body" , "Response Body Error: "+ response.code());
                                Toast.makeText(context,context.getString(R.string.failed_to_send_notification,response.errorBody()),Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(context,context.getString(R.string.failed_to_send_notification,t.getMessage()),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

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

    public static String getDay(long time){
        SimpleDateFormat sfd = new SimpleDateFormat("dd MMM yyy HH:mm");
        String dateTime = sfd.format(new Date(time));
        String [] splitString = dateTime.split(" ");
        String day = splitString[0]+" "+splitString[1]+" "+splitString[2];
        return day;
    }

    public static String getTime(long time){
        SimpleDateFormat sfd = new SimpleDateFormat("dd MMM yyy HH:mm");
        String dateTime = sfd.format(new Date(time));
        String [] splitString = dateTime.split(" ");
        String waktu = splitString[3];
        return waktu ;
    }

    public static boolean isAppInForeground(Context context) {
        List<ActivityManager.RunningTaskInfo> task =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningTasks(1);
        if (task.isEmpty()) {
            // app is in background
            return false;
        }
        return task.get(0).topActivity.getPackageName().equalsIgnoreCase(context.getPackageName());
    }
}
