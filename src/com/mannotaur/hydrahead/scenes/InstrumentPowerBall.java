package com.mannotaur.hydrahead.scenes;

import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.HydraConfig;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.objects.ParticleSystem;
import com.mannotaur.hydrahead.objects.Point;
import com.mannotaur.hydrahead.objects.Vector;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

/**
 * Created by jm on 8/03/17.
 */

public class InstrumentPowerBall implements Scene {

    private final String TAG = "InstrumentPowerBall";

    private final Networking mNetworkInterface;
    private final int mSceneID;
    private final ParticleSystem particleSystem;
    private final PowerBall powerBall;
    private ShaderProgram shaderProgram;

    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float width;
    private float height;
    private Grid grid;


    public InstrumentPowerBall(Networking networkInterface, int sceneID) {
        this.mNetworkInterface = networkInterface;
        this.mSceneID = sceneID;

        grid = new Grid(Color.rgb(255,255,255));

        particleSystem = new ParticleSystem(5000);

        Point centerPoint = new Point(1.0f, 1.0f, 0f);
        powerBall = new PowerBall(centerPoint, Color.rgb(25, 255, 25));
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

        this.width = width;
        this.height = height;
        Log.d(TAG, "W|H: "+width+" | "+height);

        // Create the viewprojection matrix on initialisation
        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, 0f, 2f*ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        grid.initialise(width, height);
    }

    @Override
    public void draw(long globalStartTime) {

        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        float r = (float)Math.sin((double)currentTime-80) * 0.2f;
        float g = (float)Math.sin((double)currentTime-30) * 0.2f;
        float b = (float)Math.sin((double)currentTime-120) * 0.2f;


        glClearColor(r, g, b, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


        powerBall.addParticles(particleSystem, currentTime, 5);

        shaderProgram.use();
        shaderProgram.setUniform("u_Time", currentTime);
        shaderProgram.setUniform("u_Matrix", viewProjectionMatrix);
        particleSystem.bindData(shaderProgram);
        particleSystem.draw();
    }

    /*
     * MotionEvent
     * |0-----------WIDTH|
     * |
     * |
     * |HEIGHT
     *
     *
     * OpenGL
     * |-1-----0-----1|
     * | 3.x
     * | |
     * | |
     * | |
     * | 0
     */
    @Override
    public void onTouch(MotionEvent event) {

        float x_vec = (event.getX() - width/2f) / (width/2f);
        float y_vec = -(event.getY() - height) / height * 3f;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "X: "+event.getX()+" Y: "+event.getY());
                Log.d(TAG, "Xv: "+x_vec+" Yv: "+y_vec);
                //mNetworkInterface.send( new byte[]{mNetworkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, x_vec_ratio, TOUCH_DOWN} );
                break;
            case MotionEvent.ACTION_MOVE:
                powerBall.setPosition(new Point(x_vec, y_vec, 0));
                //mNetworkInterface.send( new byte[]{mNetworkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, x_vec_ratio, TOUCH_DOWN} );
                break;
            case MotionEvent.ACTION_UP:
                //mNetworkInterface.send( new byte[]{mNetworkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, 0, TOUCH_UP} );
                break;
        }
    }

    @Override
    public void initialiseState(byte[] sceneState) { }

    @Override
    public int sceneID() { return mSceneID; }



    /*
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
                    //blueParticleShooter.setVelocity(fspeed);
                    greenParticleShooter.setVelocity(fspeed);
                    //redParticleShooter.setVelocity(fspeed);
                    break;

            }
        }
    }
    */


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

    }

    @Override
    public void handleMessage(byte[] data) {

    }


    /*
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
    */

    class PowerBall {

        private Point position;
        private int color;
        private float speedVariance;
        private float[] rotationMatrix = new float[16];
        private float[] directionVector = new float[4];
        private float[] resultVector = new float[4];

        private final Random random = new Random();

        PowerBall(Point entryPoint, int color) {
            this.position = entryPoint;
            this.color = color;
            directionVector = new float[]{0.2f, 0.2f, 0.2f, 0.2f};
        }

        public void addParticles(ParticleSystem particleSystem, float currentTime,
                                 int count) {
            for (int i = 0; i < count; i++) {
                setRotateEulerM(rotationMatrix, 0,
                        (random.nextFloat() - 0.5f) * 360,
                        (random.nextFloat() - 0.5f) * 360,
                        (random.nextFloat() - 0.5f) * 360
                );

                multiplyMV(resultVector, 0,
                        rotationMatrix, 0,
                        directionVector, 0);
                float speedAdjustment = 1f + random.nextFloat() * speedVariance;

                Vector thisDirection = new Vector(
                        resultVector[0] * speedAdjustment,
                        resultVector[1] * speedAdjustment,
                        resultVector[2] * speedAdjustment);
                particleSystem.addParticle(position, color, thisDirection, currentTime);
            }
        }

        public void setPosition(Point position) {
            this.position = position;
        }

    }

    class Grid {

        private final float LINE_WIDTH = 0.01f;  // Define width in terms of -1 <-> 1
        private final int HORIZONTAL_LINES = 4;
        private final int VERTICAL_LINES = 8;

        private int color;
        private float height;
        private float width;

        private ArrayList<GridLine> gridLines;

        public Grid(int rgb) {
            // initialise common color for all lines
            this.color = rgb;
        }


        public void initialise(float width, float height) {
            this.width = width;
            this.height = height;
            gridLines = new ArrayList<GridLine>();

            // Initialise the position of the grid lines
            for(float i = 0; i < width; i = i + width/HORIZONTAL_LINES) {
                gridLines.add(new HorizontalGridLine(i));
            }
            for(float i = 0; i < height; i = i + height/VERTICAL_LINES) {
                gridLines.add(new VerticalGridLine(i));
            }
        }

        public void draw(long time) {
            glLineWidth(LINE_WIDTH);
            for( GridLine line : gridLines )
                line.draw();
        }

    }

    private abstract class GridLine {

        public GridLine(float position) {

        }

        abstract void draw();
    }

    private class HorizontalGridLine extends GridLine {
        public HorizontalGridLine(float position) {
            super(position);
            // initialise the rectangle
        }

        @Override
        void draw() {

        }
    }

    /*
     * The vertical lines represent a row of 8 notes,
     * with each consecutive line changing the timbre or volume, whatever you wish.
     *
     */
    private class VerticalGridLine extends GridLine {
        public VerticalGridLine(float position) {
            super(position);
        }

        @Override
        void draw() {


        }
    }
}
