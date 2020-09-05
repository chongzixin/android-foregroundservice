package com.example.foregroundservicetutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button btnStartService, btnStopService;
    TextView txtLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);
        txtLabel = findViewById(R.id.txtLabel);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
                txtLabel.setText("start service");
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
                txtLabel.setText("stop service");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // take the string from file, add a line break after so that new rows get written nicely
        String text = Utils.readFromFile(this) + "\n";
        txtLabel.setText(text);
    }

    public void startService () {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService () {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);
    }
}