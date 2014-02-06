package com.github.unispeech.recognition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by javier.romero on 2/6/14.
 */
public class TheirLanguageSelectActivity extends BaseLanguageSelectActivity {

    private static final String EXTRA_YOUR_LANGUAGE = "EXTRA_YOUR_LANGUAGE";
    private SupportedSttLanguage mYourLanguage;

    public static Intent newIntent(Context context, SupportedSttLanguage yourLanguage) {
        Intent intent = new Intent(context, TheirLanguageSelectActivity.class);
        intent.putExtra(EXTRA_YOUR_LANGUAGE, yourLanguage.getIsoCode());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readBundle(savedInstanceState);
    }


    private void readBundle(Bundle savedInstanceState) {
        String isoCode = null;
        if (savedInstanceState != null) {
            isoCode = savedInstanceState.getString(EXTRA_YOUR_LANGUAGE);
        } else {
            if (getIntent() != null && getIntent().getExtras() != null) {
                isoCode = getIntent().getExtras().getString(EXTRA_YOUR_LANGUAGE);
            }
        }

        if (isoCode == null) {
            throw new IllegalStateException("No 'EXTRA_YOUR_LANGUAGE' passed!");
        }

        mYourLanguage = SupportedSttLanguage.fromIsoCode(isoCode);
    }

    @Override
    protected String getPrompt() {
        return "They speak...";
    }

    @Override
    protected void onLanguageClicked(SupportedSttLanguage language) {
        Intent intent = RecognitionActivity.newIntent(this, mYourLanguage, language);
        startActivity(intent);
    }
}
