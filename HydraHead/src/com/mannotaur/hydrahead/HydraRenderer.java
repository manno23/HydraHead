package com.mannotaur.hydrahead;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.hardware.SensorEvent;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.scenes.*;
import com.mannotaur.hydrahead.scenes.Connecting.ConnectingScene;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class HydraRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private long globalStartTime;
    private Map<Integer, Scene> sceneMap;
    private Scene mCurrentScene;
    private HashMap<Integer,Scene> instrumentMap;

    private Timer timer;
    private int frameCount;

    public HydraRenderer(Context context, Networking mNetworkInterface) {
        this.context = context;
        this.sceneMap = new HashMap<Integer, Scene>();
        this.instrumentMap = new HashMap<Integer, Scene>();

        sceneMap.put(0, new ConnectingScene(mNetworkInterface));
        sceneMap.put(1, new FountainScene(mNetworkInterface, 1));
        sceneMap.put(2, new ElectronScene(mNetworkInterface, 2));

        instrumentMap.put(1, new InstrumentFountainScene(mNetworkInterface, 1));

        mCurrentScene = sceneMap.get(0); // Initialise to the ConnectingScene
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        globalStartTime = System.nanoTime();
        for (int sceneID : sceneMap.keySet())
            sceneMap.get(sceneID).addShader(context);
        for (int instrumentID: instrumentMap.keySet())
            instrumentMap.get(instrumentID).addShader(context);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        glViewport(0, 0, width, height);
        for (int sceneID : sceneMap.keySet())
            sceneMap.get(sceneID).initialise(width, height);
        for (int instrumentID : instrumentMap.keySet())
            sceneMap.get(instrumentID).initialise(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        mCurrentScene.draw(globalStartTime);
        frameCount++;
    }

    public void onTouch(MotionEvent event) {
        mCurrentScene.onTouch(event);
    }

    public void onSensorChanged(SensorEvent event) {
        mCurrentScene.onSensorChanged(event);
    }

    public void handleMessage(byte[] data) {
        mCurrentScene.handleMessage(data);
    }

    public void activateInstrument(int instrumentID) {
        Log.d("ControlSurface", "adding instrument " + instrumentID);
        mCurrentScene = instrumentMap.get(instrumentID);
    }

    public void connected(int sceneID, byte[] sceneState) {
        mCurrentScene = sceneMap.get(sceneID);
        mCurrentScene.initialiseState(sceneState);
    }

    public void connecitonTimedOut() {
        if (mCurrentScene instanceof ConnectingScene) {
            byte[] msg = new byte[]{ConnectingScene.MSG_CONNECTION_TIMED_OUT};
            mCurrentScene.handleMessage(msg);
        }
    }
}
