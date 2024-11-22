package com.example.oeg.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.OutcomeReceiver;
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
import android.content.Intent;
import com.example.oeg.MainActivity;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

public class Overlay {
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
    private boolean isTouching = false;

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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
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

        //idleRunnable = () -> {
/*
        idleRunnable = new Runnable() {
            @Override
            public void run() {
                //resetIdleTimer();
                // 캐릭터 그림 변경
                if (!isTouching) {
                    int[] characterImages = {
                            R.drawable.ramen,         // 첫 번째 이미지
                            //R.drawable.sleepy_kkamppag,       // 두 번째 이미지
                            //R.drawable.strange_kkamppag      // 세 번째 이미지
                    };

                    // 랜덤으로 캐릭터 선택
                    int randomIndex = new Random().nextInt(characterImages.length); // 0부터 배열 길이-1 사이의 값
                    int selectedCharacter = characterImages[randomIndex];

                    Glide.with(context)
                            .asGif()
                            .load(selectedCharacter)  // 새로운 gif 파일 (memo.gif)
                            .into(character);  // ImageView에 설정

                    //character.setImageResource(selectedCharacter);

                    idleHandler.postDelayed(() -> {

                        Glide.with(context)
                                .asGif()
                                .load(R.drawable.basic_kkamppag)  // 새로운 gif 파일 (memo.gif)
                                .into(character);  // ImageView에 설정

                        //character.setImageResource(R.drawable.basic_kkamppag);
                        resetIdleTimer();
                    }, 20000); // 5초 후 원래 이미지로 복구
                }

                // 추가 동작 구현 가능

            }
            // 초기 타이머 설정
            //resetIdleTimer();
        };

        // 타이머 동작 정의
        //setIdleRunnable();  // idleRunnable 설정
        resetIdleTimer();

*/


