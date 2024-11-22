package com.example.oeg.overlay;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
import com.example.oeg.R;


public class OverlayService extends Service{

    private Overlay overlay;  // 기존 Overlay 클래스 인스턴스
    private WindowManager windowManager;
    private View overlayView;

    @Override
    public void onCreate() {
        super.onCreate();

        // 기존 Overlay 인스턴스 생성
        overlay = new Overlay(this);

        // 오버레이 표시
        overlay.showOverlay();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //overlay.showOverlay();
        // 서비스가 계속 실행되도록 설정
        return START_STICKY; // 화면이 꺼져도 서비스가 유지되도록
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 오버레이를 제거
        overlay.removeOverlay();
        //overlay.destroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
