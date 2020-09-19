package com.example.foregroundservicetutorial;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_SERVICE_ID = 101;
    private static final String TAG = "ALARM_RECEIVER";
    private static final String PACKAGE_NAME = "com.example.foregroundservicetutorial";
    static final String INTENT_EXTRA = "MESSAGE";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // schedule for next alarm
        scheduleExactAlarm(context, (AlarmManager)context.getSystemService(Context.ALARM_SERVICE));

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOREGROUNDAPP_ALARMRECEIVER_WAKELOCK:ALARM_RECEIVER");
        wakeLock.acquire();

        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                // do job
                Log.i(TAG, Utils.getCurrentDateTime() + " in AlarmManager run()");
                String datetime = Utils.getCurrentDateTime();

                writeDateTimeToFile(context);

                // Notify anyone listening for broadcasts about the new location.
                Intent intent = new Intent(ACTION_BROADCAST);
                intent.putExtra(INTENT_EXTRA, datetime);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                // update the notification
//                context.getSystemService(Context.NOTIFICATION_SERVICE).notify(NOTIFICATION_SERVICE_ID, getNotification());
            }
        };

        handler.post(periodicUpdate);
        wakeLock.release();
    }

    public static void scheduleExactAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Log.i(TAG, "scheduling next alarm now.");
        alarms.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+10*1000-SystemClock.elapsedRealtime()%1000, pendingIntent);
    }

    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarms.cancel(pendingIntent);
    }

    private void writeDateTimeToFile(Context context) {
        String currentTime = Utils.getCurrentDateTime();
        Utils.writeToFile(currentTime, context);
    }
}