        //드래그+꾹누르기
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isNormalMode){
                    // 짧게 누르면 버튼들 보이게 하기
                    //Log.d("GestureDetector", "isNormalMode before: " + isNormalMode);
                    startRecording();
                    return true;
                }
                else{
                    //Log.d("GestureDetector", "isNormalMode after: " + isNormalMode);
                    showStudyButton(params.x, params.y);
                    //showStudyButton((int) e.getRawX(), (int) e.getRawY());
                    return true;
                }

            }


            @Override
            public void onLongPress(MotionEvent e) {
                if(isNormalMode) {
                    showNomalButton(params.x, params.y);
                }else{
                    showExitButton(params.x, params.y);
                }
            }
        });

        character.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event); // GestureDetector에 이벤트 전달

            /*if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isTouching = true;  // 터치 시작
                resetIdleTimer();   // 터치 시 타이머 초기화
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                isTouching = false; // 터치 종료
                resetIdleTimer();
            }*/

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

        //initializeCharacter(overlayView);

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

            // 버튼 클릭 이벤트 처리
            studyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 버튼 클릭 시 원하는 동작 수행 (예: 공부 화면으로 이동)
                    // 예: Intent로 화면 전환
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



                    //Intent intent = new Intent(context, MainActivity.class);
                    //intent.putExtra("action", "start_study_mode");  // 동작 전달
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


            endButton.setOnClickListener(v -> {
                //종료
                System.exit(0);
            });


        }
        else {
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


            // 버튼 클릭 이벤트 처리
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    recordButton.setVisibility(View.GONE);
                    dragButton.setVisibility(View.GONE);

                    //Intent intent = new Intent(context, MainActivity.class);
                    //intent.putExtra("action", "start_study_mode");  // 동작 전달
                    //context.startActivity(intent);
                    // 녹음 버튼 클릭 시
                    /*
                    if (recordButton.getVisibility() == View.VISIBLE) {
                        // 버튼을 숨김
                        recordButton.setVisibility(View.GONE);
                        dragButton.setVisibility(View.GONE);
                    } else {
                        // 버튼을 다시 보이게 함
                        recordButton.setVisibility(View.VISIBLE);
                        dragButton.setVisibility(View.VISIBLE);
                    }*/

                }
            });
        }

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


            dragButton.setOnClickListener(v -> {
                recordButton.setVisibility(View.GONE);
                dragButton.setVisibility(View.GONE);
                // 녹음 버튼 클릭 시

            });


        }

        else {
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


            nomalButton.setOnClickListener(v -> {
                isNormalMode = true;

                ImageView characterImage = overlayView.findViewById(R.id.character_image);

                // Glide로 새로운 GIF 로드 (memo.gif)
                Glide.with(context)
                        .asGif()
                        .load(R.drawable.basic_kkamppag)  // 새로운 gif 파일 (memo.gif)
                        .into(characterImage);  // ImageView에 설정

                nomalButton.setVisibility(View.GONE);
            });

        }else {
            // 드래그 버튼이 이미 있을 경우 가시성 토글
            if (nomalButton.getVisibility() == View.VISIBLE) {
                nomalButton.setVisibility(View.GONE);
            } else {
                nomalButton.setVisibility(View.VISIBLE);
            }
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

/*
    // 캐릭터 초기화
    public void initializeCharacter(View overlayView)  {
        ImageView character = overlayView.findViewById(R.id.character_image);

        // 캐릭터 클릭 리스너
        character.setOnTouchListener((v, event) -> {
            resetIdleTimer(); // 터치 시 타이머 초기화
            return false; // 다른 터치 이벤트도 처리되도록 반환
        });

        // 타이머 동작 정의
        idleRunnable = () -> {
            // 캐릭터 그림 변경
            Glide.with(context)
                    .asGif()
                    .load(R.drawable.ramen)  // 새로운 gif 파일 (memo.gif)
                    .into(character);  // ImageView에 설정

            character.setImageResource(R.drawable.ramen);

            idleHandler.postDelayed(() -> {
                character.setImageResource(R.drawable.basic_kkamppag);
            }, 20000); // 5초 후 원래 이미지로 복구
            // 추가 동작 구현 가능
        };

        // 초기 타이머 설정
        resetIdleTimer();
    }

 */
/*
    // 타이머 초기화
    private void resetIdleTimer() {

        if (!isTouching) {
            // 기존 핸들러 작업 제거
            idleHandler.removeCallbacks(idleRunnable);
            // IDLE_TIME 후에 동작 실행
            idleHandler.postDelayed(idleRunnable, IDLE_TIME);
        }else {
            // 터치 중이라면 타이머를 리셋
            idleHandler.removeCallbacks(idleRunnable);
        }

    }

    // onDestroy()에서 핸들러 제거 (메모리 누수 방지)
    //@Override
    protected void destroy() {
        //super.onDestroy();
        idleHandler.removeCallbacks(idleRunnable);
    }
*/
/*
    private void setIdleRunnable(){
        idleRunnable = new Runnable() {
            @Override
            public void run() {
                //resetIdleTimer();
                // 캐릭터 그림 변경
                if(!isTouching){
                    int[] characterImages = {
                            R.drawable.ramen,         // 첫 번째 이미지
                            //R.drawable.sleepy_kkamppag,       // 두 번째 이미지
                            //R.drawable.strange_kkamppag      // 세 번째 이미지
                    };

                    // 랜덤으로 캐릭터 선택
                    int randomIndex = new Random().nextInt(characterImages.length); // 0부터 배열 길이-1 사이의 값
                    int selectedCharacter = characterImages[randomIndex];

                    Glide.with(context)
                            .asGif()
                            .load(selectedCharacter)  // 새로운 gif 파일 (memo.gif)
                            .into(character);  // ImageView에 설정

                    //character.setImageResource(selectedCharacter);

                    idleHandler.postDelayed(() -> {

                        Glide.with(context)
                                .asGif()
                                .load(R.drawable.basic_kkamppag)  // 새로운 gif 파일 (memo.gif)
                                .into(character);  // ImageView에 설정

                        //character.setImageResource(R.drawable.basic_kkamppag);
                        resetIdleTimer();
                    }, 20000); // 5초 후 원래 이미지로 복구
                    // 추가 동작 구현 가능
                }
                //resetIdleTimer();
            }
        };
    }*/
}
