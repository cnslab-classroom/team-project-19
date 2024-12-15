package com.example.oeg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oeg.overlay.Overlay;
import com.example.oeg.overlay.OverlayService;

public class MainActivity extends AppCompatActivity {

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
