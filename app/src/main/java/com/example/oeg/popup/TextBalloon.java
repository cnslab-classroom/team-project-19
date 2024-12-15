package com.example.oeg.popup;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.oeg.R;

public class TextBalloon {
    private final Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    // Constructor to initialize the context
    public TextBalloon(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void showBubbleText(String text,int characterX, int characterY) {
        try {
            // LayoutInflater를 통한 레이아웃 인플레이트
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater == null) {
                throw new NullPointerException("LayoutInflater is null");
            }

            View bubbleView = inflater.inflate(R.layout.bubble_layout, null);
            TextView bubbleTextView = bubbleView.findViewById(R.id.bubble_text);
            bubbleTextView.setText(text);

            // Measure the text and dynamically adjust size
            bubbleTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int textWidth = bubbleTextView.getMeasuredWidth();
            int textHeight = bubbleTextView.getMeasuredHeight();

            // Calculate the bubble size with padding
            int bubbleWidth = Math.max(textWidth + 180, 350); // 최소 너비 100px, 여백 추가
            int bubbleHeight = Math.max(textHeight + 150, 200); // 최소 높이 50px, 여백 추가

            bubbleTextView.setPadding(20, -10, 20, 10);

            // WindowManager와 LayoutParams 설정
            //WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    bubbleWidth,
                    bubbleHeight,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            //updateBubblePosition(characterX, characterY);

            params.x = characterX - (bubbleWidth / 2)+30; // 캐릭터 기준으로 중앙 정렬
            params.y = characterY - bubbleHeight +30; // 캐릭터 위에 배치

            // Add the view to the window
           // WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.addView(bubbleView, params);



            // 오버레이 뷰 추가
            //windowManager.addView(bubbleView, params);


            //new Handler(Looper.getMainLooper()).post(positionUpdateRunnable);

            // 필요하다면 일정 시간 후 제거하는 로직 추가 가능
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                windowManager.removeView(bubbleView);
            }, 6000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
