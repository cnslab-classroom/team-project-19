package com.example.oeg.overlay;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.oeg.Etc.MYAccessibilityService;
import com.example.oeg.Etc.VoiceToText;
import com.example.oeg.MainActivity;
import com.example.oeg.R;
import com.example.oeg.mode.Mode;
import com.example.oeg.popup.PopupManager;


public class OverlayService extends Service{

    private Overlay overlay;  // 기존 Overlay 클래스 인스턴스
    private static final String TAG = "OverlayService";
    private static final String CHANNEL_ID = "OverlayServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private WindowManager windowManager;
    private Button studyButton, endButton, recordButton, dragButton, normalButton;
    private Handler idleHandler = new Handler();
    private Runnable idleRunnable;
    private static final int IDLE_TIME = 10000; // 10초
    private boolean isNormalMode = true;
    private VoiceToText voiceToText;
    private Mode mode;
    private PopupManager popupManager;

    @Override
    public void onCreate() {
        super.onCreate();


        // 알림 채널 생성
        createNotificationChannel();

        // 알림 인텐트 설정 (MainActivity로 돌아가기 위한)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 알림 생성
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Overlay Service")
                .setContentText("오버레이 서비스가 실행 중입니다.")
                .setSmallIcon(R.mipmap.ic_notification) // 적절한 아이콘으로 교체
                .setContentIntent(pendingIntent)
                .build();

        // 포그라운드
        startForeground(NOTIFICATION_ID, notification);

        // 초기화
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mode = Mode.getInstance();
        popupManager = new PopupManager();

        // VoiceToText 초기화
        voiceToText = new VoiceToText(this, new VoiceToText.SpeechToTextListener() {
            @Override
            public void onSpeechResult(String result) {
                mode.setMessage(result);
                mode.sendMessage();

            }
            @Override
            public void onSpeechError(String errorMessage) {
                Toast.makeText(OverlayService.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        mode.getNewReplyLiveData().observe((LifecycleOwner) this, parsedMessage -> {
            if (parsedMessage != null) {
                popupManager.showResponsePopup(this, parsedMessage, updatedResponse -> {

                });
            }
        });

        // 브로드캐스트 리시버 등록
        registerOverlayActionReceiver();
        
        // 기존 Overlay 인스턴스 생성
        overlay = new Overlay(this);

        // 오버레이 표시
        overlay.showOverlay();
    }

    private void createNotificationChannel() {

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Overlay Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }

    }

    private BroadcastReceiver overlayActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("action")) {
                String action = intent.getStringExtra("action");
                Log.d(TAG, "Received action: " + action);
                handleAction(action);
            }
        }
    };

    private void registerOverlayActionReceiver() {
        IntentFilter filter = new IntentFilter("com.example.oeg.OVERLAY_ACTION");
        registerReceiver(overlayActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterOverlayActionReceiver() {
        unregisterReceiver(overlayActionReceiver);
    }

    private void handleAction(String action) {
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
                stopSelf();
                break;
            case "record":
                Log.d(TAG, "녹음시작 intent");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "오디오 녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
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
            default:
                Log.d(TAG, "알 수 없는 action: " + action);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // 화면이 꺼져도 서비스가 유지되도록
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 오버레이를 제거
        overlay.removeOverlay();
        overlay.destroy();
        unregisterOverlayActionReceiver();
        voiceToText.destroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
