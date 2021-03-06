package com.dentist.dokterdent.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import com.dentist.dokterdent.Model.Util;
import com.dentist.dokterdent.R;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ChatMessagesService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "channel";
    String img_url = null;
    Bitmap image_bitmap = null;

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
        Util.updateDeviceToken(this,s);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();
        String image = remoteMessage.getData().get("image");

        if(remoteMessage.getNotification()!=null){
            if(message.equals("New Image")){
                image_bitmap = getBitmapFromURL(image);
                showNotification(title,message,image_bitmap);
            }else{
                //create and display notification
                showNotification(title,message,null);
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

    private void showNotification(String title, String message,Bitmap image){
        //create notification channel for API 26+
        createNotificationChannel();

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
                .setVibrate(new long[]{0,250,250,250})
                .setColor(getResources().getColor(R.color.design_default_color_primary))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if(image!=null){
            builder.setLargeIcon(image);
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
            channel.setVibrationPattern(new long[]{0,250,250,250});
            //Register the channel with the system
            //You cannot change importance or other notification behaviours after this
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
