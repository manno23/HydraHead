package com.mannotaur.hydrahead.scenes.Connecting;

import static android.opengl.GLES20.*;

import android.content.Context;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.programs.ShaderProgram;
import com.mannotaur.hydrahead.util.TextureHelper;
import static com.mannotaur.hydrahead.Constants.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ConnectingTextures {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEX_COMPONENT_COUNT = 2;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + TEX_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private ShaderProgram program;
    private FloatBuffer verticiesBuffer;
    private float[] verticeArray;
    private int connectingTextTexture;
    private int positionPointer;
    private int texPointer;


    public ConnectingTextures(Context context) {
        verticeArray = new float[]
            {
              -0.5f,  0.125f, 0.0f, 0.0f,
              -0.5f, -0.125f, 0.0f, 1.0f,
               0.5f,  0.125f, 1.0f, 0.0f,
              -0.5f, -0.125f, 0.0f, 1.0f,
               0.5f, -0.125f, 1.0f, 1.0f,
               0.5f,  0.125f, 1.0f, 0.0f
            };
        connectingTextTexture = TextureHelper.loadTexture(context, R.drawable.connecting);

    }

    public void bindData(ShaderProgram program) {

        this.program = program;
        program.setTextureUniform("usTextureUnit", connectingTextTexture);
        positionPointer = program.getAttributeLocation("avPosition");
        texPointer = program.getAttributeLocation("avTextureCoordinates");

        verticiesBuffer = ByteBuffer
                .allocateDirect(verticeArray.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticiesBuffer.put(verticeArray)
                .position(0);

    }

    Cycle alphaCycle = new Cycle(0.008f, 0.8f, 0.6f, 0.8f);
    public void draw() {

        program.setUniform("ufAlpha", alphaCycle.value());

        // Draw title
        verticiesBuffer.position(0);
        glVertexAttribPointer(positionPointer, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, verticiesBuffer);
        glEnableVertexAttribArray(positionPointer);

        verticiesBuffer.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(texPointer, TEX_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, verticiesBuffer);
        glEnableVertexAttribArray(texPointer);

        glBindTexture(GL_TEXTURE_2D, connectingTextTexture);
        glDrawArrays(GL_TRIANGLES, 0, 6);


        glDisableVertexAttribArray(positionPointer);
        glDisableVertexAttribArray(texPointer);
    }
}
