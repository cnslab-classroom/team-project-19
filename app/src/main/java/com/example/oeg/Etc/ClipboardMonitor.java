package com.example.oeg.Etc;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class ClipboardMonitor extends Service {

    private static final String TAG = "ClipboardMonitorService";
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipChangedListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "서비스 생성됨.");

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    ClipData clipData = clipboardManager.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        ClipData.Item item = clipData.getItemAt(0);
                        CharSequence copiedText = item.getText();
                        if (copiedText != null && !copiedText.toString().isEmpty()) {
                            Clipboard.updateCurrentCopiedText(copiedText.toString());
                            Log.d(TAG, "클립보드 변경 감지: " + copiedText.toString());
                        }
                    }
                }
            };
            clipboardManager.addPrimaryClipChangedListener(clipChangedListener);
            Log.d(TAG, "클립보드 리스너 등록됨.");
        } else {
            Log.e(TAG, "ClipboardManager를 초기화 못함.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clipboardManager != null && clipChangedListener != null) {
            clipboardManager.removePrimaryClipChangedListener(clipChangedListener);
            Log.d(TAG, "클립보드 리스너 제거됨.");
        }
        Log.d(TAG, "서비스 소멸됨.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
