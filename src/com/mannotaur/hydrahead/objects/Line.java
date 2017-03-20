package com.mannotaur.hydrahead.objects;

import android.graphics.Color;
import android.util.Log;
import com.mannotaur.hydrahead.data.VertexArray;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import static android.opengl.GLES20.*;
import static com.mannotaur.hydrahead.Constants.BYTES_PER_FLOAT;


/**
 * Implementation for straight lines using a 6 triangles.
 * Using a 2D plane with rgba colours.
 *
 */
public class Line {

    private final static String TAG = "Line";

    protected final Point point1;
    protected final Point point2;
    protected float lineWidth;

    private VertexArray vertexBuffer;
    private int positionPointer;
    private int colourPointer;

    // number of coordinates per vertex in this array
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOUR_COMPONENT_COUNT = 3;
    private static final int COORDS_PER_VERTEX =
            POSITION_COMPONENT_COUNT + COLOUR_COMPONENT_COUNT;

    private final int vertexCount = 6;
    private final int vertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT;
    // Start off with 6 vertices, representing 2 triangles for a quad
    private float[] lineTriangleArray;
    private int colour;


    public Line(Point point1, Point point2) {
        this.point1 = point1;
        this.point2 = point2;
        lineWidth = 0.01f;

        lineTriangleArray = new float[COORDS_PER_VERTEX * 6];
        vertexBuffer = new VertexArray(lineTriangleArray);
        setVerts();
        setColour(1.0f, 1.0f, 1.0f);
    }

    public void bindData(ShaderProgram program) {
        positionPointer = program.getAttributeLocation("a_Position");
        colourPointer = program.getAttributeLocation("a_Color");
    }

    public void draw() {
        vertexBuffer.setVertexAttribPointer(0, positionPointer, POSITION_COMPONENT_COUNT, vertexStride);
        vertexBuffer.setVertexAttribPointer(POSITION_COMPONENT_COUNT, colourPointer, COLOUR_COMPONENT_COUNT, vertexStride);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glDisableVertexAttribArray(positionPointer);
        glDisableVertexAttribArray(colourPointer);
    }

    public void setVerts() {
        lineTriangleArray[0] = point1.x-lineWidth;
        lineTriangleArray[1] = point1.y;
        lineTriangleArray[5] = point2.x-lineWidth;
        lineTriangleArray[6] = point2.y;
        lineTriangleArray[10] = point1.x+lineWidth;
        lineTriangleArray[11] = point1.y;

        lineTriangleArray[15] = point2.x+lineWidth;
        lineTriangleArray[16] = point2.y;
        lineTriangleArray[20] = point1.x+lineWidth;
        lineTriangleArray[21] = point1.y;
        lineTriangleArray[25] = point2.x-lineWidth;
        lineTriangleArray[26] = point2.y;

        vertexBuffer.updateBuffer(lineTriangleArray, 0, 30);
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        setVerts();
    }

    public void setColour(float r, float g, float b) {

        lineTriangleArray[2] = r;
        lineTriangleArray[3] = g;
        lineTriangleArray[4] = b;

        lineTriangleArray[7] = r;
        lineTriangleArray[8] = g;
        lineTriangleArray[9] = b;

        lineTriangleArray[12] = r;
        lineTriangleArray[13] = g;
        lineTriangleArray[14] = b;

        lineTriangleArray[17] = r;
        lineTriangleArray[18] = g;
        lineTriangleArray[19] = b;

        lineTriangleArray[22] = r;
        lineTriangleArray[23] = g;
        lineTriangleArray[24] = b;

        lineTriangleArray[27] = r;
        lineTriangleArray[28] = g;
        lineTriangleArray[29] = b;

        vertexBuffer.updateBuffer(lineTriangleArray, 0, 30);
    }
}
