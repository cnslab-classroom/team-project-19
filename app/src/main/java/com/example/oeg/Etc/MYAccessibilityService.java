package com.example.oeg.Etc;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

public class MYAccessibilityService extends AccessibilityService {

    private static String currentSelectedText = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            // 선택된 텍스트 가져오기
            List<CharSequence> textList = event.getText();
            if (textList != null && !textList.isEmpty()) {
                currentSelectedText = textList.get(0).toString();
                Log.d("SelectedText", "드래그한 내용: " + currentSelectedText);
            }
        }

    }


    @Override
    public void onInterrupt() {
        // 서비스가 인터럽트되었을 때 처리하는 부분
    }

    public static String getCurrentSelectedText() {
        return currentSelectedText;
    }
}
