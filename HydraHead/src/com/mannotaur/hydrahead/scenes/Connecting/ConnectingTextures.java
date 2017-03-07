package com.mannotaur.hydrahead.scenes.Connecting;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.util.Log;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.objects.SpriteTexture;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import static com.mannotaur.hydrahead.Constants.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ConnectingTextures {

    private static final byte STATE_SEARCHING = 1;
    private static final byte STATE_SEARCH_FAILED = 2;
    private static final byte STATE_CONNECTING = 3;
    private static final byte STATE_CONNECTED = 4;
    private static final byte STATE_CONNECTION_FAILED = 5;
    private static final byte STATE_CONNECTING_FADE_OUT = 6;

    private final byte NUMBER_OF_TEXTURES = 4;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEX_COMPONENT_COUNT = 2;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + TEX_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final String TAG = "SpriteTexture";

    private ShaderProgram program;
    private FloatBuffer verticesBuffer;
    private SpriteTexture searchingTextTexture;
    private SpriteTexture searchFailedTextTexture;
    private SpriteTexture connectingTextTexture;
    private SpriteTexture hydraHeadTexture;
    private int positionPointer;
    private int texPointer;

    private byte currentState;

    public ConnectingTextures(Context context, byte connectionState) {

        handleMessage(connectionState);
        int[] textureObjectIDs = new int[NUMBER_OF_TEXTURES];
        glGenTextures(NUMBER_OF_TEXTURES, textureObjectIDs, 0);

        if (textureObjectIDs[0] == 0) {
            Log.w(TAG, "Could not generate a new OpenGL texture object.");
        }
        try {
            searchingTextTexture = new SpriteTexture(context, R.drawable.searching_text, 1.0f,
                    0f, 0.0f, textureObjectIDs[0]);
            connectingTextTexture = new SpriteTexture(context, R.drawable.connecting_text, 1.0f,
                    0f, 0.0f, textureObjectIDs[1]);
            searchFailedTextTexture = new SpriteTexture(context, R.drawable.search_failed_text, 1.0f,
                    0f, 0.0f, textureObjectIDs[2]);
            hydraHeadTexture = new SpriteTexture(context, R.drawable.hydra_head, 2.5f,
                    0f, 0.0f, textureObjectIDs[3]);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            glDeleteTextures(1, textureObjectIDs, 0);
        }

    }

    public void bindData(ShaderProgram program) {

        this.program = program;

        program.setUniform("ufAlpha", connectingTextTexture.getAlpha());
        program.setTextureUniform("usTextureUnit", connectingTextTexture.getTextureObjectID());

        positionPointer = program.getAttributeLocation("avPosition");
        texPointer = program.getAttributeLocation("avTextureCoordinates");
        verticesBuffer = ByteBuffer
                .allocateDirect(connectingTextTexture.getVertices().length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticesBuffer.put(connectingTextTexture.getVertices())
                .position(0);

    }

    Cycle alphaCycle = new Cycle(0.008f, 0.8f, 0.6f, 0.8f);
    float alpha = 0f;
    public void draw() {

        connectingTextTexture.setAlpha(alphaCycle.value());

        // Draw title
        verticesBuffer.position(0);
        glVertexAttribPointer(positionPointer, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, verticesBuffer);
        glEnableVertexAttribArray(positionPointer);

        verticesBuffer.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(texPointer, TEX_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, verticesBuffer);
        glEnableVertexAttribArray(texPointer);

        switch (currentState) {

            case STATE_SEARCHING:
                program.setUniform("ufAlpha", alphaCycle.value());
                glBindTexture(GL_TEXTURE_2D, searchingTextTexture.getTextureObjectID());
                break;

            case STATE_CONNECTING:
                program.setUniform("ufAlpha", alphaCycle.value());
                glBindTexture(GL_TEXTURE_2D, connectingTextTexture.getTextureObjectID());
                break;

            case STATE_CONNECTING_FADE_OUT:
                if (alpha > 0f)
                    alpha -= 0.01f;
                else {
                    currentState = STATE_CONNECTED;
                    verticesBuffer = ByteBuffer
                            .allocateDirect(hydraHeadTexture.getVertices().length * BYTES_PER_FLOAT)
                            .order(ByteOrder.nativeOrder())
                            .asFloatBuffer();
                    verticesBuffer.put(hydraHeadTexture.getVertices())
                            .position(0);
                }
                program.setUniform("ufAlpha", alpha);
                glBindTexture(GL_TEXTURE_2D, connectingTextTexture.getTextureObjectID());
                break;

            case STATE_CONNECTED:
                if (alpha < 1f)
                    alpha += 0.01f;
                program.setUniform("ufAlpha", alpha);
                glBindTexture(GL_TEXTURE_2D, hydraHeadTexture.getTextureObjectID());
                break;

            case STATE_SEARCH_FAILED:
            case STATE_CONNECTION_FAILED:
                alpha = 1.0f;
                glBindTexture(GL_TEXTURE_2D, searchFailedTextTexture.getTextureObjectID());
                break;
        }

        glDrawArrays(GL_TRIANGLES, 0, 6);

        glDisableVertexAttribArray(positionPointer);
        glDisableVertexAttribArray(texPointer);
    }

    public void handleMessage(byte msgType) {
        switch (msgType) {
            case ConnectingScene.SEARCHING:
                currentState = STATE_SEARCHING;
                break;

            case ConnectingScene.SEARCH_UNSUCCESSFUL:
                program.setUniform("ufAlpha", alpha);
                verticesBuffer = ByteBuffer
                        .allocateDirect(searchFailedTextTexture.getVertices().length * BYTES_PER_FLOAT)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                verticesBuffer.put(searchFailedTextTexture.getVertices())
                        .position(0);
                currentState = STATE_SEARCH_FAILED;
                break;

            case ConnectingScene.CONNECTING:
                currentState = STATE_CONNECTING;
                break;

            case ConnectingScene.CONNECTED_AND_WAITING:
                currentState = STATE_CONNECTING_FADE_OUT;
                alpha = alphaCycle.value();
                break;

            case ConnectingScene.CONNECTION_TIMED_OUT:
                program.setUniform("ufAlpha", alpha);
                verticesBuffer = ByteBuffer
                        .allocateDirect(searchFailedTextTexture.getVertices().length * BYTES_PER_FLOAT)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                verticesBuffer.put(searchFailedTextTexture.getVertices())
                        .position(0);
                currentState = STATE_CONNECTION_FAILED;
                break;
        }
    }

}
