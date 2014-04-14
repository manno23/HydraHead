package com.mannotaur.hydrahead.scenes;


import android.content.Context;
import com.example.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.Triangle;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

public class FlatScene implements Scene {

    private Networking mNetworkInterface;
    private ShaderProgram mShaderProgram;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private Triangle triangle;

    public FlatScene(Networking mNetworkInterface) {
        this.mNetworkInterface = mNetworkInterface;
    }

    @Override
    public void addShader(Context context) {
        mShaderProgram = new ShaderProgram(context,
                R.raw.tex_vertex_shader,
                R.raw.tex_fragment_shader)
                .addAttribute("avPosition")
                .addAttribute("avColour");
    }

    @Override
    public void initialise(int height, int width) {
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        triangle = new Triangle();
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
    public void onTouch(float x) {
    }
}
