package com.mannotaur.hydrahead;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class HydraRenderer implements GLSurfaceView.Renderer {

    private static final String MIDICLIENT = "MidiClient";
    private InteractiveSquare   mTopLeftSquare;
    private InteractiveSquare   mTopRightSquare;
    private InteractiveSquare   mBottomLeftSquare;
    private InteractiveSquare   mBottomRightSquare;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    float BLUE[] = { 0.2f, 0.709803922f, 0.898039216f, 0.2f };
    float YELLOW[] = { 0.709803922f, 0.898039216f, 0.2f, 0.2f };
    float RED[] = { 0.709803922f, 0.2f, 0.898039216f, 0.2f };
    float GREEN[] = { 0.2f, 0.1f, 0.898039216f, 0.2f };


    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw square
        mTopLeftSquare.draw(mMVPMatrix);
        mTopRightSquare.draw(mMVPMatrix);
        mBottomLeftSquare.draw(mMVPMatrix);
        mBottomRightSquare.draw(mMVPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        mTopLeftSquare = new InteractiveSquare(BLUE, ratio, new float[]{0.5f, 0.5f});
        mTopRightSquare = new InteractiveSquare(YELLOW, ratio, new float[]{-0.5f, 0.5f});
        mBottomLeftSquare = new InteractiveSquare(RED, ratio, new float[]{0.5f, -0.5f});
        mBottomRightSquare = new InteractiveSquare(GREEN, ratio, new float[]{-0.5f, -0.5f});
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(MIDICLIENT, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void squarePressed(int square) {
        switch(square) {
            case 1:
                mTopLeftSquare.onClick();
                break;
            case 2:
                mTopRightSquare.onClick();
                break;
            case 3:
                mBottomLeftSquare.onClick();
                break;
            case 4:
                mBottomRightSquare.onClick();
                break;
        }
    }

}
