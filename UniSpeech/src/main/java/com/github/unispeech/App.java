package com.github.unispeech;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.nuance.nmdp.speechkit.SpeechKit;

/**
 * Created by javier.romero on 2/5/14.
 */
public class App extends Application {

    private static final byte[] NUANCE_APPLICATION_KEY = {
    };

    private static final String NUANCE_API_ID = "YOUR_APP_ID_HERE";
    private static final String NUANCE_HOST = "YOUR_HOST_HERE";
    public static final String GOOGLE_TRANSLATE_API_KEY = "YOUR_GOOGLE_TRANSLATE_API_KEY";

    private SpeechKit mSpeechKit;

    @Override
    public void onCreate() {
        super.onCreate();

        mSpeechKit = SpeechKit.initialize(
                this.getApplicationContext(),
                NUANCE_API_ID,
                NUANCE_HOST,
                443, false, NUANCE_APPLICATION_KEY);

        mSpeechKit.connect();
    }

    public SpeechKit getSpeechKit() {
        return mSpeechKit;
    }

    public static boolean runningOnGoogleGlass() {
        Log.v("@@@", "DEVICE: " + Build.DEVICE);
        Log.v("@@@", "MODEL: " + Build.MODEL);
        Log.v("@@@", "PRODUCT: " + Build.PRODUCT);
        return Build.DEVICE.contains("glass");
    }
}
