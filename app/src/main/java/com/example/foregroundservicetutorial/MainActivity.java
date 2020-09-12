package com.example.foregroundservicetutorial;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAINACTIVITY";

    Button btnStartService, btnStopService, btnManufacturerIntent;
    TextView txtLabel;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    MyReceiver myReceiver;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT), // xiaomi - set battery saver to no restrictions
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")), // xiaomi - enable mobile data when device is locked
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")), // xiaomi - add app to AutoStart
            new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT), // xiaomi - seems to be the same as AutoStart based on Redmi Note 3
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")), // huawei
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")), // huawei
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")), //huawei
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")), // oppo
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")), // oppo
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")), // oppo
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")), // vivo
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")), // vivo
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")), // vivo
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")), // samsung
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myReceiver = new MyReceiver();

        Utils.writeToFile("First Launched on " + Utils.getCurrentDateTime(), this);

        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);
        btnManufacturerIntent = findViewById(R.id.btnManufacturerIntent);
        txtLabel = findViewById(R.id.txtLabel);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOREGROUNDAPP_WAKELOCK:"+TAG);
        wakeLock.acquire();

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
                txtLabel.setText("start service");
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
                txtLabel.setText("stop service");
                btnStartService.setEnabled(true);
                btnStopService.setEnabled(false);
            }
        });

        btnManufacturerIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean nothingToShow = true;
                for (Intent intent : POWERMANAGER_INTENTS)
                    if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        nothingToShow = false;
                        // may start multiple intents, so need to press BACK to check
                        startActivity(intent);
                    }
                if(nothingToShow)
                    Toast.makeText(getApplicationContext(), "No Manufacturer Settings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(NotificationService.ACTION_BROADCAST));

        // take the string from file, add a line break after so that new rows get written nicely
        String text = Utils.readFromFile(this) + "\n";
        txtLabel.setText(text);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    public void startService () {
        Log.i(TAG, "starting service now");
        Intent serviceIntent = new Intent(this, NotificationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService () {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);
    }

    // class that handles broadcasts events
    // 1. when NotificationService sends a new timestamp, update the label
    // 2. when the system is rebooted, start the service again
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if(intentAction != null) {
                if (intentAction.equals(NotificationService.ACTION_BROADCAST)) {
                    // take the additional text from the intent, then display in the label
                    String extra = intent.getStringExtra(NotificationService.INTENT_EXTRA);
                    String text = extra + "\n" + txtLabel.getText();
                    txtLabel.setText(text);
                }
            }
        }
    }
}