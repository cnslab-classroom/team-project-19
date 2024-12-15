package com.example.oeg;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.oeg.overlay.Overlay;
import com.example.oeg.overlay.OverlayService;

public class MainActivity extends AppCompatActivity {
    Overlay overlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        setContentView(R.layout.activity_main);
        if(isFirstRun){
            prefs.edit().putBoolean("isFirstRun", false).apply();
            finishAffinity();
        } else {
            startOverlayService();
        }



    }


    private void startOverlayService() {
        Intent serviceIntent = new Intent(this, OverlayService.class);

        startForegroundService(serviceIntent);
        finish();
    }


}
