package com.example.oeg;
// 예시~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.example.oeg.Etc.VoiceToText;
import com.example.oeg.Etc.MessageParser;
import com.example.oeg.mode.Mode;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    // onCreate 밖
    private Mode study_model;
    private VoiceToText voiceToText;

    private Button startListeningButton;
    private Button stopListeningButton;

    private LinearLayout answerListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 파일 설정

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
        startListeningButton = findViewById(R.id.startListeningButton);
        stopListeningButton = findViewById(R.id.stopListeningButton);

        // 클릭 이벤트 설정
        startListeningButton.setOnClickListener(v -> {
            // 권한 체크 및 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                voiceToText.startListening(); // 녹음 시작
            }
        }); // 녹음 중지 함수도 있음 VoiceToText 클래스 참고

        // 답변 리스트 컨테이너 초기화
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
}


/*
채은이 OverlayService 클래스에
 버튼 이름.setOnClickListener(v -> {
            Intent intent = new Intent(OverlayService.this, MainActivity.class);
            intent.putExtra("selectedText", MyAccessibilityService.selectedText);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

메인 액티비티에
  Intent intent = getIntent();
  String dragMessage = intent.getStringExtra("selectedText");

*/

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
