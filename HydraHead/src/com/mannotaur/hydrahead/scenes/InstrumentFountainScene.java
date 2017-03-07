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
import com.mannotaur.hydrahead.messages.HydraMessage;
import com.mannotaur.hydrahead.objects.ParticleShooter;
import com.mannotaur.hydrahead.objects.ParticleSystem;
import com.mannotaur.hydrahead.objects.Point;
import com.mannotaur.hydrahead.objects.Vector;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.nio.ByteBuffer;

/**
 * User: Jason Manning
 * Date: 21/05/14
 */
public class InstrumentFountainScene implements Scene {

    private final byte TOUCH_EVENT = 1;
    private final byte ROTATE_EVENT = 2;

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
    public InstrumentFountainScene(Networking networkInterface, int sceneID) {
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

        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        float r = (float)Math.sin((double)currentTime-80) * 0.2f;
        float g = (float)Math.sin((double)currentTime-30) * 0.2f;
        float b = (float)Math.sin((double)currentTime-120) * 0.2f;


        glClearColor(r, g, b, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


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
            case MotionEvent.ACTION_DOWN:
                mNetworkInterface.send(
                        new byte[]{mNetworkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, x_vec_ratio, TOUCH_DOWN}
                );
                break;
            case MotionEvent.ACTION_MOVE:
                greenParticleShooter.updateDirection(x_vec);
                mNetworkInterface.send(
                    new byte[]{mNetworkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, x_vec_ratio, TOUCH_DOWN}
                );
                break;
            case MotionEvent.ACTION_UP:
                mNetworkInterface.send(
                        new byte[]{mNetworkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, 0, TOUCH_UP}
                );
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
                        Log.d("MIDICLIENT", "kick");
                    else if (backgroundValue == 1)
                        Log.d("MIDICLIENT", "kick");

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

    /**
     * This was originally creating packets that were too large, as it is unneccesary to send
     * the full 4x4 matrix of floats.
     * @param event the SensorEvent returned by the registered listener.
     */
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
                createMessageFromRotationData(mRotationMatrix);
                break;
        }
    }

    private float[] mValues = new float[3];
    private void createMessageFromRotationData(float[] rotationMatrix) {
        SensorManager.getOrientation(rotationMatrix, mValues);
        mNetworkInterface.send(new RotationMessage(mValues, mSceneID).byteArray());
    }

    @Override
    public void initialiseState(byte[] sceneState) { }

    @Override
    public int sceneID() {
        return mSceneID;
    }


    private class RotationMessage extends HydraMessage {

        private float[] mAngles;
        private byte ROTATE_MESSAGE_ID = 2;
        private byte mSceneID;

        public RotationMessage(float[] angles, int sceneID) {
            mAngles = angles;
            mSceneID = (byte) sceneID;
        }

        @Override
        public byte[] byteArray() {

            byte[] output = new byte[16];
            output[0] = Networking.INSTRUMENT_OUTPUT;
            output[1] = HydraConfig.LOCAL_IP[3]; // Client ID
            output[2] = ROTATE_MESSAGE_ID;
            // Map the array of 3 floats (12 bytes) to the backend of the message
            System.arraycopy(floatToByte(mAngles), 0, output, 3, 12);
            return output;
        }

    }

}


