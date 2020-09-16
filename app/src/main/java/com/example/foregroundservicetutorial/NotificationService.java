package com.example.foregroundservicetutorial;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private static final int NOTIFICATION_SERVICE_ID = 101;
    private static final String TAG = "NOTIFICATION_SERVICE";
    private static final String CHANNEL_ID = "NOTIFICATION_SERVICE_CHANNELID";
    private static final String PACKAGE_NAME = "com.example.foregroundservicetutorial";
    static final String INTENT_EXTRA = "MESSAGE";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    private Timer timer;
    private NotificationManager notificationManager;

    @Nullable
    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override public void onCreate() {
        super.onCreate();
        Log.i(TAG, Utils.getCurrentDateTime() + " onCreate Service");
        Utils.writeToFile(Utils.getCurrentDateTime() + " onCreate Service", this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOREGROUNDAPP_SERVICE_WAKELOCK:"+TAG);
        if(!wakeLock.isHeld()) wakeLock.acquire();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        timer = new Timer();

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Set the Notification Channel for the Notification Manager.
            notificationManager.createNotificationChannel(channel);
        }

        // recreate itself every 5 minutes by calling onCreate again
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.i(TAG, Utils.getCurrentDateTime() + " recreated Service");
                onCreate();
            }
        }, 5*60*1000);

        startForeground(NOTIFICATION_SERVICE_ID, getNotification());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, Utils.getCurrentDateTime() + " onStartCommand Service");
        Utils.writeToFile(Utils.getCurrentDateTime() + " onStartCommand Service", this);

        // start timer to run every minute, write to file
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String datetime = Utils.getCurrentDateTime();
                Log.i(TAG, "timer ran at " + datetime);

                // try in separate thread - timertask -> get at thread -> new runnable
                writeDateTimeToFile();

                // Notify anyone listening for broadcasts about the new location.
                Intent intent = new Intent(ACTION_BROADCAST);
                intent.putExtra(INTENT_EXTRA, datetime);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                // update the notification
                notificationManager.notify(NOTIFICATION_SERVICE_ID, getNotification());
            }}, 60000, 60000); // 60000 milliseconds = 1 minute

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.writeToFile(Utils.getCurrentDateTime() + " onDestroy Service", this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Utils.writeToFile(Utils.getCurrentDateTime() + " onLowMemory Service", this);
        Log.i(TAG, Utils.getCurrentDateTime() + " onLowMemory Service");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Utils.writeToFile(Utils.getCurrentDateTime() + " onTaskRemoved Service", this);
        Log.i(TAG, Utils.getCurrentDateTime() + " onTaskRemoved Service");
    }

    private void writeDateTimeToFile() {
        String currentTime = Utils.getCurrentDateTime();
        Utils.writeToFile(currentTime, this);
    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText("-")
                .setContentTitle("Updated: " + Utils.getCurrentDateTime())
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(Utils.getCurrentDateTime())
                .setWhen(System.currentTimeMillis())
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }
}
