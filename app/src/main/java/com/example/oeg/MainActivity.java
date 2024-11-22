package com.example.oeg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.oeg.Etc.VoiceToText;
import com.example.oeg.mode.Mode;
import com.example.oeg.overlay.Overlay;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Handler;
//import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;


import com.example.oeg.overlay.OverlayService;


public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    private Overlay overlay;


    private Mode mode;
    private VoiceToText voiceToText;

    private LinearLayout answerListContainer;


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 모드 변경 하는거
        if (intent != null && "start_study_mode".equals(intent.getStringExtra("action"))) {
            mode.setModel("gpt-4");
        }
        else if (intent != null && "exit_study_mode".equals(intent.getStringExtra("action"))){
            mode.setModel("gpt-3.5-turbo");
        }
        // 드래그 처리
        else if (intent != null && intent.hasExtra("드래그함")) {
            String selectedText = intent.getStringExtra("드래그함");
            mode.setMessage(selectedText); // GPT에게 보낼 메시지 설정
            mode.sendMessage(); // 메시지 전송

        }
        // 녹음 처리
        else if(intent != null && intent.hasExtra("대충 녹음 버튼 눌렀다는 intent 받으면")){
            // 권한 체크 및 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                voiceToText.startListening(); // 녹음 시작
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = new ViewModelProvider(this).get(Mode.class);


        voiceToText = new VoiceToText(this, new VoiceToText.SpeechToTextListener() {
            @Override
            public void onSpeechResult(String result) {
                mode.setMessage(result); // GPT에게 보낼 메시지 설정
                mode.sendMessage(); // 메시지 전송
            }

            @Override
            public void onSpeechError(String errorMessage) {
                Toast.makeText(MainActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        //지은이 메모장 설정
        /* 대충 이거 비슷한거 있으면 됨
        answerListContainer = findViewById(R.id.answerListContainer);
        // LiveData 관찰자 설정
        study_model.getNewReplyLiveData().observe(this, new Observer<MessageParser.ParsedMessage>() {
            @Override
            public void onChanged(MessageParser.ParsedMessage parsedMessage) {
                // 새로운 답변 뷰 생성
                createAnswerView(parsedMessage);
            }
        });
        study_model.getErrorLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                Toast.makeText(MainActivity.this, "오류 발생: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

        */
        ///////////////////////////////////////////////////////////
        if (Settings.canDrawOverlays(MainActivity.this)) {
            //overlay.showOverlay(); // 오버레이 표시
            Intent serviceIntent = new Intent(MainActivity.this, OverlayService.class);
            startService(serviceIntent);


        } else {
            // 권한 요청
            requestOverlayPermission();
        }




    }
    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceToText.startListening();
            } else {
                Toast.makeText(this, "오디오 녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 앱 종료 시 음성 인식 객체 해제
        voiceToText.destroy();
    }




    /*
그 공부모드 버튼있는 클래스에 공부 모드에서 드래그 버튼 클릭하면 실행되게
 버튼 이름.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 접근성 서비스의 정적 메서드를 통해 텍스트 가져오기
        String draggedText = MyAccessibilityService.getCurrentSelectedText();

        Intent intent = new Intent(OverlayActivity.this, MainActivity.class);
        intent.putExtra("드래그함", draggedText);
        startActivity(intent);
    }

    if (intent != null && intent.hasExtra("드래그함")) {
            String selectedText = intent.getStringExtra("드래그함");

    }
*/



/*
        // ViewModel 초기화
        study_model = new ViewModelProvider(this).get(Mode.class);

        // 음성 인식 객체 생성
        voiceToText = new VoiceToText(this, new VoiceToText.SpeechToTextListener() {
            @Override
            public void onSpeechResult(String result) {
                study_model.setMessage(result); // GPT에게 보낼 메시지 설정
                study_model.sendMessage(); // 메시지 전송
            }

            @Override
            public void onSpeechError(String errorMessage) {
                Toast.makeText(MainActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 버튼 초기화
        //startListeningButton = findViewById(R.id.startListeningButton);
        //stopListeningButton = findViewById(R.id.stopListeningButton);

        // 클릭 이벤트 설정
        startListeningButton.setOnClickListener(v -> {
            // 권한 체크 및 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                voiceToText.startListening(); // 녹음 시작
            }
        });

        stopListeningButton.setOnClickListener(v -> { //주변이 조용해지면 자동으로 인식을 종료 하는데 굳이 필요한가?.. 음성인식 종료되고 한번 더 누르면 오류 뜸
            voiceToText.stopListening(); // 녹음 중지
        });

        // 답변 리스트 컨테이너 초기화
        //answerListContainer = findViewById(R.id.answerListContainer);

        // LiveData 관찰자 설정
        study_model.getNewReplyLiveData().observe(this, new Observer<MessageParser.ParsedMessage>() {
            @Override
            public void onChanged(MessageParser.ParsedMessage parsedMessage) {
                // 새로운 답변 뷰 생성
                createAnswerView(parsedMessage);
            }
        });

        study_model.getErrorLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                Toast.makeText(MainActivity.this, "오류 발생: " + error, Toast.LENGTH_LONG).show();
            }
        });

    }



    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceToText.startListening();
            } else {
                Toast.makeText(this, "오디오 녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 앱 종료 시 음성 인식 객체 해제
        voiceToText.destroy();

        overlay.removeOverlay();
    }

    private void createAnswerView(MessageParser.ParsedMessage parsedMessage) {
        // 뷰 생성 및 설정
        LayoutInflater inflater = LayoutInflater.from(this);
        View answerItemView = inflater.inflate(R.layout.item_answer, null);

        EditText answerEditText = answerItemView.findViewById(R.id.answerEditText);
        LinearLayout codeContainer = answerItemView.findViewById(R.id.codeContainer);
        Button deleteButton = answerItemView.findViewById(R.id.deleteButton);

        // 일반 텍스트 설정 (수정 가능)
        answerEditText.setText(parsedMessage.textContent);

        // 코드 블록 설정
        for (String code : parsedMessage.codeBlocks) {
            TextView codeTextView = new TextView(this);
            codeTextView.setText(code);
            codeTextView.setTypeface(Typeface.MONOSPACE);
            codeTextView.setTextSize(14);
            codeTextView.setPadding(8, 8, 8, 8);
            codeTextView.setBackgroundColor(Color.parseColor("#F0F0F0"));
            codeContainer.addView(codeTextView);
        }

        // 삭제 버튼 클릭 리스너 설정
        deleteButton.setOnClickListener(v -> {
            answerListContainer.removeView(answerItemView);
        });

        // 답변 뷰를 컨테이너에 추가
        answerListContainer.addView(answerItemView);
    }
*/


    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        //MainActivity.this.startActivity(intent);
    }

    // 권한 요청 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (Settings.canDrawOverlays(this)) {
                overlay.showOverlay(); // 권한이 승인되면 오버레이 표시
            } else {
                Toast.makeText(this, "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


/*
package com.example.oeg;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}*/
