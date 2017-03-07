package com.mannotaur.hydrahead;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.mannotaur.hydrahead.scenes.Connecting.ConnectingScene;

import java.util.Arrays;


/**
 * Created by jm on 15/02/17.
 */
public class HydraHead {

    private final String TAG = "HydraHead";
    private SceneAdapter mCurrentScene;

    private HydraConnectionManager connectionManager;
    private WifiConnectionManager wifiManager;


    public HydraHead(Context context) {

        Log.d(TAG, "Hydrahead const");
        this.wifiManager = new WifiConnectionManager(context, this);
        Log.d(TAG, "WifiManager");
        this.connectionManager = new HydraConnectionManager(context, this);
        Log.d(TAG, "ConnManager");

        this.mCurrentScene = new SceneAdapter(context, connectionManager.getmNetworkInterface());
        mCurrentScene.changeScene(0, null); // Initialise to the ConnectingScene
    }

    void connect() {
        sceneUpdate(0, new byte[]{ConnectingScene.SEARCHING});
        wifiManager.onResume();
        wifiManager.connectToNetwork();
    }

    void disconnect() {
        wifiManager.onPause();
        connectionManager.disconnect();
    }

    public SceneAdapter getSceneAdapter() {
        return mCurrentScene;
    }


    /* Wifi Event Responses */

    public void connectedToNetwork() {
        sceneUpdate(0, new byte[]{ConnectingScene.CONNECTING});
        connectionManager.connect();
    }

    public void networkNotFound() {
        sceneUpdate(0, new byte[]{ConnectingScene.SEARCH_UNSUCCESSFUL});
    }


    /* Network Event Responses */

    public void changeScene(int sceneID, byte[] messageData) {
        mCurrentScene.changeScene(sceneID, messageData);
    }

    public void sceneUpdate(int sceneID, byte[] messageData) {
        if (sceneID == mCurrentScene.sceneID()) {
            mCurrentScene.handleMessage(messageData);
        }
    }

    public void activateInstrument(int sceneID, byte[] messageData) {
        mCurrentScene.activateInstrument(sceneID, messageData);
    }

    public void deactivateInstrument(int sceneID, byte[] messageData) {
        mCurrentScene.changeScene(sceneID, messageData);
    }

    public void instrumentUpdate(int sceneID, byte[] messageData) {
        Log.d(TAG, byteArrayToString(messageData));
        sceneUpdate(sceneID, messageData);
    }

    private String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for(byte b : array) {
            sb.append(b); sb.append(" ");
        }
        return sb.toString();
    }
}

