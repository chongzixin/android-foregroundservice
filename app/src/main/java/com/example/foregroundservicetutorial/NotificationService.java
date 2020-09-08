package com.example.foregroundservicetutorial;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private static final int ID_SERVICE = 101;

    private static final String PACKAGE_NAME = "com.example.foregroundservicetutorial";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    private Timer timer;

    @Nullable
    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = createNotificationChannel(notificationManager);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        timer = new Timer();

        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Notification Service")
                .setContentText("This should change to the current time")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(ID_SERVICE, notification);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // start timer to run every minute, write to file
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i("NOTIFICATION_SERVICE", "ran at " + Utils.getCurrentDateTime());

                // try in separate thread - timertask -> get at thread -> new runnable
                writeDateTimeToFile();

                // Notify anyone listening for broadcasts about the new location.
                Intent intent = new Intent(ACTION_BROADCAST);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            }}, 30000, 30000); // 60000 milliseconds = 1 minute

        return START_STICKY;
    }

    private void writeDateTimeToFile() {
        String currentTime = Utils.getCurrentDateTime();
        Utils.writeToFile(currentTime, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "NAVIGINE_SERVICE_CHANNEL_ID_EXAMPLE";
        String channelName = "NAVIGINE CHANNEL NAME EXAMPLE";
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // will be shown in lock screen
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}
