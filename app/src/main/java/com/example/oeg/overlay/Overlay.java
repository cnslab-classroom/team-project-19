package com.example.oeg.overlay;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.os.OutcomeReceiver;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.oeg.Etc.Clipboard;
import com.example.oeg.Etc.MessageParser;
import com.example.oeg.Etc.VoiceToText;
import com.example.oeg.R;
import com.bumptech.glide.Glide;

import android.content.res.Configuration;
import android.widget.Button;
import android.view.GestureDetector;
import android.widget.Toast;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.app.Application;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import com.example.oeg.MainActivity;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import android.os.Process;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.oeg.Etc.MYAccessibilityService;
import com.example.oeg.mode.Mode;
import com.example.oeg.popup.PopupManager;

public class Overlay{
    private final Context context;
    private final WindowManager windowManager;
    private View overlayView;
    private boolean isNormalMode = true;
    private Button studyButton;
    private Button endButton;
    private Button recordButton;
    private Button dragButton;
    private Button nomalButton;
    private Handler idleHandler = new Handler();
    private Runnable idleRunnable;
    //private static final int IDLE_TIME = 10 * 60 * 1000; // 10분 (밀리초)
    private static final int IDLE_TIME = 10000; // 30초

    private VoiceToText voiceToText;
    private Mode mode;

    private String copiedText = "";

    private WindowManager.LayoutParams params;






    public Overlay(Context context, VoiceToText voiceToText) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mode = Mode.getInstance();
        this.voiceToText = voiceToText;
    }


    public void showOverlay() {
        if (overlayView != null) return;

        // WindowManager 초기화
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // WindowManager 설정
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );


        // 화면 방향에 따라 위치 값 설정
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 세로 모드일 때 위치
            //params.gravity = Gravity.BOTTOM | Gravity.END;
            params.x = 300;
            params.y = 1000;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 가로 모드일 때 위치
            //params.gravity = Gravity.BOTTOM | Gravity.END;
            params.x = 1000;
            params.y = 500;
        }

        // Overlay에 표시할 뷰를 생성
        overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null);
        //overlayView.setBackgroundColor(Color.TRANSPARENT);

        ImageView character = overlayView.findViewById(R.id.character_image);
//////////////////////////////////////////////////////////////////////////////////////////////////
        overlayView.setFocusableInTouchMode(true);// 포커스
        overlayView.setFocusable(true);

        overlayView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d("Overlay", "포커스를 획득!");

                    final Handler handler = new Handler(Looper.getMainLooper());
                    final long startTime = System.currentTimeMillis();
                    final long duration = 10000; // 10초
                    final long checkInterval = 500; // 0.5초
                    Runnable checkClipboardRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // 조건 체크
                            if (!Objects.equals(Clipboard.getCurrentCopiedText(), copiedText)) {
                                copiedText = Clipboard.getCurrentCopiedText();
                                mode.setMessage(copiedText);
                                mode.sendMessage();
                                Log.d("Overlay", "클립보드 보냄");

                                params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                                windowManager.updateViewLayout(overlayView, params);
                                overlayView.clearFocus();
                                Log.d("Overlay", "포커스 해제됨 (클립보드 보내짐)"+ params.flags);
                                return;
                            }

                            if (System.currentTimeMillis() - startTime < duration) {
                                handler.postDelayed(this, checkInterval);
                            } else {
                                Log.d("Overlay", "사용자가 복사를 안해서 포커스 끝나게 생김");
                                params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                                windowManager.updateViewLayout(overlayView, params);
                                overlayView.clearFocus();
                                Log.d("Overlay", "포커스 해제됨 (시간 다됨 10초)"+ params.flags);
                            }
                        }
                    };
                    handler.post(checkClipboardRunnable);
                }else {
                    Log.d("Overlay", "포커스 없음.");
                }

            }
        });

