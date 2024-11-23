package com.example.oeg.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.widget.Toast;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.view.LayoutInflater;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.example.oeg.Etc.MessageParser;
import com.example.oeg.Etc.VoiceToText;
import com.example.oeg.MainActivity;
import com.example.oeg.R;
import com.example.oeg.mode.Mode;
import com.example.oeg.popup.PopupManager;


public class OverlayService extends Service implements Mode.ModeListener {
    private static final String CHANNEL_ID = "overlay_service_channel";
    private Overlay overlay;  // 기존 Overlay 클래스 인스턴스
    private WindowManager windowManager;

    private Mode mode;
    private VoiceToText voiceToText;
    private PopupManager popupManager;

    private Observer<MessageParser.ParsedMessage> replyObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        // 기존 Overlay 인스턴스 생성
        // 알림 채널 생성
        createNotificationChannel();

        // 포그라운드 서비스 시작
        startForegroundService();

        // 필요한 객체 초기화
        popupManager = new PopupManager();
        voiceToText = new VoiceToText(this, voiceToTextListener);
        mode = Mode.getInstance();
        mode.addListener(this);

        overlay = new Overlay(this, voiceToText);
        overlay.showOverlay();

    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Overlay Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("오버레이 서비스 실행 중")
                .setContentText("서비스가 실행 중입니다.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onNewReply(MessageParser.ParsedMessage parsedMessage) {
        new Handler(Looper.getMainLooper()).post(() -> {
            popupManager.showResponsePopup(this, parsedMessage, updatedResponse -> {

            });
        });
    }

    @Override
    public void onError(String error) {
        // 에러 발생 시 처리 로직
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(this, "오류: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private VoiceToText.SpeechToTextListener voiceToTextListener = new VoiceToText.SpeechToTextListener() {
        @Override
        public void onSpeechResult(String result) {
            mode.setMessage(result);
            mode.sendMessage();
        }

        @Override
        public void onSpeechError(String errorMessage) {
            Toast.makeText(OverlayService.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlay != null) {
            overlay.removeOverlay();
            overlay.destroy();
        }
        mode.removeListener(this);
        System.exit(0);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //overlay.showOverlay();
        // 서비스가 계속 실행되도록 설정
        return START_STICKY; // 화면이 꺼져도 서비스가 유지되도록
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
