package com.example.oeg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.oeg.overlay.OverlayService;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 5678;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 레이아웃을 최소화하거나 공백으로 설정
        setContentView(R.layout.activity_main);
        moveTaskToBack(true); // 앱을 백그라운드로 이동

        // 오버레이 권한 확인
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else {
            checkAndRequestAudioPermission();
        }
    }

    private void checkAndRequestAudioPermission() {
        // 오디오 녹음 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            // 포그라운드 서비스 권한 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE_MICROPHONE},
                        RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            } else {
                startOverlayService();
            }
        }
    }

    private void startOverlayService() {
        Intent serviceIntent = new Intent(this, OverlayService.class);

        startForegroundService(serviceIntent);

    }

    private void requestOverlayPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            new AlertDialog.Builder(this)
                    .setTitle("오버레이 권한 필요")
                    .setMessage("이 앱은 오버레이 권한이 필요합니다. 권한을 허용해주세요.")
                    .setPositiveButton("허용", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                        Toast.makeText(this, "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OVERLAY_PERMISSION_REQUEST_CODE){
            if(Settings.canDrawOverlays(this)){
                checkAndRequestAudioPermission();
            } else {
                Toast.makeText(this, "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 오디오 권한이 허용되면 포그라운드 서비스 권한도 확인
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    startOverlayService();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.FOREGROUND_SERVICE_MICROPHONE},
                            RECORD_AUDIO_PERMISSION_REQUEST_CODE);
                }
            } else {
                Toast.makeText(this, "오디오 녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 필요 시 서비스 종료
        Intent serviceIntent = new Intent(this, OverlayService.class);
        stopService(serviceIntent);
    }
}
