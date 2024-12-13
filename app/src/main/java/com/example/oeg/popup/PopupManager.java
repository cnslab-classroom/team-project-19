package com.example.oeg.popup;

import android.app.Dialog;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;
import android.os.Handler;

import com.example.oeg.Etc.MessageParser;
import com.example.oeg.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PopupManager {
    private static final int MAX_POPUPS = 8; // 최대 팝업 개수
    private final List<Dialog> activePopups = new ArrayList<>();

    private MessageParser.ParsedMessage parsedMessage;
    private static final int[] PASTEL_COLORS = {
            Color.parseColor("#FFCDD2"), // Light Red
            Color.parseColor("#F8BBD0"), // Light Pink
            Color.parseColor("#E1BEE7"), // Light Purple
            Color.parseColor("#D1C4E9"), // Light Deep Purple
            Color.parseColor("#C5CAE9"), // Light Indigo
            Color.parseColor("#BBDEFB"), // Light Blue
            Color.parseColor("#B3E5FC"), // Light Cyan
            Color.parseColor("#B2EBF2"), // Light Teal
            Color.parseColor("#C8E6C9"), // Light Green
            Color.parseColor("#DCEDC8"), // Light Lime
    };

    public void showResponsePopup(Context context, MessageParser.ParsedMessage response, Consumer<String> onResponse) {
        if (activePopups.size() >= MAX_POPUPS) {
            return;
        }

        parsedMessage = response;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_response_popup);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);// 추가


        View popupLayout = dialog.findViewById(R.id.popup_layout);
        popupLayout.setBackgroundColor(getRandomPastelColor());

        // EditText를 할당
        EditText responseEditText = dialog.findViewById(R.id.response_edit_text);
        LinearLayout codeContainer = dialog.findViewById(R.id.codeContainer);
        // 응답을 EditText에 출력
        responseEditText.setText(parsedMessage.textContent);  // 텍스트를 EditText에 설정
        responseEditText.setVisibility(View.VISIBLE);  // EditText는 보이기

        for (String code : parsedMessage.codeBlocks) {
            // 코드 블록 추가
            TextView codeTextView = new TextView(context);
            codeTextView.setText(code); // 코드 내용 설정
            codeTextView.setTypeface(Typeface.MONOSPACE); // 고정 폭 글꼴
            codeTextView.setTextSize(14); // 글꼴 크기
            codeTextView.setPadding(16, 16, 16, 16); // 패딩 추가
            codeTextView.setBackgroundColor(Color.parseColor("#F5F5F5")); // 코드 배경색 설정
            codeTextView.setTextColor(Color.BLACK); // 텍스트 색상 설정
            codeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )); // 레이아웃 매개변수 설정
            codeContainer.addView(codeTextView); // 코드 블록을 레이아웃에 추가
        }


        ImageButton closeButton = dialog.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> {
            dialog.dismiss();
            activePopups.remove(dialog);
        });

        View dragArea = dialog.findViewById(R.id.drag_area);
        dragArea.setOnTouchListener(new DragTouchListener(dialog));

        View resizeHandle = dialog.findViewById(R.id.resize_handle);
        resizeHandle.setOnTouchListener(new ResizeTouchListener(dialog));

        // 팝업 외부 터치 시 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false);

        // 팝업 창을 비모달로 만들기 위해 설정
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        dialog.getWindow().setAttributes(layoutParams);

        //포커스 해제
        dialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    dialog.getWindow().setAttributes(params);
                    Log.d("PopupManager", "포커스해제");
                    return true;
                }
                return false;
            }
        });
        //포커스 복구
        responseEditText.setOnClickListener(v -> {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            dialog.getWindow().setAttributes(params);
            Log.d("PopupManager", "포커스복구");
        });


        // 더블 클릭 이벤트 추가
        popupLayout.setOnTouchListener(new DoubleTapListener(context, popupLayout));

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        activePopups.add(dialog);
    }

    public void closeAllPopups() {
        for (Dialog dialog : activePopups) {
            dialog.dismiss();
        }
        activePopups.clear();
    }

    private int getRandomPastelColor() {
        Random random = new Random();
        return PASTEL_COLORS[random.nextInt(PASTEL_COLORS.length)];
    }

    private static class DragTouchListener implements View.OnTouchListener {
        private final Dialog dialog;
        private float initialX, initialY;
        private float touchOffsetX, touchOffsetY;

        public DragTouchListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = dialog.getWindow().getAttributes().x;
                    initialY = dialog.getWindow().getAttributes().y;
                    touchOffsetX = event.getRawX();
                    touchOffsetY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - touchOffsetX;
                    float dy = event.getRawY() - touchOffsetY;
                    dialog.getWindow().setAttributes(updateWindowPosition(dialog, (int) (initialX + dx), (int) (initialY + dy)));
                    return true;
            }
            return false;
        }
    }

    private static class ResizeTouchListener implements View.OnTouchListener {
        private final Dialog dialog;
        private float initialX, initialY;
        private int initialWidth, initialHeight;

        public ResizeTouchListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            View popupLayout = dialog.findViewById(R.id.popup_layout);
            ViewGroup.LayoutParams layoutParams = popupLayout.getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getRawX();
                    initialY = event.getRawY();
                    initialWidth = layoutParams.width;
                    initialHeight = layoutParams.height;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - initialX;
                    float dy = event.getRawY() - initialY;
                    // Update LayoutParams with new dimensions
                    layoutParams = updateLayoutParams(layoutParams, (int) (initialWidth + dx), (int) (initialHeight + dy));
                    popupLayout.setLayoutParams(layoutParams);
                    return true;
            }
            return false;
        }

        // updateLayoutParams 메서드 정의
        private ViewGroup.LayoutParams updateLayoutParams(ViewGroup.LayoutParams layoutParams, int newWidth, int newHeight) {
            layoutParams.width = newWidth;
            layoutParams.height = newHeight;
            return layoutParams;
        }
    }

    private static WindowManager.LayoutParams updateWindowPosition(Dialog dialog, int x, int y) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.x = x;
        params.y = y;
        return params;
    }

    // 더블 클릭 리스너
    private static class DoubleTapListener implements View.OnTouchListener {
        private static final int DOUBLE_TAP_TIMEOUT = 300;
        private final Context context;
        private final View targetView;
        private int tapCount = 0;
        private final Handler handler = new Handler();

        public DoubleTapListener(Context context, View targetView) {
            this.context = context;
            this.targetView = targetView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                tapCount++;

                if (tapCount == 2) {
                    tapCount = 0;
                    handler.removeCallbacksAndMessages(null);
                    saveViewToClipboard();
                    return true;
                }

                handler.postDelayed(() -> tapCount = 0, DOUBLE_TAP_TIMEOUT);
            }
            return false;
        }

        private void saveViewToClipboard() {
            Bitmap bitmap = Bitmap.createBitmap(targetView.getWidth(), targetView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            targetView.draw(canvas);

            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newUri(context.getContentResolver(), "Popup Image", bitmapToUri(context, bitmap));
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "이미지가 클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
        }

        private Uri bitmapToUri(Context context, Bitmap bitmap) {
            try {
                // 내부 캐시 디렉토리에서 파일 생성
                File cacheDir = new File(context.getCacheDir(), "images");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs(); // 디렉토리가 없으면 생성
                }

                // 고유한 파일 이름 생성
                String fileName = "popup_image_" + System.currentTimeMillis() + ".png";
                File file = new File(cacheDir, fileName);

                // Bitmap을 파일로 저장
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos); // PNG 포맷으로 저장
                fos.flush();
                fos.close();

                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

                ClipData clip = ClipData.newUri(context.getContentResolver(), "Image", uri);  // "Image"는 label
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(clip);

                // 저장된 파일의 Uri 반환
                return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            } catch (IOException e) {
                e.printStackTrace();
                return null; // 예외 발생 시 null 반환
            }
        }
    }
}