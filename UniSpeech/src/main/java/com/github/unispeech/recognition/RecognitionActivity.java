package com.github.unispeech.recognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.github.unispeech.App;
import com.github.unispeech.R;
import com.github.unispeech.languageselect.SupportedSttLanguage;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.widget.SliderView;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.rookery.web_api_translate.GoogleTranslator;

import java.util.ArrayList;
import java.util.List;

public class RecognitionActivity extends Activity {

    private static final String TAG = RecognitionActivity.class.getSimpleName();
    private static final String EXTRA_YOUR_LANG_ISO_CODE = "EXTRA_YOUR_LANG_ISO_CODE";
    private static final String EXTRA_THEIR_LANG_ISO_CODE = "EXTRA_THEIR_LANG_ISO_CODE";
    private final List<SpeechData> mSpeechDatas = new ArrayList<SpeechData>();
    private Handler mHandler = new Handler();
    private Recognizer mRecognizer;
    private SupportedSttLanguage mYourLanguage;
    private SupportedSttLanguage mTheirLanguage;
    private ListView mListView;
    private SpeechAdapter mAdapter;
    private GestureDetector mGestureDetector;
    private SliderView mSliderView;
    private TextView mStatusText;
    private boolean mRecognizerRecording = false;

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
            mGestureDetector = new GestureDetector(this);
            mGestureDetector.setAlwaysConsumeEvents(true);
            mGestureDetector.setBaseListener(mBaseListener);
            mGestureDetector.setScrollListener(mScrollListener);
            mGestureDetector.setFingerListener(mFingerListener);
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
        startIndeterminate();
        setStatus(R.string.recog_initializing);
    }

    public void stopRecognizer() {
        Log.v(TAG, "stopRecognizer");
        if (mRecognizer != null) {
            mRecognizer.stopRecording();
        }

        stopIndeterminate();
        setStatus(R.string.recog_tap_and_hold);
    }

    public void cancelRecognizer() {
        Log.v(TAG, "cancelRecognizer");
        if (mRecognizer != null) {
            mRecognizer.cancel();
        }

        stopIndeterminate();
        setStatus(R.string.recog_tap_and_hold);
    }

    public void runTranslation(SpeechData speechData) {
        Log.v(TAG, "runTranslation");

        if (!mYourLanguage.equals(mTheirLanguage)) {
            startIndeterminate();
            GoogleTranslator.getInstance().execute(speechData.getOriginalText(),
                    mYourLanguage.getTranslationLanguage(),
                    App.GOOGLE_TRANSLATE_API_KEY, new TranslatorCallback(this, speechData));
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

    public void setProgress(float progress) {
        mSliderView.setVisibility(View.VISIBLE);
        mSliderView.setManualProgress(progress);
    }

    public void dismissProgress() {
        mSliderView.dismissManualProgress();
        mSliderView.setVisibility(View.INVISIBLE);
    }

    private Recognizer.Listener mListener = new Recognizer.Listener() {

        @Override
        public void onRecordingBegin(Recognizer recognizer) {
            Log.v(TAG, "onRecordingBegin");
            mRecognizerRecording = true;
            setProgress(1f);
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
            mRecognizerRecording = false;
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
            if (speechError.getErrorCode() != SpeechError.Codes.CanceledError) {
                RecognitionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopIndeterminate();
                        dismissProgress();
                        setStatus(speechError.getErrorDetail());
                    }
                });
            }
        }
    };

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.v(TAG, "Event caught: ACTION_DOWN");
                    startRecognizer();
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.v(TAG, "Event caught: ACTION_UP");
                    stopRecognizer();
                    return true;
            }

            return false;
        }
    };

    private GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            Log.v(TAG, "onGesture: " + gesture);
            switch (gesture) {
            }
            return false;
        }
    };

    private GestureDetector.ScrollListener mScrollListener = new GestureDetector.ScrollListener() {
        @Override
        public boolean onScroll(float v, float v2, float v3) {
            Log.v(TAG, "onScroll: " + v + "," + v2 + ","+ v3);
            return true;
        }
    };

    /**
     * Detect changes in fingers to start, pause, stop the recording process
     */
    private GestureDetector.FingerListener mFingerListener = new GestureDetector.FingerListener() {
        @Override
        public void onFingerCountChanged(int previousCount, int newCount) {
            Log.v(TAG, "onFingerCountChanged: " + previousCount + "," + newCount);
            if (newCount == 1 && previousCount == 0) {
                startRecognizer();
            } else if (newCount == 0) {
                if (mRecognizerRecording) {
                    stopRecognizer();
                } else {
                    cancelRecognizer();
                }
            }
        }
    };
}
