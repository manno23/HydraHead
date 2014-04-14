package com.mannotaur.hydrahead.objects;

import com.mannotaur.hydrahead.data.VertexArray;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import static android.opengl.GLES20.*;

public class Triangle {

    private static final int VERTEX_COUNT = 3; // As is traditional of a triangle
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOUR_COMPONENT_COUNT = 4;
    private static final int TOTAL_COMPONENET_COUNT =
            POSITION_COMPONENT_COUNT + COLOUR_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENET_COUNT * 4;

    private VertexArray mVertexArray;
    private VertexArray mColourArray;


    public Triangle() {
        float[] verticeArray = new float[]
               {  0.0f, 0.5f, 0.0f,
                -0.25f, 0.0f, 0.0f,
                 0.25f, 0.0f, 0.0f };
        float[] colourArray = new float[]
                { 1.0f, 0.0f, 0.0f, 1.0f,
                  0.0f, 1.0f, 0.0f, 1.0f,
                  0.0f, 0.0f, 1.0f, 1.0f };

        mVertexArray = new VertexArray(verticeArray);
        mColourArray = new VertexArray(colourArray);
    }

    public void bindData(ShaderProgram program) {
        mVertexArray.setVertexAttribPointer(0,
                program.getAttributeLocation("avPosition"),
                POSITION_COMPONENT_COUNT, 3 * 4);
        mColourArray.setVertexAttribPointer(0,
                program.getAttributeLocation("avColour"),
                COLOUR_COMPONENT_COUNT, 4 * 4);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLES, 0, 2);
    }
}
