package com.mannotaur.hydrahead;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;


public class ActiveScene implements Scene {

    private InteractiveSquare  mTopLeftSquare;
    private InteractiveSquare  mTopRightSquare;
    private InteractiveSquare  mBottomLeftSquare;
    private InteractiveSquare mBottomRightSquare;

    float BLUE[] = { 0.2f, 0.709803922f, 0.898039216f, 0.2f };
    float YELLOW[] = { 0.709803922f, 0.898039216f, 0.2f, 0.2f };
    float RED[] = { 0.709803922f, 0.2f, 0.898039216f, 0.2f };
    float GREEN[] = { 0.2f, 0.1f, 0.898039216f, 0.2f };
    private String MIDICLIENT = "MidiClient";

    private int mVertexShader;
    private int mFragmentShader;

    public ActiveScene(float ratio) {
        initialise(ratio);
    }

    @Override
    public void initialise(float ratio) {
        mTopLeftSquare = new InteractiveSquare(BLUE, ratio, new float[]{0.5f, 0.5f});
        mTopRightSquare = new InteractiveSquare(YELLOW, ratio, new float[]{-0.5f, 0.5f});
        mBottomLeftSquare = new InteractiveSquare(RED, ratio, new float[]{0.5f, -0.5f});
        mBottomRightSquare = new InteractiveSquare(GREEN, ratio, new float[]{-0.5f, -0.5f});

    }

    @Override
    public void draw(float[] mMVPMatrix) {
        // Draw squares
        mTopLeftSquare.draw(mMVPMatrix);
        mTopRightSquare.draw(mMVPMatrix);
        mBottomLeftSquare.draw(mMVPMatrix);
        mBottomRightSquare.draw(mMVPMatrix);
    }

    @Override
    public boolean onTouch(MotionEvent event, int mScreenHeight, int mScreenWidth) {
        Log.d(MIDICLIENT, event.toString());
        switch(event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (event.getY() < (float)(mScreenHeight/2)) {
                    if (event.getX() < (float)(mScreenWidth/2)) {
                        mTopLeftSquare.onClick();
                    } else {
                        mTopRightSquare.onClick();
                    }
                } else {
                    if (event.getX() < (float)(mScreenWidth/2)) {
                        mBottomLeftSquare.onClick();
                    } else {
                        mBottomRightSquare.onClick();
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (event.getY() < (float)(mScreenHeight/2)) {
                    if (event.getX() < (float)(mScreenWidth/2)) {
                        mTopLeftSquare.onRelease();
                    } else {
                        mTopRightSquare.onRelease();
                    }
                } else {
                    if (event.getX() < (float)(mScreenWidth/2)) {
                        mBottomLeftSquare.onRelease();
                    } else {
                        mBottomRightSquare.onRelease();
                    }
                }
                return true;
        }

        return false;
    }

    @Override
    public boolean onRotate() {
        return false;
    }

}
