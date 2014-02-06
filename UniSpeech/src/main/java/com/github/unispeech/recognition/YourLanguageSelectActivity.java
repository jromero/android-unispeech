package com.github.unispeech.recognition;

import android.content.Intent;

/**
 * Created by javier.romero on 2/6/14.
 */
public class YourLanguageSelectActivity extends BaseLanguageSelectActivity {

    @Override
    protected String getPrompt() {
        return "You speak...";
    }

    @Override
    protected void onLanguageClicked(SupportedSttLanguage language) {
        Intent intent = TheirLanguageSelectActivity.newIntent(this, language);
        startActivity(intent);
    }
}
