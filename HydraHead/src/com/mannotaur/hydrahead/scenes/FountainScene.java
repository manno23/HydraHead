package com.mannotaur.hydrahead.scenes;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import android.content.Context;
import android.graphics.Color;
import com.example.R;

import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.ParticleShooter;
import com.mannotaur.hydrahead.objects.ParticleSystem;
import com.mannotaur.hydrahead.objects.Point;
import com.mannotaur.hydrahead.objects.Vector;
import com.mannotaur.hydrahead.programs.ShaderProgram;

public class FountainScene implements Scene {

    private ShaderProgram shaderProgram;
    private Networking mNetworkInterface;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private final float angleVarianceinDegrees = 7f;
    private final float speedVariance = 1f;
    private ParticleSystem particleSystem;
    private ParticleShooter blueParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter redParticleShooter;

    public FountainScene(Networking mNetworkInterface) {
        this.mNetworkInterface = mNetworkInterface;
    }

    @Override
    public void addShader(Context context) {
        shaderProgram = new ShaderProgram(context,
                R.raw.particle_vertex_shader,
                R.raw.particle_fragment_shader)
                .addAttribute("a_Position")
                .addAttribute("a_Colour")
                .addAttribute("a_DirectionVector")
                .addAttribute("a_ParticleStartTime");

    }

    @Override
    public void initialise(int width, int height) {
        // Create the viewprojection matrix on initialisation
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, 0f, 2f*ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        particleSystem = new ParticleSystem(10000);

        final Vector particleDirection = new Vector(0f, 0.5f, 0f);

        redParticleShooter = new ParticleShooter(
                new Point(-1f, 0f, 0f),
                particleDirection,
                Color.rgb(255, 50, 5),
                angleVarianceinDegrees,
                speedVariance
        );
        greenParticleShooter = new ParticleShooter(
                new Point(0f, 0f, 0f),
                particleDirection,
                Color.rgb(25, 255, 25),
                angleVarianceinDegrees,
                speedVariance
        );
        blueParticleShooter = new ParticleShooter(
                new Point(1f, 0f, 0f),
                particleDirection,
                Color.rgb(5, 50, 255),
                angleVarianceinDegrees,
                speedVariance
        );

    }
    @Override
    public void draw(long globalStartTime) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;

        redParticleShooter.addParticles(particleSystem, currentTime, 5);
        greenParticleShooter.addParticles(particleSystem, currentTime, 5);
        blueParticleShooter.addParticles(particleSystem, currentTime, 5);

        shaderProgram.use();
        shaderProgram.setUniform("u_Time", currentTime);
        shaderProgram.setUniform("u_Matrix", viewProjectionMatrix);
        particleSystem.bindData(shaderProgram);
        particleSystem.draw();
    }

    public void onTouch(float x) {
        float x_vec = (x - 240.0f) / 240.0f;
        greenParticleShooter.updateDirection(x_vec);
    }


}