///////////////////////////////////////////////////////////////////////////////////

        // Glide로 GIF 이미지 로드
        Glide.with(context)
                .asGif()  // GIF 형식으로 로드
                .load(R.drawable.strange_kkamppag)  // GIF 파일을 로드
                .into(character);  // ImageView에 표시

        //idleRunnable = () -> {

        idleRunnable = new Runnable() {
            @Override
            public void run() {
                // 캐릭터 그림 변경
                int[] characterImages = {
                        R.drawable.ramen,         // 첫 번째 이미지
                        R.drawable.sleepy_kkamppag,       // 두 번째 이미지
                        R.drawable.strange_kkamppag      // 세 번째 이미지
                };

                // 랜덤으로 캐릭터 선택
                int randomIndex = new Random().nextInt(characterImages.length); // 0부터 배열 길이-1 사이의 값
                int selectedCharacter = characterImages[randomIndex];

                Glide.with(context)
                        .asGif()
                        .load(selectedCharacter)  // 새로운 gif 파일 (memo.gif)
                        .into(character);  // ImageView에 설정

                // 원래 이미지로 돌아가는 시간 설정
                int resetTime = 23000; // 기본적으로 23초

                // 선택된 캐릭터에 따라 다르게 설정
                if (selectedCharacter == R.drawable.sleepy_kkamppag) {
                    resetTime = 10000; // sleepy_kkamppag는 30초 후 원래 이미지로 돌아감
                } else if (selectedCharacter == R.drawable.strange_kkamppag) {
                    resetTime = 10000; // strange_kkamppag는 25초 후 원래 이미지로 돌아감
                }



                idleHandler.postDelayed(() -> {

                    Glide.with(context)
                            .asGif()
                            .load(R.drawable.basic_kkamppag)  // 새로운 gif 파일 (memo.gif)
                            .into(character);  // ImageView에 설정


                    resetIdleTimer();
                }, resetTime); // 초 후 원래 이미지로 복구
            }
        };
        resetIdleTimer();

        //드래그+꾹누르기
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                resetIdleTimer();   // 터치 시 타이머 초기화
                if(isNormalMode){  //녹음
                    voiceToText.startListening();
                    return true;
                }
                else{ //녹음,드래그 버튼
                    showStudyButton(params.x, params.y);
                    return true;
                }

            }


            @Override
            public void onLongPress(MotionEvent e) {
                resetIdleTimer();   // 터치 시 타이머 초기화
                if(isNormalMode) {  //공부모드,종료 바튼
                    showNomalButton(params.x, params.y);
                }else{  //알반모드 버튼
                    showExitButton(params.x, params.y);
                }
            }

        });

        character.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event); // GestureDetector에 이벤트 전달

            resetIdleTimer();
            return true;
        });

        character.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private float touchX, touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event); // 롱프레스 감지

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        touchX = event.getRawX();
                        touchY = event.getRawY();

                        float deltaX = touchX - initialX;
                        float deltaY = touchY - initialY;

                        params.x += deltaX;
                        params.y += deltaY;

                        windowManager.updateViewLayout(overlayView, params);

                        // 버튼 위치 업데이트
                        updateButtonPosition(params.x, params.y);

                        initialX = touchX;
                        initialY = touchY;
                        break;
                }
                return true;
            }
        });



        // 오버레이를 화면에 추가
        windowManager.addView(overlayView, params);

        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                if (overlayView == null) {
                    windowManager.addView(overlayView, params);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                // 앱이 포그라운드에서 벗어났을 때 뷰를 제거하지 않음
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }


    public void removeOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }



    //공부모드
    public void showNomalButton(int characterX, int characterY) {
        if (studyButton == null) {
            studyButton = new Button(context);
            studyButton.setText("공부모드");

            // 배경을 말풍선 모양으로 설정
            studyButton.setBackgroundDrawable(
                    new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1), 200, 150, false
                    ))
            );


            WindowManager.LayoutParams buttonLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            // 버튼 위치를 캐릭터 바로 위로 설정
            buttonLayoutParams.x = characterX - 180;  // 캐릭터의 x 좌표
            buttonLayoutParams.y = characterY - 150; // 캐릭터의 y 좌표에서 약간 위로 이동
            //buttonLayoutParams.gravity = Gravity.TOP | Gravity.START; // 절대 좌표를 기준으로 설정

            // 버튼을 오버레이로 띄우기
            windowManager.addView(studyButton, buttonLayoutParams);
        }
        // 버튼 클릭 이벤트 처리
        studyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Overlay", "공부 모드 버튼 클릭됨");
                // 버튼 클릭 시 원하는 동작 수행
                // 공부 모드로 전환
                isNormalMode = false;

                ImageView characterImage = overlayView.findViewById(R.id.character_image);

                // Glide로 새로운 GIF 로드 (memo.gif)
                Glide.with(context)
                        .asGif()
                        .load(R.drawable.memo_start)  // 새로운 gif 파일 (memo.gif)
                        .into(characterImage);  // ImageView에 설정

                // 일정 시간 후 memo.gif로 변경 (예: 2초 후)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(context)
                                .asGif()
                                .load(R.drawable.memo_kkamppag)  // 2초 후 memo.gif로 변경
                                .into(characterImage);
                    }
                }, 1500);  // 2초 (2000ms) 후에 실행


                studyButton.setVisibility(View.GONE);
                endButton.setVisibility(View.GONE);

                mode.setModel("gpt-4"); // 모드 변경
                Log.d("Overlay","gpt-4로 변경");

            }
        });


        //종료
        if (endButton == null) {
            endButton = new Button(context);
            endButton.setText("종료");

            // 배경을 말풍선 모양으로 설정
            endButton.setBackgroundDrawable(
                    new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud2), 200, 150, false
                    ))
            );

            WindowManager.LayoutParams endButtonParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            endButtonParams.x = characterX + 180;  // 캐릭터의 x 좌표
            endButtonParams.y = characterY - 150; // 캐릭터의 y 좌표에서 약간 위로 이동

            windowManager.addView(endButton, endButtonParams);
        }

        endButton.setOnClickListener(v -> {
            Log.d("Overlay", "종료버튼 클릭됨");
            if (context instanceof Service) {
                Service service = (Service) context;
                service.stopSelf();
            } else {
                Intent stopIntent = new Intent(context, OverlayService.class);
                context.stopService(stopIntent);
            }

        });





        // 버튼이 이미 생성된 경우 가시성 토글
        if (studyButton.getVisibility() == View.VISIBLE) {
            // 버튼을 숨김
            studyButton.setVisibility(View.GONE);
            endButton.setVisibility(View.GONE);
        } else {
            // 버튼을 보이게 함
            studyButton.setVisibility(View.VISIBLE);
            endButton.setVisibility(View.VISIBLE);
        }


    }

    // "공부하기" 버튼 제거
    public void removeStudyButton() {
        if (studyButton != null) {
            windowManager.removeView(studyButton);
            studyButton = null; // 버튼을 제거한 후 null로 설정
        }
    }



    // 녹음 시작 함수
    private void startRecording() {
        // 녹음 시작/종료 로직
        Toast.makeText(context, "녹음 시작", Toast.LENGTH_SHORT).show();
    }



    public void showStudyButton(int characterX, int characterY) {
        if (recordButton == null) {
            recordButton = new Button(context);
            recordButton.setText("녹음");

            // 배경을 말풍선 모양으로 설정
            recordButton.setBackgroundDrawable(
                    new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1), 200, 150, false
                    ))
            );


            WindowManager.LayoutParams recordButtonParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            // 버튼 위치를 캐릭터 바로 위로 설정
            recordButtonParams.x = characterX - 180;  // 캐릭터의 x 좌표
            recordButtonParams.y = characterY - 150; // 캐릭터의 y 좌표에서 약간 위로 이동

            // 버튼을 오버레이로 띄우기
            windowManager.addView(recordButton, recordButtonParams);
        }

        // 버튼 클릭 이벤트 처리
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Overlay", "녹음 버튼 클릭됨");
                recordButton.setVisibility(View.GONE);
                dragButton.setVisibility(View.GONE);
                voiceToText.startListening();


            }
        });



        //드래그
        if (dragButton == null) {
            dragButton = new Button(context);
            dragButton.setText("드래그");

            // 배경을 말풍선 모양으로 설정
            dragButton.setBackgroundDrawable(
                    new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud2), 200, 150, false
                    ))
            );

            WindowManager.LayoutParams dragButtonParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            dragButtonParams.x = characterX + 180;  // 캐릭터의 x 좌표
            dragButtonParams.y = characterY - 150; // 캐릭터의 y 좌표에서 약간 위로 이동

            windowManager.addView(dragButton, dragButtonParams);
        }

        dragButton.setOnClickListener(v -> {
            Log.d("Overlay", "드래그 버튼 클릭됨");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Toast.makeText(context, "복사를 하도록 하게.", Toast.LENGTH_SHORT).show();
            });

            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowManager.updateViewLayout(overlayView, params);

            overlayView.requestFocus();
            Log.d("Overlay", "FLAG_NOT_FOCUSABLE 제거됨"+ params.flags);

            recordButton.setVisibility(View.GONE);
            dragButton.setVisibility(View.GONE);
        });

        // 버튼이 이미 생성된 경우 가시성 토글
        if (recordButton.getVisibility() == View.VISIBLE) {
            // 버튼을 숨김
            recordButton.setVisibility(View.GONE);
            dragButton.setVisibility(View.GONE);
        } else {
            // 버튼을 보이게 함
            recordButton.setVisibility(View.VISIBLE);
            dragButton.setVisibility(View.VISIBLE);
        }
    }


    public void showExitButton(int characterX, int characterY) {
        if (nomalButton == null) {
            nomalButton = new Button(context);
            nomalButton.setText("일반모드");

            // 배경을 말풍선 모양으로 설정
            nomalButton.setBackgroundDrawable(
                    new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1), 200, 150, false
                    ))
            );

            WindowManager.LayoutParams nomalButtonParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            nomalButtonParams.x = characterX - 180;  // 캐릭터의 x 좌표
            nomalButtonParams.y = characterY - 150; // 캐릭터의 y 좌표에서 약간 위로 이동

            windowManager.addView(nomalButton, nomalButtonParams);
        }

            nomalButton.setOnClickListener(v -> {
                isNormalMode = true;

                ImageView characterImage = overlayView.findViewById(R.id.character_image);

                // Glide로 새로운 GIF 로드 (memo.gif)
                Glide.with(context)
                        .asGif()
                        .load(R.drawable.basic_kkamppag)  // 새로운 gif 파일 (memo.gif)
                        .into(characterImage);  // ImageView에 설정

                mode.setModel("gpt-3.5-turbo");
                Log.d("Overlay","gpt-3.5-turbo로 변경");

                nomalButton.setVisibility(View.GONE);
            });

        // 드래그 버튼이 이미 있을 경우 가시성 토글
        if (nomalButton.getVisibility() == View.VISIBLE) {
            nomalButton.setVisibility(View.GONE);
        } else {
            nomalButton.setVisibility(View.VISIBLE);
        }


    }



    private void updateButtonPosition(int characterX, int characterY) {
        if (studyButton != null) {
            WindowManager.LayoutParams buttonLayoutParams = (WindowManager.LayoutParams) studyButton.getLayoutParams();

            // 버튼 위치 업데이트
            buttonLayoutParams.x = characterX-180;
            buttonLayoutParams.y = characterY - 150;

            windowManager.updateViewLayout(studyButton, buttonLayoutParams);
        }

        if (endButton != null) {
            WindowManager.LayoutParams endButtonParams = (WindowManager.LayoutParams) endButton.getLayoutParams();
            endButtonParams.x = characterX + 180; // 캐릭터 오른쪽에 위치
            endButtonParams.y = characterY - 150;
            windowManager.updateViewLayout(endButton, endButtonParams);
        }

        if (recordButton != null) {
            WindowManager.LayoutParams recordButtonParams = (WindowManager.LayoutParams) recordButton.getLayoutParams();

            // 버튼 위치 업데이트
            recordButtonParams.x = characterX-180;
            recordButtonParams.y = characterY - 150;

            windowManager.updateViewLayout(recordButton, recordButtonParams);
        }

        if (dragButton != null) {
            WindowManager.LayoutParams dragButtonParams = (WindowManager.LayoutParams) dragButton.getLayoutParams();
            dragButtonParams.x = characterX + 180; // 캐릭터 오른쪽에 위치
            dragButtonParams.y = characterY - 150;
            windowManager.updateViewLayout(dragButton, dragButtonParams);
        }

        if (nomalButton != null) {
            WindowManager.LayoutParams nomalButtonParams = (WindowManager.LayoutParams) nomalButton.getLayoutParams();
            nomalButtonParams.x = characterX - 180; // 캐릭터 오른쪽에 위치
            nomalButtonParams.y = characterY - 150;
            windowManager.updateViewLayout(nomalButton, nomalButtonParams);
        }
    }


    // 타이머 초기화
    private void resetIdleTimer() {
        idleHandler.removeCallbacks(idleRunnable);
        // IDLE_TIME 후에 동작 실행
        idleHandler.postDelayed(idleRunnable, IDLE_TIME);

    }

    //@Override
    public void destroy() {
        //super.onDestroy();
        idleHandler.removeCallbacks(idleRunnable);
        windowManager.removeView(overlayView);
        voiceToText.destroy();
    }
}
