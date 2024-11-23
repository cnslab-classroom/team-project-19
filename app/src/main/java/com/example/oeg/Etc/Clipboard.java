package com.example.oeg.Etc;



public class Clipboard {
    private static volatile String currentCopiedText = "";

    public static synchronized String getCurrentCopiedText() {
        return currentCopiedText;
    }

    public static synchronized void updateCurrentCopiedText(String text) {
        currentCopiedText = text;
    }

    public static synchronized void clearCurrentCopiedText() {
        currentCopiedText = "";
    }
}
