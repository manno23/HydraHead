package com.mannotaur.hydrahead.scenes;

import android.graphics.Shader;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.*;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.FloatBuffer;
import java.util.Random;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;


public class InstrumentPowerBall implements Scene {

    private final String TAG = "InstrumentPowerBall";

    private final int mSceneID;
    private final Networking networkInterface;
    private final ParticleSystem particleSystem;
    private PowerBall powerBall;
    private Grid grid;
    private ShaderProgram shaderProgram;
    private ShaderProgram lineShaderProgram;

    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float width;
    private float height;
    private float ratio;


    public InstrumentPowerBall(Networking networkInterface, int sceneID) {
        this.networkInterface = networkInterface;
        this.mSceneID = sceneID;

        particleSystem = new ParticleSystem(5000);

        powerBall = new PowerBall(Color.rgb(25, 255, 25));
        grid = new Grid(Color.rgb(255, 255, 255));
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

        lineShaderProgram = new ShaderProgram(context,
                R.raw.powerball_vert,
                R.raw.powerball_frag)
                .addAttribute("a_Position")
                .addAttribute("a_Color")
                .addAttribute("a_lineWidth");
    }

    @Override
    public void initialise(int width, int height) {
        this.width = width;
        this.height = height;
        this.ratio = (float)height / (float)width;
        Log.d(TAG, "W|H: "+width+" | "+height);

        powerBall.setPosition(new Point(0f, ratio, 0f));

        grid.initialise(2f, ratio*2f);

        // Create the viewprojection matrix on initialisation
        orthoM(projectionMatrix, 0, -1f, 1f, 0f, 2f*ratio, -1f, 1f);
        setIdentityM(viewMatrix, 0);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    @Override
    public void draw(long globalStartTime) {

        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        /*
        float r = (float)Math.sin((double)currentTime-80) * 0.2f;
        float g = (float)Math.sin((double)currentTime-30) * 0.2f;
        float b = (float)Math.sin((double)currentTime-120) * 0.2f;
        */


        glClearColor(0f, 0f, 0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        lineShaderProgram.use();
        lineShaderProgram.setUniform("u_MVPMatrix", viewProjectionMatrix);
        grid.bindData(lineShaderProgram);
        grid.draw(globalStartTime);

        powerBall.addParticles(particleSystem, currentTime, 5);

        shaderProgram.use();
        shaderProgram.setUniform("u_Time", currentTime);
        shaderProgram.setUniform("u_Matrix", viewProjectionMatrix);
        particleSystem.bindData(shaderProgram);
        particleSystem.draw();

    }

    @Override
    public void onTouch(MotionEvent event) {

        float x_pos = (event.getX() - width/2f) / (width/2f);
        float y_pos = -(event.getY() - height) / height * (ratio * 2f);
        final Point point = new Point(x_pos, y_pos, 0);

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "X: "+event.getX()+" Y: "+event.getY());
                Log.d(TAG, "Xv: "+x_pos+" Yv: "+y_pos);
                //networkInterface.send( new byte[]{networkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, x_vec_ratio, TOUCH_DOWN} );
                break;
            case MotionEvent.ACTION_MOVE:
                powerBall.setPosition(point);
                grid.handleBallPosition(point);
                //networkInterface.send( new byte[]{networkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, x_vec_ratio, TOUCH_DOWN} );
                break;
            case MotionEvent.ACTION_UP:
                grid.handleBallRelease();
                //networkInterface.send( new byte[]{networkInterface.INSTRUMENT_OUTPUT, HydraConfig.LOCAL_IP[3], TOUCH_EVENT, 0, TOUCH_UP} );
                break;
        }
    }

    @Override
    public void initialiseState(byte[] sceneState) { }

    @Override
    public int sceneID() { return mSceneID; }


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


    private class PowerBall {

        private Point position;
        private int color;
        private float speedVariance;

        // Transformation matricies that are applied to the particles.
        private float[] rotationMatrix = new float[16];
        private float[] directionVector = new float[4];
        private float[] resultVector = new float[4];

        private final Random random = new Random();

        PowerBall(int color) {
            this.position = new Point(0f, 0f, 0f);
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


}
