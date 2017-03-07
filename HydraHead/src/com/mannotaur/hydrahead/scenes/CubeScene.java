package com.mannotaur.hydrahead.scenes;

import android.content.Context;
import android.hardware.SensorEvent;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.objects.Cube;
import com.mannotaur.hydrahead.programs.ShaderProgram;
import com.mannotaur.hydrahead.util.TextureHelper;

import static android.opengl.GLES20.*;

/**
 * User: Jason Manning
 * Date: 14/04/14
 */
public class CubeScene implements Scene {

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] MVPMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];

    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    private ShaderProgram cubeShaderProgram;
    private ShaderProgram lightShaderProgram;
    private Cube cube;
    private int textureHandle;

    private final float[] mAccumulatedRotation = new float[16];
    private final float[] mCurrentRotation = new float[16];
    private float[] mTempMatrix = new float[16];

    public volatile float mDeltaX;
    public volatile float mDeltaY;
    private int mSceneID;

    public CubeScene(int sceneID) {
        mSceneID = sceneID;
    }

    @Override
    public void addShader(Context context) {
        cubeShaderProgram = new ShaderProgram(context,
                R.raw.cube_vertex_shader,
                R.raw.cube_fragment_shader)
                .addAttribute("a_Position")
                .addAttribute("a_Normal")
                .addAttribute("a_TexCoordinate");
        lightShaderProgram = new ShaderProgram(context,
                R.raw.point_vertex_shader,
                R.raw.point_fragment_shader)
                .addAttribute("a_Position");


        textureHandle = TextureHelper.loadTexture(context,R.drawable.glass_cyan);
        glGenerateMipmap(textureHandle);
        cube = new Cube();
    }

    @Override
    public void initialise(int width, int height) {
        float ratio = (float)width / (float)height;
        Matrix.setLookAtM(viewMatrix, 0,
                0.0f, 0.0f, -0.5f,
                0.0f, 0.0f, -5.0f,
                0.0f, 1.0f, 0.0f );
        Matrix.frustumM(projectionMatrix, 0,
                -ratio, ratio,
                -1.0f, 1.0f,
                 1.0f, 1000.0f);

        Matrix.setIdentityM(mAccumulatedRotation, 0);
    }

    @Override
    public void draw(long globalStartTime) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int)time);


        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -2.0f);
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.5f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mLightModelMatrix, 0, mLightPosInWorldSpace, 0);


        // Drawing the textured cube
        cubeShaderProgram.use();
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.8f, -3.5f);

        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
        mDeltaX = 0.0f;
        mDeltaY = 0.0f;

        Matrix.multiplyMM(mTempMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTempMatrix, 0, modelMatrix, 0, 16);

        cubeShaderProgram.setTextureUniform("u_Texture", textureHandle);

        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        cubeShaderProgram.setUniform("u_MVMatrix", MVPMatrix);

        Matrix.multiplyMM(mTempMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
        System.arraycopy(mTempMatrix, 0, MVPMatrix, 0, 16);
        cubeShaderProgram.setUniform("u_MVPMatrix", MVPMatrix);
        cubeShaderProgram.setUniform("u_LightPos", mLightPosInEyeSpace);
        cube.bindData(cubeShaderProgram);
        cube.draw();


        // Drawing the light elements
        lightShaderProgram.use();
        final int lspHandle = lightShaderProgram.getAttributeLocation("a_Position");
        glVertexAttrib3f(
                lspHandle,
                mLightPosInModelSpace[0],
                mLightPosInModelSpace[1],
                mLightPosInModelSpace[2]);
        glDisableVertexAttribArray(lspHandle);

        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
        System.arraycopy(mTempMatrix, 0, MVPMatrix, 0, 16);
        lightShaderProgram.setUniform("u_MVPMatrix", MVPMatrix);
        glDrawArrays(GL_POINTS, 0, 1);

    }

    @Override
    public void onTouch(MotionEvent event) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void handleMessage(byte[] msg) {

    }

    @Override
    public void initialiseState(byte[] sceneState) {  }

    @Override
    public int sceneID() {
        return mSceneID;
    }

}
