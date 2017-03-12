package com.mannotaur.hydrahead.objects;

import android.opengl.GLES20;
import com.mannotaur.hydrahead.data.VertexArray;


public class Line {

    private VertexArray vertexBuffer;

    protected int GlProgram;
    protected int positionHandle;
    protected int colorHandle;
    protected int mvpMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float lineCoords[] = {
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
    };

    private final int vertexCount = lineCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };

    public Line() {
        // initialize vertex byte buffer for shape coordinates
        vertexBuffer = new VertexArray(lineCoords);
    }

    public void setVerts(float v0, float v1, float v2, float v3, float v4, float v5) {
        lineCoords[0] = v0;
        lineCoords[1] = v1;
        lineCoords[2] = v2;
        lineCoords[3] = v3;
        lineCoords[4] = v4;
        lineCoords[5] = v5;
        vertexBuffer.updateBuffer(lineCoords, 0, 6);

        /*
        vertexBuffer.put(lineCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
        */
    }

    public void setColor(float red, float green, float blue, float alpha) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    public void draw() {
        // Add program to OpenGL ES environment
        //GLES20.glUseProgram(GlProgram);

        // get handle to vertex shader's vPosition member
        // positionHandle = GLES20.glGetAttribLocation(GlProgram, "vPosition");

        // Enable a handle to the triangle vertices
        // GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        vertexBuffer.setVertexAttribPointer(0, positionHandle, COORDS_PER_VERTEX, vertexStride);

        // get handle to fragment shader's vColor member
        // colorHandle = GLES20.glGetUniformLocation(GlProgram, "vColor");

        // Set color for drawing the triangle
        // GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        // mvpMatrixHandle = GLES20.glGetUniformLocation(GlProgram, "uMVPMatrix");
        //ArRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        // GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        // ArRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
