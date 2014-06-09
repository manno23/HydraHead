package com.mannotaur.hydrahead.scenes;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

import static com.mannotaur.hydrahead.Constants.*;
import static android.opengl.GLES20.*;

/**
 * User: Jason Manning
 * Date: 29/05/14
 */
public class ElectronScene implements Scene {

    private final Networking networkInterface;
    private final int mSceneID;
    private ShaderProgram shaderProgram;

    private FloatBuffer quadVerticeBuffer;
    private FloatBuffer surfaceBuffer;
    float[] quad;
    float[] surfacePosition;

    private boolean touchEvent = false;
    private float h;
    private float w;

    public ElectronScene(Networking networkInterface, int sceneID) {
        this.networkInterface = networkInterface;
        mSceneID = sceneID;
    }

    @Override
    public void addShader(Context context) {
        shaderProgram = new ShaderProgram(context,
                R.raw.electrons_vert,
                R.raw.electrons_frag)
        .addAttribute("position")
        .addAttribute("surfacePosAttrib");
    }

    private int[] buffers = new int[2];
    @Override
    public void initialise(int width, int height) {

        quad = new float[]{
            -1.0f,  1.0f,
            -1.0f, -1.0f,
             1.0f,  1.0f,
            -1.0f, -1.0f,
             1.0f, -1.0f,
             1.0f,  1.0f
        };

        this.w = 0.5f;
        this.h = (float)height / (float)width / 2.0f;
        //this.maxDist = (float)Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2));
        surfacePosition = new float[]{
                -w, h,-w,-h, w, h,
                -w,-h, w,-h, w, h
        };


        quadVerticeBuffer = ByteBuffer
                .allocateDirect(quad.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        quadVerticeBuffer.put(quad)
                .position(0);

        surfaceBuffer = ByteBuffer
                .allocateDirect(surfacePosition.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        surfaceBuffer.put(surfacePosition)
                .position(0);

        glGenBuffers(2, buffers, 0);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, quadVerticeBuffer.capacity() * BYTES_PER_FLOAT,
                quadVerticeBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
        glBufferData(GL_ARRAY_BUFFER, surfaceBuffer.capacity() * BYTES_PER_FLOAT,
                surfaceBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    float time = 0.0f;
    ClickQueue queue = new ClickQueue(4);
    @Override
    public void draw(long globalStartTime) {

        queue.update();
        shaderProgram.setUniform("dists", queue.getDistances());
        shaderProgram.setUniform("colours", queue.getColours());

        time+=0.2f;
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shaderProgram.use();
        shaderProgram.setUniform("time", time);
        int positionPointer = shaderProgram.getAttributeLocation("position");
        int surfacePointer = shaderProgram.getAttributeLocation("surfacePosAttrib");

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        quadVerticeBuffer.position(0);
        glVertexAttribPointer(positionPointer, 2,
                GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(positionPointer);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
        surfaceBuffer.position(0);
        glVertexAttribPointer(surfacePointer, 2,
                GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(surfacePointer);

        glDrawArrays(GL_TRIANGLES, 0, 6);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

    @Override
    public void onTouch(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                queue.addClick();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void handleMessage(byte[] data) {

    }

    @Override
    public void initialiseState(byte[] sceneState) {

    }

    class ClickQueue {

        public int length;
        private float[] dists;
        private float[] colours;
        private float[] colourRange;
        private int distIndex;
        private int colourIndex;

        private float RED = 1.0f;
        private float GREEN = 1.0f;
        private float BLUE = 1.0f;

        public ClickQueue(int length) {
            this.length = length;
            this.dists = new float[length];
            for (int i=0; i<length; i++)
                dists[i] = -1.0f;
            this.distIndex = 0;
            this.colours = new float[]{RED, RED, RED, RED};
            this.colourRange = new float[]{RED, GREEN, BLUE};
            this.colourIndex = 0;
        }

        public void addClick() {
            Log.d("ControlSurface", distIndex+" "+colourIndex);
            dists[distIndex] = 0.2f;
            colours[distIndex] = colourRange[colourIndex];

            //Increment the index
            distIndex = (distIndex+1)%length;
            colourIndex = (colourIndex+1)%3;
        }

        public void update() {
            for (int i=0; i<4; i++) {
                if (dists[i] < 2.0f && dists[i] >= 0.0f)
                    dists[i] += 0.16;
                else
                    dists[i] = -1.0f;
            }
            Log.d("ControlSurface", dists[0]+" "+dists[1]+" "+dists[2]+" "+dists[3]);
        }

        public float[] getDistances() {
            return this.dists;
        }

        public float[] getColours() {
            return this.colours;
        }
    }
}
