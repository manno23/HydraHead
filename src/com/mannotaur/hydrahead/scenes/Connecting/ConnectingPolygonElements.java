package com.mannotaur.hydrahead.scenes.Connecting;

import android.util.Log;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.nio.*;

import static android.opengl.GLES20.*;
import static com.mannotaur.hydrahead.Constants.*;

/**
 * User: Jason Manning
 * Date: 24/05/14
 */
public class ConnectingPolygonElements {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOUR_COMPONENT_COUNT = 4;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOUR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private FloatBuffer verticiesBuffer;
    private ShortBuffer indiciesBuffer;
    private float[] verticies;
    private short[] indicies;

    private float h;
    private int positionPointer;
    private int colourPointer;

    public ConnectingPolygonElements() {
        verticies = new float[192];
        indicies = new short[]{
                0, 1, 2, 1, 3, 2,
                4, 5, 6, 5, 7, 6,
                8, 9, 10, 9, 11, 10,
                12, 13, 14, 13, 15, 14,
                //top left corner, a fan arrangement
                16, 17, 18,
                16, 18, 19,
                //bottom left
                22, 21, 20,
                23, 22, 20,
                //bottom right
                24, 25, 26,
                24, 26, 27,
                //top right
                30, 29, 28,
                31, 30, 28
        };
    }

    public void setDimensions(int width, int height) {
        h = (float)height / (float)width;
    }

    public void bindData(ShaderProgram program) {

        positionPointer = program.getAttributeLocation("a_Position");
        colourPointer = program.getAttributeLocation("a_Colour");

        verticiesBuffer = ByteBuffer
                .allocateDirect(verticies.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        indiciesBuffer = ByteBuffer
                .allocateDirect(indicies.length * BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indiciesBuffer
                .put(indicies)
                .position(0);

    }

    float l;
    float r;
    float t;
    float b;
    float a = 0.6f;
    float c = 0.0f;

    Cycle widthCycle1 = new Cycle(0.004f, 0.1f, 0.03f, 0.03f);
    Cycle widthCycle2 = new Cycle(0.001f, 0.1f, 0.03f, 0.03f);
    Cycle widthCycle3 = new Cycle(0.0026f, 0.1f, 0.03f, 0.03f);
    float width;

    public void draw() {

        width = (widthCycle1.value() + widthCycle2.value() + widthCycle3.value()) / 3.0f;
        l = -1.0f + width;
        r = 1.0f - width;
        t = h - width;
        b = -h + width;

        verticies = new float[]{
                // Edges
                -1f, t, c, c, c, a  //TL
                ,-1f, b, c, c, c, a //BL
                , l, t, c, c, c, c  //TR
                , l, b, c, c, c, c  //BR

                , l,  b, c, c, c, c //TL
                , l, -h, c, c, c, a //BL
                , r,  b, c, c, c, c //TR
                , r, -h, c, c, c, a //BR

                , r, t, c, c, c, c //TL
                , r, b, c, c, c, c //BL
                ,1f, t, c, c, c, a //TR
                ,1f, b, c, c, c, a //BR

                , l, h, c, c, c, a //TL
                , l, t, c, c, c, c //BL
                , r, h, c, c, c, a //TR
                , r, t, c, c, c, c //BR

                , l, t, c, c, c, c
                , l, h, c, c, c, a
                , -1f, h, c, c, c, a
                , -1f, t, c, c, c, a

                , l, b, c, c, c, c
                , l, -h, c, c, c, a
                , -1f, -h, c, c, c, a
                , -1f, b, c, c, c, a

                , r, b, c, c, c, c
                , r, -h, c, c, c, a
                , 1f, -h, c, c, c, a
                , 1f, b, c, c, c, a

                , r, t, c, c, c, c
                , r, h, c, c, c, a
                , 1f, h, c, c, c, a
                , 1f, t, c, c, c, a
        };

        verticiesBuffer.position(0);
        verticiesBuffer.put(verticies);

        verticiesBuffer.position(0);
        glVertexAttribPointer(positionPointer, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, verticiesBuffer);
        glEnableVertexAttribArray(positionPointer);

        verticiesBuffer.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(colourPointer, COLOUR_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, verticiesBuffer);
        glEnableVertexAttribArray(colourPointer);


        glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, indiciesBuffer);

        glDisableVertexAttribArray(positionPointer);
        glDisableVertexAttribArray(colourPointer);
    }

}
