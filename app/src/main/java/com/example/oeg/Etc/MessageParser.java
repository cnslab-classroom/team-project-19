package com.example.oeg.Etc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {
    public static class ParsedMessage implements Serializable { // 파싱 한거 담는거
        public String textContent; // 일반 텍스트 부분
        public ArrayList<String> codeBlocks; // 코드 블록 목록

        public ParsedMessage(String textContent, ArrayList<String> codeBlocks) {
            this.textContent = textContent;
            this.codeBlocks = codeBlocks;
        }
    }

    public static ParsedMessage parseResponse(String response) {
        ArrayList<String> codeBlocks = new ArrayList<>();
        StringBuilder textContentBuilder = new StringBuilder();

        // ```로 감싸진 부분 찾기
        Pattern pattern = Pattern.compile("(?s)(```(?:\\w+)?\\s*\\n(.*?)```)");
        Matcher matcher = pattern.matcher(response);

        int lastEnd = 0;
        while (matcher.find()) {
            String textBeforeCode = response.substring(lastEnd, matcher.start());
            textContentBuilder.append(textBeforeCode);

            String codeBlock = matcher.group(1); // ``` 포함
            String codeContent = matcher.group(2); // 코드 내용만
            codeBlocks.add(codeContent.trim());

            lastEnd = matcher.end();
        }

        if (lastEnd < response.length()) {
            textContentBuilder.append(response.substring(lastEnd));
        }

        String textContent = textContentBuilder.toString().trim();

        return new ParsedMessage(textContent, codeBlocks);
    }
}
