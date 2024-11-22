package com.example.oeg.Etc;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.List;

public class MYAccessibilityService extends AccessibilityService {

    public static String selectedText = ""; // 전역 변수로 선택된 텍스트 저장

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            // 선택된 텍스트 가져오기
            List<CharSequence> textList = event.getText();
            if (textList != null && !textList.isEmpty()) {
                selectedText = textList.get(0).toString();
                Log.d("SelectedText", "드래그한 텍스트: " + selectedText);
            }
        }
    }


    @Override
    public void onInterrupt() {
        // 서비스가 인터럽트되었을 때 처리하는 부분
    }
}
