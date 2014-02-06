package com.github.unispeech.recognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import com.github.unispeech.App;
import com.github.unispeech.R;
import com.google.glass.widget.SliderView;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.rookery.web_api_translate.GoogleTranslator;
import com.rookery.web_api_translate.type.Language;
import com.rookery.web_api_translate.type.TranslateError;

import java.util.ArrayList;
import java.util.List;

public class RecognitionActivity extends Activity {

    private static final String TAG = RecognitionActivity.class.getSimpleName();
    private static final String EXTRA_YOUR_LANG_ISO_CODE = "EXTRA_YOUR_LANG_ISO_CODE";
    private static final String EXTRA_THEIR_LANG_ISO_CODE = "EXTRA_THEIR_LANG_ISO_CODE";
    private static final String GOOGLE_TRANSLATE_API_KEY = "YOUR_KEY_HERE";
    private final List<SpeechData> mSpeechDatas = new ArrayList<SpeechData>();
    private Handler mHandler = new Handler();
    private Recognizer mRecognizer;
    private TextView mText;
    private SupportedSttLanguage mYourLanguage;
    private SupportedSttLanguage mTheirLanguage;
    private boolean mRecognizerRunning = false;
    private ListView mListView;
    private SpeechAdapter mAdapter;
    private GestureDetector mGestureDetector;
    private SliderView mSliderView;
    private TextView mStatusText;

