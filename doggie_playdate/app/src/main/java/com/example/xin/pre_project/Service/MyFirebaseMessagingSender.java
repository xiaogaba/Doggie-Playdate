package com.example.xin.pre_project.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import android.os.Handler;

import com.example.xin.pre_project.Helper.NotificationHelper;
import com.example.xin.pre_project.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//riderApp
public class MyFirebaseMessagingSender extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage){
        if(remoteMessage.getNotification().getTitle().equals("Cancel")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessagingSender.this, "" + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(remoteMessage.getNotification().getTitle().equals("Arrived")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());
            else
                showArrivedNotification(remoteMessage.getNotification().getBody());
        }
        else if(remoteMessage.getNotification().getTitle().equals("DropOff")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                openRateActivity(remoteMessage.getNotification().getBody());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent contentIntent = PendingIntent.getActivities(getBaseContext(),
                0, new Intent[]{new Intent()},PendingIntent.FLAG_ONE_SHOT);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        //Notification.Builder builder = notificationHelper.getDoggieNotification("Arrived",body,contentIntent,defaultSound);
        //notificationHelper.getManager().notify(1,builder.build());

    }

    private void openRateActivity(String body) {
    }

    private void showArrivedNotification(String body) {
        PendingIntent contentIntent = PendingIntent.getActivities(getBaseContext(),
                0, new Intent[]{new Intent()},PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.pawprint)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }
}
