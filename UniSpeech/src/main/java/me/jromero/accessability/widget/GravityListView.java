package me.jromero.accessability.widget;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class GravityListView extends ListView {

    private static final String TAG = GravityListView.class.getSimpleName();
    private static final boolean ENABLED = false;

    private SensorManager mSensorManager;

    public GravityListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GravityListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GravityListView(Context context) {
        super(context);
    }

    public View getViewAtPosition(int index) {
        int firstPosition = getFirstVisiblePosition() - getHeaderViewsCount();
        int wantedChild = index - firstPosition;

        if (wantedChild < 0 || wantedChild >= getChildCount()) {
            Log.w(TAG, "Unable to get view for desired position");
        }

        return getChildAt(wantedChild);
    }

    public void onCreate() {
        mSensorManager = (SensorManager) getContext().getSystemService(
                Context.SENSOR_SERVICE);
    }

    public void onResume() {
        registerListeners();
    }

    public void onPause() {
        unregisterListeners();
    }

    public void onDestroy() {
        unregisterListeners();
    }

    private void registerListeners() {
        mSensorManager.registerListener(mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterListeners() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        private static final int NORMAL_INTERUPT = 1000;
        private static final boolean DEBUG = false;

        private long mNextChangeMilli = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!ENABLED) {
                return;
            }

            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                long currentTime = SystemClock.elapsedRealtime();
                if (mNextChangeMilli == 0) {
                    if (DEBUG) Log.v(TAG, "Started!");
                } else if (currentTime < mNextChangeMilli) {
                    return;
                }

                float[] values = event.values;
                if (DEBUG) Log.v(TAG, "z = " + values[2]);
                int scrollIntensity = Math.round(values[2]);
                if (DEBUG) Log.v(TAG, "scroll intensity = " + scrollIntensity);

                int scrollDirection = 0;
                if (scrollIntensity >= 4) {
                    if (DEBUG) Log.d(TAG, "intensity: +faster");
                    mNextChangeMilli = currentTime + (NORMAL_INTERUPT / 8);
                    scrollDirection = 1;
                } else if (scrollIntensity >= 3) {
                    if (DEBUG) Log.d(TAG, "intensity: +fast");
                    mNextChangeMilli = currentTime + (NORMAL_INTERUPT / 2);
                    scrollDirection = 1;
                } else if (scrollIntensity >= 2) {
                    if (DEBUG) Log.d(TAG, "intensity: +normal");
                    mNextChangeMilli = currentTime + NORMAL_INTERUPT;
                    scrollDirection = 1;
                } else if (scrollIntensity <= -4) {
                    if (DEBUG) Log.d(TAG, "intensity: -faster");
                    mNextChangeMilli = currentTime + (NORMAL_INTERUPT / 8);
                    scrollDirection = -1;
                } else if (scrollIntensity <= -3) {
                    if (DEBUG) Log.d(TAG, "intensity: -fast");
                    mNextChangeMilli = currentTime + (NORMAL_INTERUPT / 2);
                    scrollDirection = -1;
                } else if (scrollIntensity <= -2) {
                    Log.d(TAG, "intensity: -normal");
                    mNextChangeMilli = currentTime + NORMAL_INTERUPT;
                    scrollDirection = -1;
                } else {
                    // do nothing
                    return;
                }

                if (DEBUG) Log.v(TAG, "scroll direction = " + scrollDirection);

                int selectedItemPosition = getSelectedItemPosition();
                View selectedView = getViewAtPosition(selectedItemPosition);
                if (selectedView != null) {
                    setSelectionFromTop(selectedItemPosition + scrollDirection,
                            selectedView.getHeight());
                } else {
                    setSelection(selectedItemPosition + scrollDirection);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    };
}
