package com.example.oeg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        VideoView videoView = findViewById(R.id.introVideoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro);
        videoView.setVideoURI(videoUri);

        videoView.setOnCompletionListener(mediaPlayer -> {
            // 비디오 재생 완료 후 isFirstRun을 false로 설정하고 메인 액티비티로 이동
            SharedPreferences prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isFirstRun", false).apply();

            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        videoView.start();
    }
}

