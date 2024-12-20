package com.example.oeg.mode;

import android.util.Log;

import com.example.oeg.Etc.MessageParser;
import com.example.oeg.model.GptRequest;
import com.example.oeg.network.ChatGPTClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mode{
    private static Mode instance; //싱글톤
    private String model = "gpt-3.5-turbo"; // 기본 모델
    private String message;
    private ChatGPTClient chatGPTClient = new ChatGPTClient();

    private GptRequest request;
    private GptRequest.Message assistantMessage;

    private List<ModeListener> listeners = new ArrayList<>();

    private Mode() { }

    public static synchronized Mode getInstance() { // 싱글톤
        if (instance == null) {
            instance = new Mode();
        }
        return instance;
    }

    public void addListener(ModeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ModeListener listener) {
        listeners.remove(listener);
    }

    private void notifyNewReply(MessageParser.ParsedMessage parsedMessage) {
        for (ModeListener listener : listeners) {
            listener.onNewReply(parsedMessage);
        }
    }

    private void notifyError(String error) {
        for (ModeListener listener : listeners) {
            listener.onError(error);
        }
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel(){
        return model;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void sendMessage() {
        // 시스템 메시지 정의
        GptRequest.Message systemMessage;

        if (model.equals("gpt-3.5-turbo")) {
            systemMessage = new GptRequest.Message(
                    "system",
                    "당신은 비관적인 말투의 귀족 출신 외계인이며 하게체(말투)를 사용합니다. 철학적인 말을 즐겨합니다. 당신의 이름은 '왱제르 레그자 웨이저'입니다. 아주 짧게 답변해 주세요." +
                            "하게체 --> 평서법: ~네, ~(ㄴ/는)다네, ~(이)라네, ~(으)ㄹ세[1], ~(으/느)니, ~(으)이, ~(으)ㄹ레" +
                            "명령법: ~게, ~게나" +
                            "의문법: ~나, ~(으/느)ㄴ가, ~(으/느)ㄴ감, ~(으)ㄹ런가, ~(으)ㄹ쏜가" +
                            "청유법: ~(으)세, ~(으)세나" +
                            "약속법: ~(으)ㅁ세" +
                            "감탄법: ~(으)ㄹ세, ~(이)로세" +
                            "추측·의도법: ~(으)ㄹ세"
            );
        } else if (model.equals("gpt-4")) {
            systemMessage = new GptRequest.Message(
                    "system",
                    "당신은 모든 질문에 대해 명확하게 명사형 종결어미(~함., ~임.등)를 사용하여 대답하는 인공지능임. 질문이 어려울 시 길게 설명해도 좋음."
            );
        } else {
            notifyError("유효하지 않은 모델");
            return;
        }

        // 요청 생성
        if (assistantMessage != null) {
            request = new GptRequest(model, Arrays.asList(systemMessage, assistantMessage, new GptRequest.Message("user", message)));
        } else {
            request = new GptRequest(model, Arrays.asList(systemMessage, new GptRequest.Message("user", message)));
        }

        // API 호출
        chatGPTClient.sendMessage(request, model, new ChatGPTClient.ChatGPTResponseListener() {
            @Override
            public void onResponse(MessageParser.ParsedMessage parsedMessage) {
                assistantMessage = new GptRequest.Message("assistant", parsedMessage.textContent);
                notifyNewReply(parsedMessage);
                Log.d("Mode", "parsedMessage 받음: " + parsedMessage.textContent);
            }

            @Override
            public void onFailure(String error) {
                notifyError(error);
            }
        });
    }

    public interface ModeListener {
        void onNewReply(MessageParser.ParsedMessage parsedMessage);
        void onError(String error);
    }
}
