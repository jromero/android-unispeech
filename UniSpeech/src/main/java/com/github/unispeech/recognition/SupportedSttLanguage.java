package com.github.unispeech.recognition;

import com.rookery.web_api_translate.type.Language;

/**
 * Created by javier.romero on 2/5/14.
 */
public enum SupportedSttLanguage {
    ENGLISH_US("eng-USA", "English (US)", Language.ENGLISH),
    SPANISH_MX("spa-XLA", "Spanish (MX)", Language.SPANISH),
    KOREAN("kor-KOR", "Korean", Language.KOREAN),
    JAPANESE("jpn-JPN", "Japanese", Language.JAPANESE),
    MANDARIN_CH("cmn-CHN", "Mandarin Chinese", Language.CHINESE_SIMPLIFIED);

    private final String mIsoCode;
    private final String mLabel;
    private final Language mTranslateLang;

    private SupportedSttLanguage(String isoCode, String label, Language translateLanguage) {
        mIsoCode = isoCode;
        mLabel = label;
        mTranslateLang = translateLanguage;
    }

    public String getIsoCode() {
        return mIsoCode;
    }

    public String getLabel() {
        return mLabel;
    }

    public Language getTranslationLanguage() {
        return mTranslateLang;
    }

    public static SupportedSttLanguage fromIsoCode(String isoCode) {
        SupportedSttLanguage[] values = SupportedSttLanguage.values();
        for (SupportedSttLanguage value : values) {
            if (value.getIsoCode().equals(isoCode)) {
                return value;
            }
        }

        throw new IllegalArgumentException("IsoCode: " + isoCode + " is not invalid!");
    }
}
