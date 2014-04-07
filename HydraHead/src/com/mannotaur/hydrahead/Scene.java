package com.mannotaur.hydrahead;

import android.view.MotionEvent;

public interface Scene {

    public abstract void initialise(float ratio);
    public abstract void draw(float[] mMVPMatrix);
    public abstract boolean onTouch(MotionEvent event, int mScreenHeight, int mScreenWidth);
    public abstract boolean onRotate();

}
