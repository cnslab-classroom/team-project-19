package com.example.oeg.popup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.oeg.R;


public class TextBalloon {
    private final Context context;

    // Constructor to initialize the context
    public TextBalloon(Context context) {
        this.context = context;
    }

    public void showBubbleText(String text) {
        try {
            // 말풍선 레이아웃 inflate
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater == null) {
                throw new NullPointerException("LayoutInflater is null");
            }

            View bubbleView = inflater.inflate(R.layout.bubble_layout, null);

            // TextView에 텍스트 설정
            TextView bubbleTextView = bubbleView.findViewById(R.id.bubble_text);
            if (bubbleTextView == null) {
                throw new NullPointerException("TextView not found in layout");
            }
            bubbleTextView.setText(text);

            // 간단한 예제로 Toast로 표시 (UI에 추가하는 로직은 개발 환경에 따라 수정 필요)
            Toast toast = new Toast(context);
            toast.setView(bubbleView);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
