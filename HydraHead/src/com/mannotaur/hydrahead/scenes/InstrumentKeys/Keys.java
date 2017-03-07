package com.mannotaur.hydrahead.scenes.InstrumentKeys;

import android.view.MotionEvent;
import com.mannotaur.hydrahead.HydraConfig;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.mannotaur.hydrahead.Constants.*;
import static android.opengl.GLES20.*;

/**
 * User: Jason Manning
 * Date: 11/06/14
 */
public class Keys {

    private float NOTE_PRESSED_ALPHA = 1.0f;
    private float NOTE_INITIAL_ALPHA = 0.3f;
    private static final int POSITION_COMPONENTS = 2;
    private static final int COLOUR_COMPONENTS = 4;
    private static final int STRIDE =
            (POSITION_COMPONENTS + COLOUR_COMPONENTS) * BYTES_PER_FLOAT;

    private int mNoteCount;
    private int screenHeight;
    private int screenWidth;

    private ShaderProgram mShaderProgram;
    private FloatBuffer verticeBuffer;
    private float[] noteVertices;
    private float[] noteAlphas;

    public Keys() {
        mNoteCount = 0;
        setNoteCount(0);
    }

    public void addShader(ShaderProgram shaderProgram) {
        mShaderProgram = shaderProgram;
    }

    public void initialise(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    public void draw() {
        if (mNoteCount > 0) {
        mShaderProgram.use();
        int positionPointer = mShaderProgram.getAttributeLocation("position");
        int colourPointer = mShaderProgram.getAttributeLocation("colour");

        float n = (float)mNoteCount;
        float l = -1.0f;
        float r = 1.0f;
        int o = 0;
        for (int note=0; note < n; note++) {

            float re = (float)note/n;
            float gr = 1.0f - (float)note/n;
            float bl = 0.5f;
            float al = noteAlphas[note];

            float b = (float)note/n*2.0f - 1.0f;
            float t = ((float)note+1.0f)/n*2.0f - 1.0f;


            noteVertices[o++] = l;
            noteVertices[o++] = t;
            noteVertices[o++] = re;
            noteVertices[o++] = gr;
            noteVertices[o++] = bl;
            noteVertices[o++] = al;

            noteVertices[o++] = l;
            noteVertices[o++] = b;
            noteVertices[o++] = re;
            noteVertices[o++] = gr;
            noteVertices[o++] = bl;
            noteVertices[o++] = al;

            noteVertices[o++] = r;
            noteVertices[o++] = t;
            noteVertices[o++] = re;
            noteVertices[o++] = gr;
            noteVertices[o++] = bl;
            noteVertices[o++] = al;

            noteVertices[o++] = l;
            noteVertices[o++] = b;
            noteVertices[o++] = re;
            noteVertices[o++] = gr;
            noteVertices[o++] = bl;
            noteVertices[o++] = al;

            noteVertices[o++] = r;
            noteVertices[o++] = b;
            noteVertices[o++] = re;
            noteVertices[o++] = gr;
            noteVertices[o++] = bl;
            noteVertices[o++] = al;

            noteVertices[o++] = r;
            noteVertices[o++] = t;
            noteVertices[o++] = re;
            noteVertices[o++] = gr;
            noteVertices[o++] = bl;
            noteVertices[o++] = al;
        }
        verticeBuffer.position(0);
        verticeBuffer.put(noteVertices);
        verticeBuffer.position(0);
        glVertexAttribPointer(positionPointer, POSITION_COMPONENTS,
                GL_FLOAT, false, STRIDE, verticeBuffer);
        glEnableVertexAttribArray(positionPointer);
        verticeBuffer.position(POSITION_COMPONENTS);
        glVertexAttribPointer(colourPointer, COLOUR_COMPONENTS,
                GL_FLOAT, false, STRIDE, verticeBuffer);
        glEnableVertexAttribArray(colourPointer);


        int num_points = verticeBuffer.capacity() / 6;
        glDrawArrays(GL_TRIANGLES, 0, num_points);
        }
    }

    public void setNoteCount(int noteCount) {
        mNoteCount = noteCount;
        noteAlphas = new float[noteCount];
        for (int i=0; i<noteCount; i++) {
            noteAlphas[i] = NOTE_INITIAL_ALPHA;
        }
        noteVertices = new float[noteCount * 36];
        verticeBuffer = ByteBuffer
                .allocateDirect(noteVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        touchState.reset();
    }

    private class TouchState {

        private ArrayList<Pointer> pointers;

        public TouchState() {
            pointers = new ArrayList<Pointer>(3);
            pointers.add(new Pointer());
            pointers.add(new Pointer());
            pointers.add(new Pointer());
        }

        class Pointer {
            private int note_touched = -1;

            public void note_on(int y) {
                note_touched = (screenHeight - y) * mNoteCount / screenHeight;
                noteAlphas[note_touched] = NOTE_PRESSED_ALPHA;
                touchEventMessage = new byte[]{(byte)note_touched, (byte)1};
            }

            public void note_off() {
                if (note_touched >= 0 && note_touched < mNoteCount) {
                    noteAlphas[note_touched] = NOTE_INITIAL_ALPHA;
                    touchEventMessage = new byte[]{(byte)note_touched, (byte)0};
                }
                note_touched = -1;
            }

            public void reset() {
                note_off();
            }
        }

        public void note_on(int pointer, int y) {
            if (pointer >=0 && pointer <3)
                pointers.get(pointer).note_on(y);
        }

        public void note_off(int pointer) {
            if (pointer >=0 && pointer <3) {
                pointers.get(pointer).note_off();
            }
        }

        public void reset() {
            for (Pointer pointer : pointers) {
                pointer.reset();
            }
        }
    }

    byte[] touchEventMessage;
    TouchState touchState = new TouchState();
    public byte[] onTouch(MotionEvent event) {
        if (mNoteCount > 0) {
            int action = event.getActionMasked();
            int index = event.getActionIndex();
            int pointer_id = event.getPointerId(index);
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                touchState.note_on(pointer_id, (int) event.getY(index));
                return touchEventMessage;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                touchState.note_off(pointer_id);
                return touchEventMessage;
            }
        }
        return null;
    }
}