    public static Intent newIntent(Context context, SupportedSttLanguage yourLanguage,
                                   SupportedSttLanguage theirLanguage) {
        Intent intent = new Intent(context, RecognitionActivity.class);
        intent.putExtra(EXTRA_YOUR_LANG_ISO_CODE, yourLanguage.getIsoCode());
        intent.putExtra(EXTRA_THEIR_LANG_ISO_CODE, theirLanguage.getIsoCode());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        readBundle(savedInstanceState);
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new SpeechAdapter(this, mSpeechDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnTouchListener(mOnTouchListener);
        findViewById(R.id.container).setOnTouchListener(mOnTouchListener);

        mStatusText = (TextView) findViewById(R.id.lbl_status);
        mSliderView = (SliderView) findViewById(R.id.indeterm_slider);

        if (App.runningOnGoogleGlass()) {
            mGestureDetector = createGestureDetector(this);
        }

        setStatus(R.string.recog_tap_and_hold);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(EXTRA_YOUR_LANG_ISO_CODE, mYourLanguage.getIsoCode());
        outState.putString(EXTRA_THEIR_LANG_ISO_CODE, mTheirLanguage.getIsoCode());
    }

    @Override
    protected void onDestroy() {
        cancelRecognizer();
        super.onDestroy();
    }

    private void readBundle(Bundle savedInstanceState) {
        String yourIsoCode = null;
        if (savedInstanceState != null) {
            yourIsoCode = savedInstanceState.getString(EXTRA_YOUR_LANG_ISO_CODE);
        } else {
            if (getIntent() != null && getIntent().getExtras() != null) {
                yourIsoCode = getIntent().getExtras().getString(EXTRA_YOUR_LANG_ISO_CODE);
            }
        }

        if (yourIsoCode == null) {
            throw new IllegalStateException("No 'EXTRA_YOUR_LANG_ISO_CODE' in bundle!");
        }

        mYourLanguage = SupportedSttLanguage.fromIsoCode(yourIsoCode);

        String theirIsoCode = null;
        if (savedInstanceState != null) {
            theirIsoCode = savedInstanceState.getString(EXTRA_THEIR_LANG_ISO_CODE);
        } else {
            if (getIntent() != null && getIntent().getExtras() != null) {
                theirIsoCode = getIntent().getExtras().getString(EXTRA_THEIR_LANG_ISO_CODE);
            }
        }

        if (theirIsoCode == null) {
            throw new IllegalStateException("No 'EXTRA_THEIR_LANG_ISO_CODE' in bundle!");
        }


        mTheirLanguage = SupportedSttLanguage.fromIsoCode(theirIsoCode);
    }

    public void addSpeechData(SpeechData speechData) {
        mSpeechDatas.add(speechData);
        mAdapter.notifyDataSetChanged();
    }

    public void updateSpeechData(SpeechData speechData) {
        for (SpeechData currentSpeechData : mSpeechDatas) {
            if (currentSpeechData.equals(speechData)) {
                currentSpeechData.setTranslatedText(speechData.getTranslatedText());
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void startRecognizer() {
        Log.v(TAG, "startRecognizer");
        if (mRecognizer != null) {
            cancelRecognizer();
        }

        mRecognizer = ((App) getApplication()).getSpeechKit().createRecognizer(
                Recognizer.RecognizerType.Dictation,
                Recognizer.EndOfSpeechDetection.None,
                mTheirLanguage.getIsoCode(), mListener, mHandler);

        mRecognizer.start();
        mRecognizerRunning = true;

        startIndeterminate();
        setStatus(R.string.recog_initializing);
    }

    public void stopRecognizer() {
        Log.v(TAG, "stopRecognizer");
        if (mRecognizer != null) {
            mRecognizer.stopRecording();
        }

        mRecognizerRunning = false;
        stopIndeterminate();
        setStatus(R.string.recog_tap_and_hold);
    }

    public void cancelRecognizer() {
        Log.v(TAG, "cancelRecognizer");
        if (mRecognizer != null) {
            mRecognizer.cancel();
        }

        mRecognizerRunning = false;
        stopIndeterminate();
        setStatus(R.string.recog_tap_and_hold);
    }

    public void runTranslation(SpeechData speechData) {
        Log.v(TAG, "runTranslation");

        if (!mYourLanguage.equals(mTheirLanguage)) {
            startIndeterminate();
            GoogleTranslator.getInstance().execute(speechData.getOriginalText(),
                    mYourLanguage.getTranslationLanguage(),
                    GOOGLE_TRANSLATE_API_KEY, new TranslatorCallback(this, speechData));
        } else {
            speechData.setTranslatedText(speechData.getOriginalText());
            updateSpeechData(speechData);
            setStatus(R.string.recog_tap_and_hold);
        }
    }

    public void setStatus(String text) {
        mStatusText.setText(text);
    }

    public void setStatus(int resource) {
        mStatusText.setText(getString(resource));
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);

        //Create a base listener for generic gestures
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int newCount) {
                if (newCount == 1 && previousCount == 0) {
                    startRecognizer();
                } else if (newCount == 0) {
                    stopRecognizer();
                }
            }
        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    public void startIndeterminate() {
        mSliderView.setVisibility(View.VISIBLE);
        mSliderView.startIndeterminate();
    }

    public void stopIndeterminate() {
        mSliderView.stopIndeterminate();
        mSliderView.setVisibility(View.INVISIBLE);
    }

    public void setProgress(long progress) {
        mSliderView.setVisibility(View.VISIBLE);
        mSliderView.startProgress(progress);
    }

    public void dismissProgress() {
        mSliderView.dismissManualProgress();
        mSliderView.setVisibility(View.INVISIBLE);
    }

    private static class TranslatorCallback implements GoogleTranslator.Callback {

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

    public static class SpeechAdapter extends ArrayAdapter<SpeechData> {

        private final LayoutInflater mLayoutInflater;

        public SpeechAdapter(Context context, List<SpeechData> speechDataList) {
            super(context, 0, speechDataList);
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpeechData data = getItem(position);

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_speech, null);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (data.isTranslated()) {
                holder.text.setText(data.getTranslatedText());
                holder.text.setTypeface(null, Typeface.NORMAL);
            } else {
                holder.text.setText(data.getOriginalText());
                holder.text.setTypeface(null, Typeface.ITALIC);
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        public final TextView text;

        public ViewHolder(View view) {
           text = (TextView) view.findViewById(R.id.lbl_speech);
        }
    }

    public static class SpeechData {
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


    private Recognizer.Listener mListener = new Recognizer.Listener() {

        @Override
        public void onRecordingBegin(Recognizer recognizer) {
            Log.v(TAG, "onRecordingBegin");
            setProgress(50L);
            RecognitionActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setStatus(R.string.recog_listenting);
                }
            });
        }

        @Override
        public void onRecordingDone(Recognizer recognizer) {
            Log.v(TAG, "onRecordingDone");
            dismissProgress();
            RecognitionActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setStatus(R.string.recog_processing);
                }
            });
        }

        @Override
        public void onResults(Recognizer recognizer, Recognition recognition) {
            Log.v(TAG, "onResults");
            Log.v(TAG, "Results: " + recognition.getResultCount());

            if (recognition.getResultCount() < 0) {
                Log.w(TAG, "No results back!");
                return;
            }

            mRecognizerRunning = false;

            final String text = recognition.getResult(0).getText();
            RecognitionActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopIndeterminate();
                    dismissProgress();
                    SpeechData speechData = new SpeechData(text);
                    addSpeechData(speechData);
                    runTranslation(speechData);
                }
            });
        }

        @Override
        public void onError(Recognizer recognizer, final SpeechError speechError) {
            Log.v(TAG, "onError");
            Log.v(TAG, "Error: " + speechError.getErrorDetail());
            RecognitionActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopIndeterminate();
                    dismissProgress();
                    mRecognizerRunning = false;
                    setStatus(speechError.getErrorDetail());
                }
            });
        }
    };


    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecognizer();
                    break;
                case MotionEvent.ACTION_UP:
                    stopRecognizer();
                    break;
            }

            return true;
        }
    };
}
