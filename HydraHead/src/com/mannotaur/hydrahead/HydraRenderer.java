package com.mannotaur.hydrahead;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import com.example.R;
import com.mannotaur.hydrahead.util.LoggerConfig;
import com.mannotaur.hydrahead.util.ShaderHelper;
import com.mannotaur.hydrahead.util.TextResourceLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class HydraRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int COORD_COUNT = 2;
    private static final String AV_POSITION = "avPosition";
    private static final String AV_COLOUR = "avColour";
    private static final String UM_MVP = "umMVP";

    private float[] triangle_vertices = {
            0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };
    private float[] colours = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    private Context context;
    private float[] projectionMatrix;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colourBuffer;
    private int program;
    private int colourLocation;
    private int verticeLocation;
    private int mvpLocation;




    public HydraRenderer(Context context) {
        this.context = context;
        projectionMatrix = new float[16];

        vertexBuffer = ByteBuffer
                .allocateDirect(triangle_vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(triangle_vertices);
        vertexBuffer.position(0);

        colourBuffer = ByteBuffer
                .allocateDirect(colours.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colourBuffer.put(colours);
        colourBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        String vertexShaderSource = TextResourceLoader
                .readTextFile(context, R.raw.vertex_shader);
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);

        String fragmentShaderSource = TextResourceLoader
                .readTextFile(context, R.raw.fragment_shader);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }
        glUseProgram(program);

        colourLocation = glGetAttribLocation(program, AV_COLOUR);
        glVertexAttribPointer(colourLocation, 4, GL_FLOAT,
                false, 0, colourBuffer);
        glEnableVertexAttribArray(colourLocation);

        verticeLocation = glGetAttribLocation(program, AV_POSITION);
        glVertexAttribPointer(verticeLocation, COORD_COUNT, GL_FLOAT,
                false, 0, vertexBuffer);
        glEnableVertexAttribArray(verticeLocation);

        mvpLocation = glGetUniformLocation(program, UM_MVP);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniformMatrix4fv(mvpLocation, 1, false, projectionMatrix, 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);

        float ratio = (float)height / (float)width;
        orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f);
    }

}
