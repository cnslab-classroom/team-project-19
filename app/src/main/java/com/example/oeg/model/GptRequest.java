package com.example.oeg.model;

import java.util.List;
public class GptRequest {
    private String model;
    private List<Message> messages;

    public GptRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
