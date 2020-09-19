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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAINACTIVITY";
    private static final String PACKAGE_NAME = "com.example.foregroundservicetutorial";

    Button btnStartService, btnStopService, btnWhitelist, btnManufacturerIntent;
    TextView txtLabel;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    MyReceiver myReceiver;

    PowerManager powerManager;
    Handler handler;

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

        Utils.writeToFile(Utils.getCurrentDateTime() + " onCreate MainActivity", this);

        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);
        btnWhitelist = findViewById(R.id.btnWhitelist);
        btnManufacturerIntent = findViewById(R.id.btnManufacturerIntent);
        txtLabel = findViewById(R.id.txtLabel);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

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

        btnWhitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                String whitelistStatus = "Whitelisted: ";
                if (powerManager.isIgnoringBatteryOptimizations(PACKAGE_NAME)) {
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    whitelistStatus += "YES";
                } else {
                    // show the intent to add this app to whitelist
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + PACKAGE_NAME));
                    whitelistStatus += "NO";
                }
                btnWhitelist.setText(whitelistStatus);
                startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(NotificationService.ACTION_BROADCAST));

        // take the string from file, add a line break after so that new rows get written nicely
        String text = Utils.readFromFile(this) + "\n";
        txtLabel.setText(text);

        // check the whitelist status then set the button text.
        String whitelistStatus = "Whitelisted: ";
        if (powerManager.isIgnoringBatteryOptimizations(PACKAGE_NAME)) {
            whitelistStatus += "YES";
        } else {
            whitelistStatus += "NO";
        }
        btnWhitelist.setText(whitelistStatus);
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
    // when NotificationService sends a new timestamp, update the label
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