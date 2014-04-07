package com.mannotaur.hydrahead;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.*;
import com.mannotaur.hydrahead.Messages.ButtonDownMessage;
import com.mannotaur.hydrahead.Messages.ButtonUpMessage;

public class ControlSurface extends GLSurfaceView {

    private final int mScreenHeight;
    private final int mScreenWidth;
    private HydraRenderer hydraRenderer;
    private MidiClientActivity parentActivity;
    private String MIDICLIENT = "MidiClient";

    public ControlSurface(Context context) {
        super(context);

        parentActivity = (MidiClientActivity)context;
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
        Log.d(MIDICLIENT, event.toString());
        switch(event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                parentActivity.mNetworkInterface.send(new ButtonDownMessage(event.getX(), event.getY(), mScreenWidth, mScreenHeight));

            case MotionEvent.ACTION_UP:
                parentActivity.mNetworkInterface.send(new ButtonUpMessage(event.getX(), event.getY(), mScreenWidth, mScreenHeight));

        }
        return hydraRenderer.touchEvent(event, mScreenHeight, mScreenWidth);
    }

}
