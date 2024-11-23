package com.example.oeg.Etc;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MYAccessibilityService extends AccessibilityService {

    private ClipboardManager clipboardManager;
    private final int DELAY_MILLIS = 2000; // 포커스 요청할 때 2초 뒤에

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(() -> {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    CharSequence copiedText = item.getText();
                    if (copiedText != null) {
                        Clipboard.updateCurrentCopiedText(copiedText.toString());
                        Log.d("ClipboardMonitor", "접근성 서비스에서 클립보드 변경 감지: " + copiedText.toString());

                    }
                }
            });
        } else {
            Log.d("ClipboardMonitor", "ClipboardManager를 초기화할 수 없습니다.");
        }
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {
        // 접근성 서비스 중단 시 필요한 처리
    }
}

