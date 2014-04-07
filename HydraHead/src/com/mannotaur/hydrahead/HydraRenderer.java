package com.mannotaur.hydrahead;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLSurfaceView;
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
    private static final String UV_COLOUR = "uvColour";

    private Context context;
    private float[] triangle_vertices = {
            0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f
    };
    private final FloatBuffer vertexBuffer;
    private int program;
    private int colourHandle;
    private int verticeHandle;


    public HydraRenderer(Context context) {
        this.context = context;
        vertexBuffer = ByteBuffer
                .allocateDirect(triangle_vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(triangle_vertices);
        vertexBuffer.position(0);

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

        colourHandle = glGetUniformLocation(program, UV_COLOUR);

        verticeHandle = glGetAttribLocation(program, AV_POSITION);
        glVertexAttribPointer(verticeHandle, COORD_COUNT, GL_FLOAT,
                false, 0, vertexBuffer);
        glEnableVertexAttribArray(verticeHandle);

    }

    @Override
    public void onDrawFrame(GL10 unused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniform4f(colourHandle, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);
    }

}
