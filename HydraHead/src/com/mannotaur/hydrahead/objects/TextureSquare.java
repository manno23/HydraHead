package com.mannotaur.hydrahead.objects;

import static android.opengl.GLES20.*;
import com.mannotaur.hydrahead.data.VertexArray;
import com.mannotaur.hydrahead.programs.ShaderProgram;

public class TextureSquare {

    private static final int VERTEX_COUNT = 6;
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    private VertexArray mVertexArray;
    private VertexArray mTextureCoordArray;


    public TextureSquare() {
        float[] verticeArray = new float[]
                { -0.5f,  0.125f, //TL
                  -0.5f, -0.125f, //BL
                   0.5f,  0.125f, //TR
                  -0.5f, -0.125f, //BL
                   0.5f, -0.125f, //BR
                   0.5f,  0.125f }; //TR
        float[] texCoordArray = new float[]
                { 0.0f, 0.0f,
                  0.0f, 1.0f,
                  1.0f, 0.0f,
                  0.0f, 1.0f,
                  1.0f, 1.0f,
                  1.0f, 0.0f };

        mVertexArray = new VertexArray(verticeArray);
        mTextureCoordArray = new VertexArray(texCoordArray);
    }

    public void bindData(ShaderProgram program) {
        mVertexArray.setVertexAttribPointer(0,
                program.getAttributeLocation("avPosition"),
                POSITION_COMPONENT_COUNT, 0);
        mTextureCoordArray.setVertexAttribPointer(0,
                program.getAttributeLocation("avTextureCoordinates"),
                TEXTURE_COMPONENT_COUNT, 0);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);
    }
}
