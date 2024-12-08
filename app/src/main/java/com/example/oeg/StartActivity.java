package com.example.oeg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class StartActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 버튼 찾아오기
        Button button = findViewById(R.id.start_button);

        // 버튼 클릭 시 MainActivity로 이동
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity로 이동
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // StartActivity 종료
            }
        });
    }
}
