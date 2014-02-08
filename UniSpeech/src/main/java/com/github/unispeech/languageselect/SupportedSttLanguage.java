package com.github.unispeech.languageselect;

import com.rookery.web_api_translate.type.Language;

/**
 * Created by javier.romero on 2/5/14.
 */
public enum SupportedSttLanguage {
    ENGLISH_US("eng-AUS", "English", "US", Language.ENGLISH),
    ENGLISH_UK("eng-GBR", "English", "UK", Language.ENGLISH),
    ENGLISH_AUS("eng-USA", "English", "Australia", Language.ENGLISH),
    ARABIC_EGYPT("ara-EGY", "Arabic", "Egypt", Language.ARABIC),
    ARABIC_SAUDI("ara-SAU", "Arabic", "Saudi", Language.ARABIC),
    ARABIC_UAE("ara-XWW", "Arabic", "UAE", Language.ARABIC),
    //CANTONESE("yue-CHN", "Cantonese", "China", ????),
    CATALAN("cat-ESP", "Catalan", "Spain", Language.CATALAN),
    CZECH("ces-CZE", "Czech", Language.CZECH),
    DANISH("dan-DNK", "Danish", Language.DANISH),
    DUTCH("nld-NLD", "Dutch", Language.DUTCH),
    FINNISH("fin-FIN", "Finnish", Language.FINNISH),
    FRENCH_CAN("fra-CAN", "French", "Canada", Language.FRENCH),
    FRENCH_EU("fra-FRA", "French", "Europe", Language.FRENCH),
    GERMAN("deu-DEU", "German", Language.GERMAN),
    GREEK("ell-GRC", "Greek", Language.GREEK),
    HEBREW("heb-ISR", "Hebrew", Language.HEBREW),
    HUNGARIAN("hun-HUN", "Hungarian", Language.HUNGARIAN),
    INDONESIAN("ind-IDN", "Indonesian", Language.INDONESIAN),
    ITALIAN("ita-ITA", "Italian", Language.ITALIAN),
    JAPANESE("jpn-JPN", "Japanese", Language.JAPANESE),
    KOREAN("kor-KOR", "Korean", Language.KOREAN),
    MALAY("zlm-MYS", "Malay", Language.MALAY),
    MANDARIN_CHN("cmn-CHN", "Mandarin", "China", Language.CHINESE_SIMPLIFIED),
    MANDARIN_TWN("cmn-TWN", "Mandarin", "Taiwan", Language.CHINESE_SIMPLIFIED),
    NORWEGIAN("nor-NOR", "Norwegian", Language.NORWEGIAN),
    POLISH("pol-POL", "Polish", Language.POLISH),
    PORTUGUESE_BRA("por-BRA", "Portuguese", "Brazil", Language.PORTUGUESE),
    PORTUGUESE_EU("por-PRT", "Portuguese", "Europe", Language.PORTUGUESE),
    ROMANIAN("ron-ROU", "Romanian", Language.ROMANIAN),
    RUSSIAN("rus-RUS", "Russian", Language.RUSSIAN),
    SLOVAK("slk-SVK", "Slovak", Language.SLOVAK),
    SPANISH_EU("spa-ESP", "Spanish", "Europe", Language.SPANISH),
    SPANISH_MX("spa-XLA", "Spanish", "Mexico", Language.SPANISH),
    SWEDISH("swe-SWE", "Swedish", Language.SWEDISH),
    THAI("tha-THA", "Thai", Language.THAI),
    TURKISH("tur-TUR", "Turkish", Language.TURKISH),
    UKRAINIAN("ukr-UKR", "Ukrainian", Language.UKRANIAN),
    VIETNAMESE("vie-VNM", "Vietnamese", Language.VIETNAMESE);

    private final String mIsoCode;
    private final String mLabel;
    private final Language mTranslateLang;
    private final String mSpecifier;

    private SupportedSttLanguage(String isoCode, String label, Language translateLanguage) {
        this(isoCode, label, null, translateLanguage);
    }

    private SupportedSttLanguage(String isoCode, String label, String specifier, Language translateLanguage) {
        mIsoCode = isoCode;
        mLabel = label;
        mSpecifier = specifier;
        mTranslateLang = translateLanguage;
    }

    public String getIsoCode() {
        return mIsoCode;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getSpecifier() {
        return mSpecifier;
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
