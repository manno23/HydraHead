package com.mannotaur.hydrahead.scenes.Connecting;

import android.content.Context;
import android.hardware.SensorEvent;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.programs.ShaderProgram;
import com.mannotaur.hydrahead.scenes.Scene;
import com.mannotaur.hydrahead.util.TextureHelper;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;
import static java.lang.Math.random;

public class ConnectingScene implements Scene {

    public static byte MSG_CONNECTION_TIMED_OUT = 1;

    private ShaderProgram textureShaderProgram;
    private ShaderProgram polygonShaderProgram;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private ConnectingPolygonElements polygons;
    private ConnectingTextures textures;
    private float[] borderColour;

    public ConnectingScene(Networking mNetworkInterface) {
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

        textures = new ConnectingTextures(context);
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
    public void draw(long globalStartTime) {

        borderColour[0] = colourCycleR.value();
        borderColour[1] = colourCycleG.value();
        borderColour[2] = colourCycleB.value();


        glClearColor(0f, 0f, 0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        polygonShaderProgram.use();
        polygonShaderProgram.setUniform("umMVP", projectionMatrix);
        polygonShaderProgram.setUniform("u_Colour", borderColour);
        polygons.draw();

        textureShaderProgram.use();
        textureShaderProgram.setUniform("umMVP", projectionMatrix);
        textures.draw();
    }

    @Override
    public void onTouch(MotionEvent event) {}

    @Override
    public void onSensorChanged(SensorEvent event) {}

    public void handleMessage(byte[] msg) {
        // Here we can alter the seen to signal the connection has dropped
        if (msg[0] == MSG_CONNECTION_TIMED_OUT) {
            Log.d("ControlSurface", "Remove texture");
        }
    }

    /**
     * Synchronises the state of the client with the server upon initialisation.
     * @param sceneState a Bundle object allows for decoupling of the state information
     *                   from the Scene interface, allowing for the addition of new scenes.
     */
    public void initialiseState(byte[] sceneState){};
}
