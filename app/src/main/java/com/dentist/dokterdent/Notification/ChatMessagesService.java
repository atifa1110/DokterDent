package com.dentist.dokterdent.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dentist.dokterdent.Chat.ChatActivity;
import com.dentist.dokterdent.Group.GroupActivity;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.Utils.Preference;
import com.dentist.dokterdent.Utils.Util;
import com.dentist.dokterdent.R;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.Group;
import java.util.Map;

public class ChatMessagesService extends FirebaseMessagingService {

    private static final String TAG = "ChatMessageService";
    private static final String CHANNEL_ID = "channel";
    Bitmap image_bitmap = null;

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
        Util.updateDeviceToken(this,s);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(!remoteMessage.getData().isEmpty()){
            Map<String,String> data = remoteMessage.getData();
            Log.d(TAG,data.get("title"));
            Log.d(TAG,data.get("body"));
            Log.d(TAG,data.get("image"));
            Log.d(TAG,data.get("chatid"));
            Log.d(TAG,data.get("to"));

            //check if the app is running in foreground or background
            //cause we want to get notification when the app is closed
            if(!Util.isAppInForeground(getApplicationContext())){
                if (data.get("image").startsWith("https://firebasestorage")) {
                    image_bitmap = getBitmapFromURL(data.get("image"));
                    showNotification(data.get("title"), data.get("body"), image_bitmap, data.get("chatid"), data.get("to"));
                } else {
                    //create and display notification
                    showNotification(data.get("title"), data.get("body"), null, data.get("chatid"), data.get("to"));
                }
            }
        }
    }

    private Bitmap getBitmapFromURL(String img_url) {
        try {
            URL url = new URL(img_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showNotification(String title, String message,Bitmap image,String id,String to){
        //create notification channel for API 26+
        createNotificationChannel();

        Intent intentChat = new Intent(getApplicationContext(), ChatActivity.class);
        Preference.setKeyChatId(getApplicationContext(), id);
        intentChat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentChat = PendingIntent.getActivity(getApplicationContext(), 0, intentChat, 0);

        Intent intentGroup = new Intent(getApplicationContext(), GroupActivity.class);
        intentGroup.putExtra(Extras.GROUP_KEY,id);
        intentGroup.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentGroup = PendingIntent.getActivity(getApplicationContext(), 1, intentGroup, 0);

        Uri defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this
                ,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_tooth)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultNotificationSound)
                .setLights(Color.GREEN,500,200)
                .setColor(getResources().getColor(R.color.design_default_color_primary))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if(image!=null){
            builder.setLargeIcon(image);
        }

        if(to.equals("Chat")){
            builder.setContentIntent(pendingIntentChat);
        }else{
            builder.setContentIntent(pendingIntentGroup);
        }

        //notification ID is unique for each notification you create
        notificationManager.notify(2,builder.build());
    }

    private void createNotificationChannel(){
        //create notification channel only on API level 26+
        //Notification channel is a new class and not a support library
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            String name = "My Chat Channel ";
            String description = "My Chat Channel Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,importance);
            channel.setDescription(description);
            //Register the channel with the system
            //You cannot change importance or other notification behaviours after this
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
