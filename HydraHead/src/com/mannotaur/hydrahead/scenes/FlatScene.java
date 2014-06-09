package com.mannotaur.hydrahead.scenes;


import android.content.Context;
import android.hardware.SensorEvent;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.Triangle;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

public class FlatScene implements Scene {

    private Networking mNetworkInterface;
    private ShaderProgram mShaderProgram;
    private int mSceneID;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private Triangle triangle;

    public FlatScene(Networking mNetworkInterface, int sceneID) {
        this.mNetworkInterface = mNetworkInterface;
        this.mSceneID = sceneID;
    }

    @Override
    public void addShader(Context context) {
        mShaderProgram = new ShaderProgram(context,
                R.raw.connecting_textures_frag,
                R.raw.connecting_textures_frag)
                .addAttribute("avPosition")
                .addAttribute("avColour");

        triangle = new Triangle();
    }

    @Override
    public void initialise(int height, int width) {
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    @Override
    public void draw(long globalStartTime) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        mShaderProgram.use();
        mShaderProgram.setUniform("umMVP", viewProjectionMatrix);
        triangle.bindData(mShaderProgram);
        triangle.draw();
    }

    @Override
    public void onTouch(MotionEvent event) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    public void handleMessage(byte[] msg) {

    }


    /**
     * Synchronises the state of the client with the server upon initialisation.
     * @param sceneState a Bundle object allows for decoupling of the state information
     *                   from the Scene interface, allowing for the addition of new scenes.
     */
    public void initialiseState(byte[] sceneState) {};
}
