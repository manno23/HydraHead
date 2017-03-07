package com.mannotaur.hydrahead.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static android.opengl.GLES20.*;
import static com.mannotaur.hydrahead.Constants.BYTES_PER_FLOAT;

/**
 * User: Jason Manning
 * Date: 27/05/14
 */
public class VertexBufferObject {

    private final FloatBuffer verticiesBuffer;
    private final String TAG = "ControlSurface";

    public VertexBufferObject(float[] vertexArray) {
        verticiesBuffer = ByteBuffer
                .allocateDirect(vertexArray.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexArray);
        verticiesBuffer.position(0);
    }

    public void bindBuffer(int vertexBufferObject) {
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, verticiesBuffer.capacity(), verticiesBuffer, GL_STATIC_DRAW);
    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride) {
        verticiesBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(attributeLocation);
    }
}
