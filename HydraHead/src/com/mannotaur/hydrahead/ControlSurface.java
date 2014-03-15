package com.mannotaur.hydrahead;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.*;

public class ControlSurface extends GLSurfaceView {

    private final int mScreenHeight;
    private final int mScreenWidth;
    private HydraRenderer hydraRenderer;
    private String MIDICLIENT = "MidiClient";

    public ControlSurface(Context context) {
        super(context);

        WindowManager window = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        mScreenHeight = point.y;
        mScreenWidth = point.x;

        setEGLContextClientVersion(2);
        hydraRenderer = new HydraRenderer();
        setRenderer(hydraRenderer);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(MIDICLIENT, event.getY() + " " + event.getX());
        if (event.getY() < (float)(mScreenHeight/2)) {
            if (event.getX() < (float)(mScreenWidth/2)) {
                hydraRenderer.squarePressed(1);
                new UDPTask().execute(1.0f, 1.0f);
            } else {
                hydraRenderer.squarePressed(2);
                new UDPTask().execute(1.0f, 2.0f);
            }
        } else {
            if (event.getX() < (float)(mScreenWidth/2)) {
                hydraRenderer.squarePressed(3);
                new UDPTask().execute(1.0f, 3.0f);
            } else {
                hydraRenderer.squarePressed(4);
                new UDPTask().execute(1.0f, 4.0f);
            }

        }

        return super.onTouchEvent(event);
    }

}
