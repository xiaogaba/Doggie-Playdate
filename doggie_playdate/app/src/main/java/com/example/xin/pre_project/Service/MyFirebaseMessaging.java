package com.example.xin.pre_project.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.xin.pre_project.CustomerCall;
import com.example.xin.pre_project.Helper.NotificationHelper;
import com.example.xin.pre_project.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
//driverPart
public class MyFirebaseMessaging extends FirebaseMessagingService{
    /*

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("EDMTDEV",remoteMessage.getNotification().getBody());
        //Because I will send the Firbase message with contain lat and lng from Rider app
        //So I need convert message to LatLng

    }
    */

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage){
        if(remoteMessage.getNotification().getTitle().equals("Accepted")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this, "" + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if(remoteMessage.getNotification().getTitle().equals("Cancel")) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this, "" + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
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
        } else {
            LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
            Intent intent  = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat",customer_location.latitude);
            intent.putExtra("lng",customer_location.longitude);
            intent.putExtra("customer",remoteMessage.getNotification().getTitle());
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(final String body) {
        PendingIntent contentIntent = PendingIntent.getActivities(getBaseContext(),
                0, new Intent[]{new Intent()},PendingIntent.FLAG_ONE_SHOT);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getDoggieNotification("Arrived",body,contentIntent,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notificationHelper.getManager().notify(1,builder.build());
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFirebaseMessaging.this, "" + body, Toast.LENGTH_SHORT).show();
            }
        });
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
//part 9 16:33
