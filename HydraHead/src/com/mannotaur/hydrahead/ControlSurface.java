package com.mannotaur.hydrahead;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.messages.RotationMessage;

public class ControlSurface extends GLSurfaceView implements SensorEventListener {

    private Context context;
    private HydraRenderer hydraRenderer;
    public Networking mNetworkInterface;
    private final WifiHandler handler;
    private final WifiEventReceiver receiver;
    private IntentFilter filter;
    private boolean handlerIsRegistered;
    private SensorManager mSensorManager;
    private Sensor mRotationVector;

    private float[] mInitialOrientation;
    private float[] mRotationMatrix;
    private float[] mOutputMatrix;

    public ControlSurface(Context context) {
        super(context);
        this.context = context;

        setEGLContextClientVersion(2);
        hydraRenderer = new HydraRenderer(context, mNetworkInterface);
        setRenderer(hydraRenderer);

        mNetworkInterface = new Networking();
        handler = new WifiHandler(context);
        receiver = new WifiEventReceiver(context, handler, mNetworkInterface);

        handlerIsRegistered = false;
        filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mInitialOrientation = null;
        mRotationMatrix = new float[16];
        mOutputMatrix = new float[16];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hydraRenderer.onTouch(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                hydraRenderer.onTouch(event.getX());
                break;
            case MotionEvent.ACTION_UP:
                hydraRenderer.onTouch(event.getX());
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_UI); //   50 Updates/s
        if(!handlerIsRegistered) {
            context.registerReceiver(receiver, filter);
            handlerIsRegistered = true;
        }

        handler.connectToNetwork();
    }

    @Override
    public void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
        if(handlerIsRegistered) {
            context.unregisterReceiver(receiver);
            handlerIsRegistered = false;
        }

        mNetworkInterface.close();
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
                mNetworkInterface.send(new RotationMessage(mRotationMatrix));
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
