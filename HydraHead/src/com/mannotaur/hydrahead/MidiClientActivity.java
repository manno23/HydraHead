package com.mannotaur.hydrahead;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MidiClientActivity extends Activity {

    private ControlSurface mGLSurfaceView;
    private boolean rendererRegistered;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the initial Window Title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mGLSurfaceView = new ControlSurface(this);
        rendererRegistered = false;
        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!rendererRegistered) {
            mGLSurfaceView.onResume();
            rendererRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererRegistered) {
            mGLSurfaceView.onPause();
            rendererRegistered = false;
        }
    }

}