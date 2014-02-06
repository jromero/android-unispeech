package com.github.unispeech;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.nuance.nmdp.speechkit.SpeechKit;

/**
 * Created by javier.romero on 2/5/14.
 */
public class App extends Application {

    private static final byte[] SPEECH_KIT_APPLICATION_KEY = {
    };
    private static final String API_ID = "YOUR_ID_HERE";
    private static final String HOST = "YOUR_HOST_HERE";

    private SpeechKit mSpeechKit;

    @Override
    public void onCreate() {
        super.onCreate();

        mSpeechKit = SpeechKit.initialize(
                this.getApplicationContext(),
                API_ID,
                HOST,
                443, false, SPEECH_KIT_APPLICATION_KEY);

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
