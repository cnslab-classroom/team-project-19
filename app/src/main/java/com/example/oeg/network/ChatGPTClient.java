package com.example.oeg.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.example.oeg.Etc.MessageParser;
import com.example.oeg.model.GptRequest;
import com.example.oeg.model.GptResponse;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.oeg.BuildConfig;
public class ChatGPTClient {
    private OkHttpClient client;
    private Gson gson; // 통신할 때 사용하는 데이터 형식

    public ChatGPTClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    // gpt에 요청 보내는 함수
    public void sendMessage(GptRequest request, String model, ChatGPTResponseListener listener) {

        String json = gson.toJson(request); // gpt에 보내기 위해 GptRequest --> JSON 형식으로 변환
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        //변환한 내용을 HTTP 요청 body로 사용

        Request httpRequest = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                // openai로 요청 보냄
                .addHeader("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                // 숨겨놓은 api를 헤더에 추가
                .post(body)
                // HTTP 요청을 POST로 설정, body 내용 넣기
                .build();


        //비동기적으로 요청 보내기 (결과를 계속 기다리지 않고, 다른거 할 수 있음)
        //Callback은 요청이 성공 또는 실패했을 때 호출되는 함수 정의
        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    GptResponse openAIResponse = gson.fromJson(responseBody, GptResponse.class);
                    String reply = openAIResponse.getChoices().get(0).getMessage().getContent();
                    MessageParser.ParsedMessage parsedMessage = MessageParser.parseResponse(reply);
                    listener.onResponse(parsedMessage);
                    // 응답 성공하면 파싱해서 리스너에게 전달 parsedMessage는 코드랑 텍스트 나눠서 저장되어있음
                } else {
                    // 오류 처리
                    String errorBody = response.body().string();
                    System.err.println("Response error: " + response.code() + " " + response.message());
                    System.err.println("Error body: " + errorBody);
                    listener.onFailure("Error: " + response.code() + " " + response.message());
                }
            }
        });
    }

    public interface ChatGPTResponseListener {
        void onResponse(MessageParser.ParsedMessage parsedMessage);
        void onFailure(String error);
    }
}
