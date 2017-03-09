package com.mannotaur.hydrahead;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.scenes.*;
import com.mannotaur.hydrahead.scenes.Connecting.ConnectingScene;
import com.mannotaur.hydrahead.scenes.InstrumentKeys.InstrumentMelodyScene;

import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jm on 15/02/17.
 */
public class SceneAdapter implements Scene {

    private Scene mCurrentScene;
    private Context context;
    private Map<Integer, Scene> sceneMap;
    private HashMap<Integer,Scene> instrumentMap;

    public SceneAdapter(Context context, Networking mNetworkInterface) {

        this.context = context;
        this.sceneMap = new HashMap<Integer, Scene>();
        this.instrumentMap = new HashMap<Integer, Scene>();

        sceneMap.put(0, new ConnectingScene(mNetworkInterface, 0));
        sceneMap.put(1, new FountainScene(mNetworkInterface, 1));
        sceneMap.put(2, new ElectronScene(2));

        instrumentMap.put(1, new InstrumentFountainScene(mNetworkInterface, 1));
        instrumentMap.put(2, new InstrumentMelodyScene(mNetworkInterface, 2));
        instrumentMap.put(3, new InstrumentPowerBall(mNetworkInterface, 3));


    }

    @Override
    public void addShader(Context context) {
        mCurrentScene.addShader(context);
    }

    @Override
    public void initialise(int width, int height) {
        Log.d("HydraHead", "initialising scenes "+width+" "+height);
        HydraConfig.SCREEN_HEIGHT = height;
        HydraConfig.SCREEN_WIDTH = width;

        for (int sceneID : sceneMap.keySet()) {
            sceneMap.get(sceneID).addShader(context);
            sceneMap.get(sceneID).initialise(width, height);
        }
        for (int instrumentID: instrumentMap.keySet()) {
            instrumentMap.get(instrumentID).addShader(context);
            instrumentMap.get(instrumentID).initialise(width, height);
        }
    }

    @Override
    public void draw(long globalStartTime) {
        mCurrentScene.draw(globalStartTime);
    }

    @Override
    public void onTouch(MotionEvent event) {
        mCurrentScene.onTouch(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mCurrentScene.onSensorChanged(event);
    }

    @Override
    public void handleMessage(byte[] data) {
        mCurrentScene.handleMessage(data);
    }

    @Override
    public void initialiseState(byte[] sceneState) {
        mCurrentScene.initialiseState(sceneState);
    }

    @Override
    public int sceneID() {
        return mCurrentScene.sceneID();
    }

    public void changeScene(int sceneNo, byte[] initialData) {
        mCurrentScene = sceneMap.get(sceneNo);
        if (initialData != null) {
            mCurrentScene.initialiseState(initialData);
        }
    }

    public void activateInstrument(int instrumentID, byte[] messageData) {
        mCurrentScene = instrumentMap.get(instrumentID);
        if (messageData != null) {
            mCurrentScene.initialiseState(messageData);
        }
    }

}
