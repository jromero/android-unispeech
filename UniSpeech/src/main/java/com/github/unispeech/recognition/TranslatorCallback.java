package com.github.unispeech.recognition;

import android.text.Html;
import android.util.Log;

import com.github.unispeech.R;
import com.rookery.web_api_translate.GoogleTranslator;
import com.rookery.web_api_translate.type.Language;
import com.rookery.web_api_translate.type.TranslateError;

/**
* Created by javier.romero on 2/8/14.
*/
class TranslatorCallback implements GoogleTranslator.Callback {

    private static final String TAG = TranslatorCallback.class.getSimpleName();
    // FIXME: Make weak reference
    private final RecognitionActivity mActivity;
    private final SpeechData mSpeechData;

    public TranslatorCallback(RecognitionActivity activity, SpeechData speechData) {
        mActivity = activity;
        mSpeechData = speechData;
        mActivity.setStatus(R.string.recog_translating);
    }

    @Override
    public void onSuccess(Language detected_lang, String translated_text) {
        Log.v(TAG, "GoogleTranslator: onSuccess: from " + detected_lang + " '" + translated_text + "'");

        // decode html: http://stackoverflow.com/questions/2918920/decode-html-entities-in-android
        translated_text = Html.fromHtml(Html.fromHtml(translated_text).toString()).toString();

        mSpeechData.setTranslatedText(translated_text);
        mActivity.updateSpeechData(mSpeechData);
        mActivity.stopIndeterminate();
        mActivity.setStatus(R.string.recog_tap_and_hold);
    }

    @Override
    public void onFailed(TranslateError e) {
        Log.e(TAG, "GoogleTranslator: onFailed", e);
        mActivity.stopIndeterminate();
        mActivity.setStatus(e.getMessage());
    }
}
