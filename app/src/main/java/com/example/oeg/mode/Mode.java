package com.example.oeg.mode;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oeg.Etc.MessageParser;
import com.example.oeg.model.GptRequest;
import com.example.oeg.network.ChatGPTClient;

import java.util.Arrays;
import java.util.List;


public class Mode extends ViewModel { //model set 해야함 밖에서
    private String model = "gpt-3.5-turbo"; //혹은 gpt-4
    private String message;
    private ChatGPTClient chatGPTClient = new ChatGPTClient();
    private MutableLiveData<MessageParser.ParsedMessage> ReplyLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private GptRequest request;

    private GptRequest.Message assistantMessage;

    public LiveData<MessageParser.ParsedMessage> getNewReplyLiveData() {
        return ReplyLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void sendMessage() {
        if(model.equals("gpt-3.5-turbo")){
            GptRequest.Message systemMessage = new GptRequest.Message(
                    "system",
                    "당신은 비관적인 말투의 귀족 출신 외계인이며 하게체(말투)를 사용합니다. 철학적인 말을 즐겨합니다. 아주 짧게 답변해 주세요." +
                            "하게체 --> 평서법: ~네, ~(ㄴ/는)다네, ~(이)라네, ~(으)ㄹ세[1], ~(으/느)니, ~(으)이, ~(으)ㄹ레" +
                            "명령법: ~게, ~게나" +
                            "의문법: ~나, ~(으/느)ㄴ가, ~(으/느)ㄴ감, ~(으)ㄹ런가, ~(으)ㄹ쏜가" +
                            "청유법: ~(으)세, ~(으)세나" +
                            "약속법: ~(으)ㅁ세" +
                            "감탄법: ~(으)ㄹ세, ~(이)로세" +
                            "추측·의도법: ~(으)ㄹ세"
            );
            if (assistantMessage != null) {
                request = new GptRequest(model, Arrays.asList(systemMessage, assistantMessage, new GptRequest.Message("user", message)));
            }else{
                request = new GptRequest(model, Arrays.asList(systemMessage, new GptRequest.Message("user", message)));
            }

        }else if(model.equals("gpt-4")){
            GptRequest.Message systemMessage = new GptRequest.Message(
                    "system",
                    "당신은 모든 질문에 대해 명확하게 명사형 종결어미(~함., ~임.등)를 사용하여 대답하는 인공지능임. 질문이 어려울 시 길게 설명해도 좋음."
            );
            if (assistantMessage != null) {
                request = new GptRequest(model, Arrays.asList(systemMessage, assistantMessage, new GptRequest.Message("user", message)));
            }else{
                request = new GptRequest(model, Arrays.asList(systemMessage, new GptRequest.Message("user", message)));
            }

        }else {
            errorLiveData.postValue("유효하지 않은 모델");
            return;
        }


        chatGPTClient.sendMessage(request, model, new ChatGPTClient.ChatGPTResponseListener() {
            @Override
            public void onResponse(MessageParser.ParsedMessage parsedmessage) {
                assistantMessage = new GptRequest.Message("assistant", parsedmessage.textContent);
                ReplyLiveData.postValue(parsedmessage);
                Log.d("Mode", "parsedmessage 받음" + parsedmessage.textContent);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.postValue(error);
            }
        });
    }

}
