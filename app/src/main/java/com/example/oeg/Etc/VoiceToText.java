package com.example.oeg.Etc;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import java.util.ArrayList;
import android.os.Bundle;

public class VoiceToText {
    private SpeechRecognizer speechRecognizer; //음성 인식에 쓸거
    private Intent intent; // 음성인식 설정 정보
    private SpeechToTextListener listener; // 음성인식 결과 호출한쪽에 보내는 콜백 인터페이스(음성인식 비동기라 필요(결과 언제나올지 모름))
    private Context context; // 음성 인식기의 실행 환경에 대한 정보를 제공하는 용도

    public VoiceToText(Context context, SpeechToTextListener listener) {
        this.context = context;
        this.listener = listener;
        initializeSpeechRecognizer(); // 음성인식기 초기화 함수
    }

    private void initializeSpeechRecognizer() { //초기화 함수
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context); // 음성인식 객체 생성
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener()); // 리스너 설정

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //음성 인식기 동작 설정
        intent.putExtra( // 추가 설정
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 한국어 설정
    }

    public void startListening() {
        speechRecognizer.startListening(intent); // 음성 인식 시작 메서드
    }

    public void stopListening() {
        speechRecognizer.stopListening(); //음성 인식 중지
    }

    public void destroy() { // 음성 인식 안쓸거면 해제(꼭..!)
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            // 준비 완료
        }

        @Override
        public void onBeginningOfSpeech() {
            // 말하기 시작
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // 음성 크기 변경
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // 음성 데이터 수신
        }

        @Override
        public void onEndOfSpeech() {
            // 말하기 종료
        }

        @Override
        public void onError(int error) {
            String errorMessage = getErrorText(error);
            listener.onSpeechError(errorMessage);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String result = matches.get(0);
                listener.onSpeechResult(result);
            } else {
                listener.onSpeechError("인식 안됨");
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // 부분 결과
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // 이벤트 발생
        }

        private String getErrorText(int errorCode) {
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    return "오디오 에러";
                case SpeechRecognizer.ERROR_CLIENT:
                    return "클라이언트 에러";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    return "권한 없음";
                case SpeechRecognizer.ERROR_NETWORK:
                    return "네트워크 에러";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    return "네트워크 타임아웃";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    return "일치하는 결과 없음";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    return "인식기가 바쁨";
                case SpeechRecognizer.ERROR_SERVER:
                    return "서버 에러";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    return "말하기 시간 초과";
                default:
                    return "알 수 없는 오류";
            }
        }
    }

    // SpeechToTextListener.java
    public interface SpeechToTextListener {
        void onSpeechResult(String result);
        void onSpeechError(String errorMessage);
    }

}
