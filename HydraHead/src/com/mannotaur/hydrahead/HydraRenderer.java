package com.mannotaur.hydrahead;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

import android.opengl.GLSurfaceView;
import android.util.Log;
import com.mannotaur.hydrahead.scenes.*;

public class HydraRenderer implements GLSurfaceView.Renderer {


    private final String TAG = "HydraRenderer";
    private long globalStartTime;
    private Scene mCurrentScene;

    HydraRenderer(SceneAdapter mCurrentScene) {
        this.mCurrentScene = mCurrentScene;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        globalStartTime = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);
        mCurrentScene.initialise(width, height);
        Log.d(TAG, "onSurfaceChanged");
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        mCurrentScene.draw(globalStartTime);
    }

}
