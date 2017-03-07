package com.mannotaur.hydrahead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.mannotaur.hydrahead.scenes.Connecting.ConnectingScene;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * Created by jm on 24/11/16.
 */
public class HydraConnectionManager implements Handler.Callback {

    private final String TAG = "HydraConnectionManager";
    private final ConnectionThread connectionThread;
    private final Networking mNetworkInterface;
    private InetSocketAddress local_address;
    private boolean connectionThreadRunning;
    private boolean networkThreadsRunning;
    private HydraHead hydra;

    public HydraConnectionManager(Context context, HydraHead hydra) {

        this.hydra = hydra;

        connectionThreadRunning = false;
        networkThreadsRunning = false;
        connectionThread = new ConnectionThread(this);
        mNetworkInterface = new Networking(this);
    }

    public void connect() {
        try {
            String localIp = InetAddress.getByAddress(HydraConfig.LOCAL_IP).getHostAddress();
            local_address = new InetSocketAddress(localIp, HydraConfig.LOCAL_PORT);
            synchronized (this) {
                if (!connectionThreadRunning) {
                    connectionThread.connect(local_address);
                    connectionThreadRunning = true;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if(connectionThreadRunning) {
            connectionThread.close();
            connectionThreadRunning = false;
        }
        if(networkThreadsRunning) {
            mNetworkInterface.close();
            networkThreadsRunning = false;
        }
    }

    @SuppressLint("NewApi")
    @Override
    /*
     * Delivered messages are of the form
     * [ MSG_TYPE, SCENE_ID, DATA... ]
     */
    public boolean handleMessage(Message msg) {
        Bundle packet = msg.getData();

        if(packet == null)
            return false;

        byte[] data = packet.getByteArray("data");
        int sceneID = data[1];
        byte[] messageData = Arrays.copyOfRange(data, 2, data.length-1);
        switch (msg.what) {


            /* HydraConnection Callbacks */

            case Networking.CONFIRM_CONNECT:
                Log.d(TAG, "Connected to server " + data[0] + " " + data[1] + " " + data[2]);
                //TODO problem when connect to live performance and skip the connection phase
                //Graphic never progresses to CONNECTED_AND_WAITING

                connectionThreadRunning = false;
                // Update the ConnectingScene graphic.
                sceneID = data[1];
                if (sceneID == 0) {
                    /* If we're on connecting Scene, wait for server to go online */
                    hydra.sceneUpdate(0, new byte[]{ConnectingScene.CONNECTED_AND_WAITING});
                } else {
                    /* else change scene to whatever state the server is at */
                    hydra.changeScene(sceneID, messageData);
                }
                // Begin the network send and receive threads.
                mNetworkInterface.initialise(local_address);
                networkThreadsRunning = true;
                break;

            case Networking.CHANGE_SCENE:
                Log.d(TAG, "changing scene " + sceneID);
                hydra.changeScene(sceneID, messageData);
                break;

            case Networking.SCENE_UPDATE:
                Log.d(TAG, ""+messageData[0]+" " + messageData[1]);
                hydra.sceneUpdate(sceneID, messageData);
                break;

            case Networking.ACTIVATE_INSTRUMENT:
                Log.d(TAG, "activating instrument " + sceneID);
                hydra.activateInstrument(sceneID, messageData);
                break;

            case Networking.DEACTIVATE_INSTUMENT:
                Log.d(TAG, "deactivating instrument " + sceneID);
                hydra.deactivateInstrument(sceneID, messageData);
                break;

            case Networking.UPDATE_INSTRUMENT:
                Log.d(TAG, "updating instrument");
                hydra.instrumentUpdate(sceneID, messageData);
                break;

            default:
                //TODO we are getting bulk errors
                if(data != null)
                    Log.d(TAG, "Ignored message " + data.length+ ": " + data[0] + " " + data[1] + " " + data[3]);
                else
                    Log.d(TAG, "Ignored message empty");

        }
        return true;
    }

    public Networking getmNetworkInterface() {
        return mNetworkInterface;
    }


}
