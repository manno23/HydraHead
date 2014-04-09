package com.mannotaur.hydrahead;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class ControlSurface extends GLSurfaceView {

    private HydraRenderer hydraRenderer;

    public ControlSurface(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        hydraRenderer = new HydraRenderer(context);
        setRenderer(hydraRenderer);

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
}
