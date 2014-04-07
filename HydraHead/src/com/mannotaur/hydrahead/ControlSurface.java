package com.mannotaur.hydrahead;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class ControlSurface extends GLSurfaceView {

    private HydraRenderer hydraRenderer;

    public ControlSurface(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        hydraRenderer = new HydraRenderer(context);
        setRenderer(hydraRenderer);

    }

}
