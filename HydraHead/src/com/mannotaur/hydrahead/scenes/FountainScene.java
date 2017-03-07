package com.mannotaur.hydrahead.scenes;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.R;

import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.ParticleShooter;
import com.mannotaur.hydrahead.objects.ParticleSystem;
import com.mannotaur.hydrahead.objects.Point;
import com.mannotaur.hydrahead.objects.Vector;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.nio.ByteBuffer;

/**
 * Consists of 3 particle fountains. Makes use of a Particle System <br>
 * to give realistic particle effects.
 *
 * Scene Policy
 *
 * Server -> Client
 * Initialisation: 0/1 - back_white = False/True
 *                 [0 - 127] - Sets fountain velocity
 *
 * Update: 0 0 - back_white = True
 *         0 1 - back_white = False
 *         1 [0-127] - Sets fountain velocity
 *
 *
 * Client -> Server
 * Event: 1 (16)[float]:Rotation matrix - forward rotation raises volume
 *        2 [0 - 100]:Horizontal screen movement ratio - pitch value (within server specified range)
 *
 */
public class FountainScene implements Scene {


    private static final String TAG = "FountainScene";
    private ShaderProgram shaderProgram;
    private final Networking mNetworkInterface;
    public final int mSceneID;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private float mScreenWidth = 0.0f;
    private boolean back_white = true;

    private final float angleVarianceinDegrees = 7f;
    private final float speedVariance = 1f;
    private ParticleSystem particleSystem;
    private ParticleShooter blueParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter redParticleShooter;


    /**
     * Main constructor.
     * @param networkInterface the applications network interface.
     * @param sceneID an assigned, unique ID.
     */
    public FountainScene(Networking networkInterface, int sceneID) {
        this.mNetworkInterface = networkInterface;
        this.mSceneID = sceneID;

        particleSystem = new ParticleSystem(5000);

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
        mScreenWidth = width;
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, 0f, 2f*ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

    }

    @Override
    public void draw(long globalStartTime) {

        if(back_white) {
           glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        } else {
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
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


    @Override
    public void onTouch(MotionEvent event) {
        float x_pos = (event.getX() - mScreenWidth/2f) / mScreenWidth/2f;
        switch(event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                greenParticleShooter.updateDirection(x_pos);
                break;
        }
    }

    private final byte BACKGROUND_STATE = 0;
    private final byte FOUNTAIN_SPEED = 1;
    @Override
    public void handleMessage(byte[] msg) {

        if (msg != null) {
            ByteBuffer bb = ByteBuffer.wrap(msg);
            byte type = bb.get();

            switch (type) {
                case BACKGROUND_STATE:

                    byte backgroundValue = bb.get();
                    if (backgroundValue == 0) {
                        back_white = true;
                    } else if (backgroundValue == 1) {
                        back_white = false;
                    }

                    break;

                case FOUNTAIN_SPEED:

                    byte fountainSpeedValue = bb.get();
                    float fspeed = ((float)fountainSpeedValue / 100.0f) + 0.2f;
                    blueParticleShooter.setVelocity(fspeed);
                    greenParticleShooter.setVelocity(fspeed);
                    redParticleShooter.setVelocity(fspeed);
                    break;

            }
        }
    }

    private float[] mInitialOrientation = null;
    private float[] mRotationMatrix = new float[16];
    private float[] mOutputMatrix = new float[16];
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                if(mInitialOrientation == null) {
                    // Reverse the rotation
                    mInitialOrientation = new float[16];
                    Matrix.invertM(mInitialOrientation, 0, mRotationMatrix, 0);
                }

                Matrix.multiplyMM(mOutputMatrix, 0, mRotationMatrix, 0, mInitialOrientation, 0);
                Matrix.invertM(mRotationMatrix, 0, mOutputMatrix, 0);
                mNetworkInterface.send(new RotationMessage(mRotationMatrix, mSceneID).byteArray());
                break;
        }
        */
    }


    @Override
    public void initialiseState(byte[] sceneState) {
        ByteBuffer bb = ByteBuffer.wrap(sceneState);
        bb.get();
        bb.get();
        byte backgroundValue = bb.get();
        Log.d(TAG, "FountainScene initialisation");
        if (backgroundValue == 0)
            back_white = true;
        else if (backgroundValue == 1)
            back_white = false;

        byte fountainSpeedValue = bb.get();
        float fountainVelocity = (float)fountainSpeedValue / 100.0f + 0.2f;
        Log.d(TAG, "background:" + backgroundValue + " fountain speed:" + fountainVelocity);

        redParticleShooter.setVelocity(fountainVelocity);
        greenParticleShooter.setVelocity(fountainVelocity);
        blueParticleShooter.setVelocity(fountainVelocity);
    }

    @Override
    public int sceneID() {
        return mSceneID;
    }
}
