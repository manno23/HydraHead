package com.mannotaur.hydrahead;


import android.view.MotionEvent;


public class ConnectingScene implements Scene {

    private Square background;
    private float[] bg_colour;
    private HydraRenderer mRenderer;

    public ConnectingScene(HydraRenderer renderer) {
        mRenderer = renderer;
        bg_colour = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
    }

    @Override
    public void initialise(float ratio) {
        background = new BackgroundSquare(bg_colour);
    }

    @Override
    public void draw(float[] mMVPMatrix) {
        background.draw(mMVPMatrix);
    }

    @Override
    public boolean onTouch(MotionEvent event, int mScreenHeight, int mScreenWidth) {
        mRenderer.transition();
        return true;
    }

    @Override
    public boolean onRotate() {
        return false;
    }

    private class BackgroundSquare extends Square {

        public BackgroundSquare(float[] color) {
            super(color);
        }

    }
}
