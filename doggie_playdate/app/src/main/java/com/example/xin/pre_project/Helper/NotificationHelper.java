package com.example.xin.pre_project.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.xin.pre_project.Model.Notification;
import com.example.xin.pre_project.R;

import static android.app.Notification.VISIBILITY_PRIVATE;
import static com.example.xin.pre_project.Model.Notification.*;


public class NotificationHelper extends ContextWrapper {
    private static final String EDMT_CHANNEL_ID = "edmt.dev.androidriderapp.EDMTDEV";
    private static final String EDMT_CHANNEL_NAME = "EDMTDEV Uber";

    private NotificationManager manager;
    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel edmtChannels = new NotificationChannel(EDMT_CHANNEL_ID,
                EDMT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        edmtChannels.enableLights(true);
        edmtChannels.enableVibration(true);
        edmtChannels.setLightColor(Color.GRAY);
        edmtChannels.setLockscreenVisibility(VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(edmtChannels);
    }

    public NotificationManager getManager() {
        if(manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder getDoggieNotification(String title, String content, PendingIntent contenIntent, Uri soundUri){
        return new android.app.Notification.Builder(getApplicationContext(),EDMT_CHANNEL_ID)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(contenIntent)
                .setSmallIcon(R.drawable.pawprint);
    }
}
