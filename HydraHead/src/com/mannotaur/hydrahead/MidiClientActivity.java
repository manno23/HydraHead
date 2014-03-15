package com.mannotaur.hydrahead;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MidiClientActivity extends Activity implements SensorEventListener {

    private ControlSurface mGLSurfaceView;
    private WifiHandler handler;
    private WifiEventReceiver receiver;
    private IntentFilter filter;
    private boolean handlerIsRegistered;
    private SensorManager mSensorManager;
    private Sensor mRotationVector;

    private float[] mInitialOrientation;
    private float[] mRotationMatrix;
    private float[] mOutputMatrix;

    private final float MIDI_GYROSCOPE_TYPE = 2f;
    private String MIDICLIENT = "MidiClient";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the initial Window Title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mGLSurfaceView = new ControlSurface(this);
        setContentView(mGLSurfaceView);

        handler = new WifiHandler((WifiManager)getSystemService(Context.WIFI_SERVICE));
        receiver = new WifiEventReceiver((WifiManager)getSystemService(Context.WIFI_SERVICE), handler);
        handlerIsRegistered = false;
        filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mInitialOrientation = null;
        mRotationMatrix = new float[16];
        mOutputMatrix = new float[16];
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.connectToNetwork();
        if(!handlerIsRegistered) {
            registerReceiver(receiver, filter);
            handlerIsRegistered = true;
        }
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_UI); //   50 Updates/s
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(handlerIsRegistered) {
            unregisterReceiver(receiver);
            handlerIsRegistered = false;
        }
        mSensorManager.unregisterListener(this);
        mGLSurfaceView.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                if(mInitialOrientation == null) {
                    // Reverse the rotation
                    mInitialOrientation = new float[16];
                    Matrix.invertM(mInitialOrientation, 0, mRotationMatrix, 0);
                }

                Matrix.multiplyMM(mOutputMatrix, 0, mRotationMatrix, 0, mInitialOrientation, 0);
                Matrix.invertM(mRotationMatrix, 0, mOutputMatrix, 0);
                Float R[] = new Float[17];
                for (int i=1; i<R.length; i++) R[i] = mRotationMatrix[i - 1];
                R[0] = MIDI_GYROSCOPE_TYPE;
                new UDPTask().execute(R);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }


}