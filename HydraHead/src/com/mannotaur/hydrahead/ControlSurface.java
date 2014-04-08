package com.mannotaur.hydrahead;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ControlSurface extends GLSurfaceView {

    private HydraRenderer hydraRenderer;

    public ControlSurface(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        hydraRenderer = new HydraRenderer(context);
        setRenderer(hydraRenderer);
    }

}
