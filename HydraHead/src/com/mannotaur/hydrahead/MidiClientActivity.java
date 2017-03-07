package com.mannotaur.hydrahead;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MidiClientActivity extends Activity {

    private HydraHead hydraHead;
    private ControlSurface mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the initial Window Title
        // TODO maybe we can do this in layout properties
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        hydraHead = new HydraHead(this);
        mGLSurfaceView = new ControlSurface(this, hydraHead.getSceneAdapter());

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hydraHead.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hydraHead.disconnect();
    }

}