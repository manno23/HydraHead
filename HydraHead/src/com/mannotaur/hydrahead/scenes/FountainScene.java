package com.mannotaur.hydrahead.scenes;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.HydraConfig;
import com.mannotaur.hydrahead.R;

import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.messages.RotationMessage;
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

    private final byte TOUCH_EVENT = 1;
    private final byte TOUCH_DOWN = 1;
    private final byte TOUCH_UP = 2;

    private ShaderProgram shaderProgram;
    private Networking mNetworkInterface;
    private int mSceneID;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] mvpMatrix;

    private boolean active_instrument_one = false;
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
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, 0f, 2f*ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

    }

    @Override
    public void draw(long globalStartTime) {

        if(back_white) {
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
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

        float x_vec = (event.getX() - 240.0f) / 240.0f;
        byte x_vec_ratio = (byte)x_vec;

        switch(event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                greenParticleShooter.updateDirection(x_vec);
                break;
        }
    }


    private final byte BACKGROUND_STATE = 0;
    private final byte FOUNTAIN_SPEED = 1;
    public void handleMessage(byte[] msg) {

        if (msg != null) {
            ByteBuffer bb = ByteBuffer.wrap(msg);
            bb.get();
            byte type = bb.get();

            switch (type) {
                case BACKGROUND_STATE:

                    byte backgroundValue = bb.get();
                    if (backgroundValue == 0)
                        back_white = true;
                    else if (backgroundValue == 1)
                        back_white = false;

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
    }


    /**
     * Synchronises the state of the client with the server upon initialisation.
     * @param sceneState a byte array allows for decoupling of the state information
     *                   from the Scene interface, allowing for the addition of new scenes.
     */
    public void initialiseState(byte[] sceneState) {
        ByteBuffer bb = ByteBuffer.wrap(sceneState);
        bb.get();
        bb.get();
        byte backgroundValue = bb.get();
        if (backgroundValue == 0)
            back_white = true;
        else if (backgroundValue == 1)
            back_white = false;

        byte fountainSpeedValue = bb.get();
        float fountainVelocity = (float)fountainSpeedValue / 100.0f + 0.2f;

        redParticleShooter.setVelocity(fountainVelocity);
        greenParticleShooter.setVelocity(fountainVelocity);
        blueParticleShooter.setVelocity(fountainVelocity);

    }
}