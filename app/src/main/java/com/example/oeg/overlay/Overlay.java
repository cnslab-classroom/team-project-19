package com.example.oeg.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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


public class Overlay {
    private final Context context;
    private final WindowManager windowManager;
    private View overlayView;
    private Button studyButton;
    private Button endButton;
    //private final ServiceStopCallback stopServiceCallback; // 서비스 종료 콜백

    public Overlay(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }


    public void showOverlay() {
        if (overlayView != null) return;

        // WindowManager 초기화
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // WindowManager 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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



        // Glide로 GIF 이미지 로드
        Glide.with(context)
                .asGif()  // GIF 형식으로 로드
                .load(R.drawable.strange_kkamppag)  // GIF 파일을 로드
                .into(character);  // ImageView에 표시





        //드래그+꾹누르기
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                // 짧게 누르면 버튼들 보이게 하기
                startRecording();
                return true;
            }


            @Override
            public void onLongPress(MotionEvent e) {
                showStudyButton(params.x, params.y);
                //showEndButton(params.x, params.y);
            }
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
    public void showStudyButton(int characterX, int characterY) {
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

            //buttonLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            //buttonLayoutParams.y = 200;

            // 버튼 위치를 캐릭터 바로 위로 설정
            buttonLayoutParams.x = characterX - 200;  // 캐릭터의 x 좌표
            buttonLayoutParams.y = characterY - 200; // 캐릭터의 y 좌표에서 약간 위로 이동
            //buttonLayoutParams.gravity = Gravity.TOP | Gravity.START; // 절대 좌표를 기준으로 설정

            // 버튼을 오버레이로 띄우기
            windowManager.addView(studyButton, buttonLayoutParams);

            // 버튼 클릭 이벤트 처리
            studyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 버튼 클릭 시 원하는 동작 수행 (예: 공부 화면으로 이동)
                    // 예: Intent로 화면 전환
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

                    // StudyMode 액티비티로 이동
                    //Intent intent = new Intent(context, StudyMode.class);
                    //context.startActivity(intent);
                }
            });
        }

        //종료
        if (endButton == null) {
            endButton = new Button(context);
            endButton.setText("종료");

            // 배경을 말풍선 모양으로 설정
            endButton.setBackgroundDrawable(
                    new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1), 200, 150, false
                    ))
            );

            WindowManager.LayoutParams endButtonParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            endButtonParams.x = characterX + 200;  // 캐릭터의 x 좌표
            endButtonParams.y = characterY - 200; // 캐릭터의 y 좌표에서 약간 위로 이동

            windowManager.addView(endButton, endButtonParams);


            endButton.setOnClickListener(v -> {
                //stopServiceCallback.stopService();
                removeOverlay();
            });


        }
    }


    private void updateButtonPosition(int characterX, int characterY) {
        if (studyButton != null) {
            WindowManager.LayoutParams buttonLayoutParams = (WindowManager.LayoutParams) studyButton.getLayoutParams();

            // 버튼 위치 업데이트
            buttonLayoutParams.x = characterX-200;
            buttonLayoutParams.y = characterY - 200;

            windowManager.updateViewLayout(studyButton, buttonLayoutParams);
        }

        if (endButton != null) {
            WindowManager.LayoutParams endButtonParams = (WindowManager.LayoutParams) endButton.getLayoutParams();
            endButtonParams.x = characterX + 200; // 캐릭터 오른쪽에 위치
            endButtonParams.y = characterY - 200;
            windowManager.updateViewLayout(endButton, endButtonParams);
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


}
