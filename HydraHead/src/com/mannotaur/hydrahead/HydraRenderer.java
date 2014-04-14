package com.mannotaur.hydrahead;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mannotaur.hydrahead.scenes.*;

import java.util.HashMap;
import java.util.Map;

public class HydraRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private Networking mNetworkInterface;
    private long globalStartTime;
    private Map<Integer, Scene> sceneMap;
    private Scene mCurrentScene;


    public HydraRenderer(Context context, Networking mNetworkInterface) {
        this.context = context;
        this.sceneMap = new HashMap<Integer, Scene>();
        this.mNetworkInterface = mNetworkInterface;

        sceneMap.put(1, new ConnectingScene(mNetworkInterface));
        sceneMap.put(2, new FountainScene(mNetworkInterface));
        sceneMap.put(3, new FlatScene(mNetworkInterface));
        mCurrentScene = sceneMap.get(1);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_CULL_FACE | GL_DEPTH_TEST);

        globalStartTime = System.nanoTime();
        for (int sceneID : sceneMap.keySet())
            sceneMap.get(sceneID).addShader(context);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        mCurrentScene.draw(globalStartTime);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);
        for (int sceneID : sceneMap.keySet())
            sceneMap.get(sceneID).initialise(width, height);
    }

    public void onTouch(float x) {
        mCurrentScene.onTouch(x);
    }
}
