package com.github.unispeech.recognition;

/**
* Created by javier.romero on 2/8/14.
*/
public class SpeechData {
    private String mOriginalText;
    private String mTranslatedText;

    public SpeechData(String originalText) {
        mOriginalText = originalText;
    }

    public SpeechData(String originalText, String translatedText) {
        mOriginalText = originalText;
        mTranslatedText = translatedText;
    }

    public String getOriginalText() {
        return mOriginalText;
    }

    public String getTranslatedText() {
        return mTranslatedText;
    }

    public void setTranslatedText(String translatedText) {
        mTranslatedText = translatedText;
    }

    public boolean isTranslated() {
        return mTranslatedText != null;
    }
}
