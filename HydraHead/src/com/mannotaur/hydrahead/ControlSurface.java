package com.mannotaur.hydrahead;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;


public class ControlSurface extends GLSurfaceView implements SensorEventListener {

    private static final String TAG = "ControlSurface";
    private Context context;
    private SceneAdapter mCurrentScene;
    private HydraRenderer hydraRenderer;
    private boolean rendererRegistered;


    private SensorManager mSensorManager;
    private Sensor mRotationVector;


    /* Lifecycle events */

    public ControlSurface(Context context, SceneAdapter mCurrentScene) {
        super(context);
        this.context = context;
        this.mCurrentScene = mCurrentScene;

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        hydraRenderer = new HydraRenderer(mCurrentScene);
        setRenderer(hydraRenderer);
        rendererRegistered = false;     // this here seems ugly

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Log.d("TAG", "Renderer shouldve registered");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(!rendererRegistered) {
            rendererRegistered = true;
        }
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL); //   50 Updates/s
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
        mSensorManager.unregisterListener(this);
        if(rendererRegistered) {
            rendererRegistered = false;
        }
    }


    /* Sensor Event Listeners */
    @Override
    public boolean onTouchEvent(MotionEvent event) { mCurrentScene.onTouch(event); return true; }

    @Override
    public void onSensorChanged(SensorEvent event) { mCurrentScene.onSensorChanged(event); }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }




}

