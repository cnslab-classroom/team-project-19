package com.example.oeg.mode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oeg.Etc.MessageParser;
import com.example.oeg.model.GptRequest;
import com.example.oeg.network.ChatGPTClient;

import java.util.Arrays;


public class Mode extends ViewModel { //!!!!!!!!사용하려면 반드시 set함수로 message(지금은 아직 따로 받는거 안만듦)와 model 값 넣어야함!!!!!!!!!
    private String model = "gpt-3.5-turbo"; //혹은 gpt-4
    private String message;
    private ChatGPTClient chatGPTClient = new ChatGPTClient();
    private MutableLiveData<MessageParser.ParsedMessage> ReplyLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private GptRequest request;

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
                    "당신은 비관적인 말투의 귀족 출신 외계인입니다. 철학적인 말을 즐겨합니다. 아주 짧게 답변해 주세요."
            );
            GptRequest request = new GptRequest(model, Arrays.asList(systemMessage, new GptRequest.Message("user", message)));
        }else if(model.equals("gpt-4")){
            GptRequest.Message systemMessage = new GptRequest.Message(
                    "system",
                    "당신은 모든 질문에 대해 명확하고 주관식으로 대답하는 인공지능입니다. 질문이 어려운 경우 자세히 설명하세요. 개념, 사용 예, 그리고 관련된 맥락까지 포함하여 쉽게 풀어서 대답하세요. 종결어미는 사용하지 마세요."
            );
            GptRequest request = new GptRequest(model, Arrays.asList(systemMessage, new GptRequest.Message("user", message)));
        }else {
            errorLiveData.postValue("유효하지 않은 모델");
            return;
        }


        chatGPTClient.sendMessage(request, model, new ChatGPTClient.ChatGPTResponseListener() {
            @Override
            public void onResponse(MessageParser.ParsedMessage parsedmessage) {
                ReplyLiveData.postValue(parsedmessage);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.postValue(error);
            }
        });
    }

    //******추가해야하는거: 음성으로 메시지 받는 함수 message에 저장, 혹은 드래그
    // 버튼 이벤트로 녹음된거 받고.. voiceToMessage 인자로?? 되나? 가능하면 있는 라이브러리
    // voiceToMessage selectionToMessage
}
/*
!!!!!!!!!!!!!!!!!!!!!!!!!!!!메인 액티비티 사용예시!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//onCreate 밖
private Mode study_model;
private VoiceToText voiceToText;

    onCreate 안
    // ViewModel 초기화
    study_model = new ViewModelProvider(this).get(Mode.class);

    //음성 인식 객체 생성..
    voiceToText = new VoiceToText(this, new VoiceToText.SpeechToTextListener() {
        @Override
        public void onSpeechResult(String result) {
            study_model.setMessage(result); // gpt한테 message 보낼 내용 넣기
        }

        @Override
        public void onSpeechError(String errorMessage) {
            Toast.makeText(MainActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    });

    버튼 두개 만들어서 클릭이벤트 안에 넣기
        voiceToText.startListening();(녹음 시작할 때) voiceToText.stopListening();(녹음 끝냈을 때)

    앱 끄려고 하면 반드시 voiceToText.destroy();



    // LiveData 관찰자 설정
    modeViewModel.getNewReplyLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(MessageParser.ParsedMessage parsedmessage) {
            // 응답받은 텍스트랑 코드 블록으로 분리되어있음.... ---> 텍스트는 수정 가능한 뷰에 넣기

            아래 꺼는 다 함수로 묶고,livedata 관찰 해서 새로운 답변이 도착할 때마다 뷰 생성(메모장) 하게 함수 만들기(지은이가 할거)
            !MessageParser 클래스 참고 etc 파일에 잇음!

                코드블록 추가할때 이런식으로 for문 사용해서 넣기
                for (String code : parsedMessage.codeBlocks) {
                    코드 넣을 텍스트 뷰 = new 텍스트뷰(this); //메모장 뷰에 넣게 될듯
                }

                + 삭제기능 버튼 누르면 실행 하는 이벤트 뭐엿더라 그거 안에 넣기
                    +뷰 삭제하는 코드

                + 모드 변경하면 다 삭제하는것도 만들어야할듯


        }
    });

    study_model.getErrorLiveData().observe(this, new Observer<String>() {
        @Override
        public void onChanged(String error) {
            Toast.makeText(MainActivity.this, "오류 발생: " + error, Toast.LENGTH_LONG).show();
        }
    });

    //set해주기
    study_model.setMessage(message); //나중에 삭제
    study_model.setModel("gpt-4");
    study_model.sendMessage();

 */