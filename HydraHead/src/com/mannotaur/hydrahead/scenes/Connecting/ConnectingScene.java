package com.mannotaur.hydrahead.scenes.Connecting;

import android.content.Context;
import android.hardware.SensorEvent;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.programs.ShaderProgram;
import com.mannotaur.hydrahead.scenes.Scene;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

public class ConnectingScene implements Scene {

    /* Connecting process Message Types */
    public static final byte SEARCHING = 1;
    public static final byte SEARCH_UNSUCCESSFUL = 2;
    public static final byte CONNECTING = 3;
    public static final byte CONNECTION_TIMED_OUT = 4;
    public static final byte CONNECTED_AND_WAITING = 5;


    private final int mSceneID;
    private ShaderProgram textureShaderProgram;
    private ShaderProgram polygonShaderProgram;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private ConnectingPolygonElements polygons;
    private ConnectingTextures textures;
    private byte connectionState;
    private float[] borderColour;

    public ConnectingScene(Networking mNetworkInterface, int sceneID) {
        mSceneID = sceneID;
        connectionState = SEARCHING;
        borderColour = new float[]{0.5f, 0.1f, 0.3f, 1.0f};
    }

    @Override
    public void addShader(Context context) {

        polygons = new ConnectingPolygonElements();
        polygonShaderProgram = new ShaderProgram(context,
                R.raw.connecting_polygons_vert,
                R.raw.connecting_polygons_frag)
                .addAttribute("a_Position")
                .addAttribute("a_Colour");
        polygons.bindData(polygonShaderProgram);

        textures = new ConnectingTextures(context, connectionState);
        textureShaderProgram = new ShaderProgram(context,
                R.raw.connecting_textures_vert,
                R.raw.connecting_textures_frag)
                .addAttribute("avPosition")
                .addAttribute("avTextureCoordinates");
        textures.bindData(textureShaderProgram);

    }

    @Override
    public void initialise(int width, int height) {
        // Create the viewprojection matrix on initialisation
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
        glEnable(GL_BLEND);

        polygons.setDimensions(width, height);
    }


    Cycle colourCycleR = new Cycle(0.017f, 1.0f, 0.2f, (float)Math.random());
    Cycle colourCycleG = new Cycle(0.011f, 1.0f, 0.2f, (float)Math.random());
    Cycle colourCycleB = new Cycle(0.007f, 1.0f, 0.2f, (float)Math.random());
    @Override
    public void draw(long globaltime) {

        borderColour[0] = colourCycleR.value();
        borderColour[1] = colourCycleG.value();
        borderColour[2] = colourCycleB.value();


        glClearColor(0f, 0f, 0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        textureShaderProgram.use();
        textureShaderProgram.setUniform("umMVP", projectionMatrix);
        textures.draw();

        polygonShaderProgram.use();
        polygonShaderProgram.setUniform("umMVP", projectionMatrix);
        polygonShaderProgram.setUniform("u_Colour", borderColour);
        polygons.draw();

    }

    @Override
    public void onTouch(MotionEvent event) {}

    @Override
    public void onSensorChanged(SensorEvent event) {}

    @Override
    public void handleMessage(byte[] msg) {
        byte msgType = msg[0];
        connectionState = msgType;
        if (textures != null)
            textures.handleMessage(msgType);
    }

    @Override
    public void initialiseState(byte[] sceneState){ };

    @Override
    public int sceneID() {
        return mSceneID;
    }

}
