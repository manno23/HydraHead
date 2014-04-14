package com.mannotaur.hydrahead.scenes;

import android.content.Context;
import com.example.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.TextureSquare;
import com.mannotaur.hydrahead.programs.ShaderProgram;
import com.mannotaur.hydrahead.util.TextureHelper;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

public class ConnectingScene implements Scene {

    private ShaderProgram shaderProgram;
    private Networking mNetworkInterface;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private TextureSquare texturedSquare;
    private int texture;

    public ConnectingScene(Networking mNetworkInterface) {
        this.mNetworkInterface = mNetworkInterface;
    }

    @Override
    public void addShader(Context context) {

        texturedSquare = new TextureSquare();

        shaderProgram = new ShaderProgram(context,
                R.raw.tex_vertex_shader,
                R.raw.tex_fragment_shader)
                .addAttribute("avPosition")
                .addAttribute("avTextureCoordinates");

        texture = TextureHelper.loadTexture(context,
                R.drawable.connecting);
    }

    @Override
    public void initialise(int width, int height) {

        // Create the viewprojection matrix on initialisation
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f);

    }

    @Override
    public void draw(long globalStartTime) {

        long time = System.nanoTime() / 100000000;
        float alpha = (float)Math.sin(time);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shaderProgram.use();
        shaderProgram.setUniform("umMVP", projectionMatrix);
        shaderProgram.setUniform("ufAlpha", alpha);
        shaderProgram.setTextureUniform("usTextureUnit", texture);
        texturedSquare.bindData(shaderProgram);
        texturedSquare.draw();

    }

    @Override
    public void onTouch(float x) {

    }
}
