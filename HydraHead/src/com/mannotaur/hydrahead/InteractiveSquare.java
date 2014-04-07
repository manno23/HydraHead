package com.mannotaur.hydrahead;

import android.opengl.Matrix;

/**
 * User: jason
 * Date: 12/03/14
 * Time: 3:51 PM
 */
public class InteractiveSquare extends Square {
    private final float[] mTranslate;
    private float mRatio;
    private boolean squarePressed;

    public InteractiveSquare(float [] colour, float ratio, float[] translate) {
        super(colour);
        mRatio = ratio;
        mTranslate = translate;
        squarePressed = false;
    }

    @Override
    public void draw(float[] mMVPMatrix) {
        float[] transform = new float[16];
        float[] output = new float[16];
        Matrix.setIdentityM(transform, 0);

        //Translate and scale the square
        Matrix.scaleM(transform, 0, mRatio, 1.0f, 1.0f);
        Matrix.translateM(transform, 0, mTranslate[0], mTranslate[1], 0.0f);
        Matrix.multiplyMM(output, 0, mMVPMatrix, 0, transform, 0);

        //Gradually reduce the alpha to base line
        if(color[3] > 0.2f && !squarePressed) {
            color[3] = color[3] - 0.08f;
        }
        super.draw(output);
    }

    public void onClick() {
        color[3] = 1.0f;
        squarePressed = true;
    }

    public void onRelease() {
        squarePressed = false;
    }
}
