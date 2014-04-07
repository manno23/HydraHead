package com.mannotaur.hydrahead;


import android.view.MotionEvent;

public class TransitionScene implements Scene {

    Scene mScene1;
    Scene mScene2;
    float alpha;
    float ratio;

    public TransitionScene(Scene scene1, Scene scene2, float ratio) {
        mScene1 = scene1;
        mScene2 = scene2;
        alpha = 0.0f;
        this.ratio = ratio;
        initialise(ratio);
    }

    @Override
    public void initialise(float ratio) {
        mScene1.initialise(ratio);
        mScene2.initialise(ratio);
    }

    @Override
    public void draw(float[] mMVPMatrix) {
        //set alpha for scene1
        mScene1.draw(mMVPMatrix);
        //set alpha for scene2
        mScene2.draw(mMVPMatrix);
        if (alpha < 1.0f) alpha += 0.02f;
    }

    @Override
    public boolean onTouch(MotionEvent event, int mScreenHeight, int mScreenWidth) {
        return false;
    }

    @Override
    public boolean onRotate() {
        return false;
    }

}
