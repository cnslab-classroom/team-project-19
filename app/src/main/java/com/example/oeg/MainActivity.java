package com.example.oeg;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.oeg.Etc.MYAccessibilityService;
import com.example.oeg.Etc.VoiceToText;
import com.example.oeg.mode.Mode;
import com.example.oeg.overlay.Overlay;


import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;


import com.example.oeg.overlay.OverlayService;
import com.example.oeg.popup.PopupManager;


public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    private Overlay overlay;


    private Mode mode;
    private VoiceToText voiceToText;

    private RelativeLayout answerListContainer;

    private PopupManager popupManager;
    private BroadcastReceiver overlayActionReceiver;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //moveTaskToBack(true);

        mode = new ViewModelProvider(this).get(Mode.class);
        overlayActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.hasExtra("action")) {
                    String action = intent.getStringExtra("action");
                    switch (action) {
                        case "start_study_mode":
                            Log.d(TAG, "Study Mode 시작 intent");
                            mode.setModel("gpt-4");
                            break;
                        case "exit_study_mode":
                            Log.d(TAG, "Study Mode 끝 intent");
                            mode.setModel("gpt-3.5-turbo");
                            break;
                        case "end":
                            Log.d(TAG, "어플 종료 intent");
                            finish();
                            break;
                        case "record":
                            Log.d(TAG, "녹음시작 intent");
                            // 권한 체크 및 녹음 시작
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                            } else {
                                voiceToText.startListening();
                            }
                            break;
                        case "drag":
                            Log.d(TAG, "드래그 intent");
                            String draggedText = MYAccessibilityService.getCurrentSelectedText();
                            mode.setMessage(draggedText);
                            mode.sendMessage();
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.oeg.OVERLAY_ACTION");
        registerReceiver(overlayActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // PopupManager 초기화
        popupManager = new PopupManager();

        voiceToText = new VoiceToText(this, new VoiceToText.SpeechToTextListener() {
            @Override
            public void onSpeechResult(String result) {
                mode.setMessage(result); // GPT에게 보낼 메시지 설정
                mode.sendMessage(); // 메시지 전송
            }

            @Override
            public void onSpeechError(String errorMessage) {
                Toast.makeText(MainActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // LiveData 관찰자 설정
        mode.getNewReplyLiveData().observe(this, parsedMessage -> {
            if (parsedMessage != null) {
                popupManager.showResponsePopup(this, parsedMessage, updatedResponse -> {

                });
            }
        });

        if (Settings.canDrawOverlays(MainActivity.this)) {
            //overlay.showOverlay(); // 오버레이 표시
            Intent serviceIntent = new Intent(MainActivity.this, OverlayService.class);
            startService(serviceIntent);


        } else {
            // 권한 요청
            requestOverlayPermission();
        }

    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceToText.startListening();
            } else {
                Toast.makeText(this, "오디오 녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 브로드캐스트 리시버 해제
        unregisterReceiver(overlayActionReceiver);
        // 앱 종료 시 음성 인식 객체 해제
        voiceToText.destroy();
    }


    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        //MainActivity.this.startActivity(intent);
    }

    // 권한 요청 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (Settings.canDrawOverlays(this)) {
                overlay.showOverlay(); // 권한이 승인되면 오버레이 표시
            } else {
                Toast.makeText(this, "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
