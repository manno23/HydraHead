package com.mannotaur.hydrahead.scenes;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.messages.HydraMessage;

/**
 * A Scene contains it's own shader program, interactivity and network repsonses. <br>
 * The server will have it's own scene models that correspond with the client scenes <br>
 * effectively forming a contract between the clients and server.
 */
public interface Scene {

    /**
     * All scene shader programs are preloaded by the application.
     * @param context the application context.
     */
    public void addShader(Context context);

    /**
     * Called by the graphics display on surface creation.
     * @param width screen width in pixels.
     * @param height screen height in pixels.
     */
    public void initialise(int width, int height);

    /**
     * Called by the graphics display to update the display.
     * @param globalStartTime initial time to discern time passed from initialisation.
     */
    public void draw(long globalStartTime);

    /**
     * Respond to screen touch events from the phone. <br>
     * We can send these to the server to use and also update the scene <br>
     * with this as well.
     * @param event the MotionEvent returned by the registered listener.
     */
    public void onTouch(MotionEvent event);

    /**
     * Respond to sensor events from the phone. <br>
     * We can send these to the server to use and also update the scene <br>
     * with this as well.
     * @param event the SensorEvent returned by the registered listener.
     */
    public void onSensorChanged(SensorEvent event);

    /**
     * Update the scene using the message from the server.
     * @param data as described by the policy for each Scene.
     */
    public void handleMessage(byte[] data);

    /**
     * Synchronises the state of the client with the server upon initialisation.
     * @param sceneState a Bundle object allows for decoupling of the state information
     *                   from the Scene interface, allowing for the addition of new scenes.
     */
    public void initialiseState(byte[] sceneState);
}
