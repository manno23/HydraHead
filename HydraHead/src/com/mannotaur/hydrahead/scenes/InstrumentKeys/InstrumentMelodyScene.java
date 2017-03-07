package com.mannotaur.hydrahead.scenes.InstrumentKeys;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.MotionEvent;
import com.mannotaur.hydrahead.HydraConfig;
import com.mannotaur.hydrahead.Networking;
import com.mannotaur.hydrahead.R;
import com.mannotaur.hydrahead.programs.ShaderProgram;
import com.mannotaur.hydrahead.scenes.Scene;

import static android.opengl.GLES20.*;

/**
 * User: Jason Manning
 * Date: 9/06/14
 */
public class InstrumentMelodyScene implements Scene {

    private static final String TAG = "InstrumentMelodyScene";
    private static final byte UPDATE_NOTES = 1;

    public final int mSceneID;
    private final Networking mNetworkInterface;

    private Keys keys;
    private Starfall starfall;
    private ShaderProgram keyShaderProgram;
    private ShaderProgram starfallShaderProgram;


    public InstrumentMelodyScene(Networking networkInterface, int sceneID) {
        mNetworkInterface = networkInterface;
        mSceneID = sceneID;

        keys = new Keys();
        starfall = new Starfall();
    }

    @Override
    public void addShader(Context context) {
        keyShaderProgram = new ShaderProgram(context,
                R.raw.inst_melody_vert,
                R.raw.inst_melody_frag)
                .addAttribute("position")
                .addAttribute("colour");
        keys.addShader(keyShaderProgram);

        starfallShaderProgram = new ShaderProgram(context,
                R.raw.starfall_vert,
                R.raw.starfall_frag)
                .addAttribute("position")
                .addAttribute("colour")
                .addAttribute("speed")
                .addAttribute("start_time");
    }

    @Override
    public void initialise(int width, int height) {
        keys.initialise(width, height);
    }

    @Override
    public void draw(long globalStartTime) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;

        starfall.addStars(currentTime, 0.02f);
        starfallShaderProgram.use();
        starfallShaderProgram.setUniform("u_time", currentTime);
        starfall.bindData(starfallShaderProgram);
        starfall.draw();

        keys.draw();
    }

    @Override
    public void onTouch(MotionEvent event) {

        byte[] msg = new byte[4];
        byte[] touch = keys.onTouch(event);

        if (touch != null) {
            msg[0] = Networking.INSTRUMENT_OUTPUT;
            msg[1] = HydraConfig.LOCAL_IP[3];
            msg[2] = touch[0];
            msg[3] = touch[1];
            Log.d(TAG, "onTouch msg:"+msg[0]+" "+msg[1]+" "+msg[2]);
            mNetworkInterface.send(msg);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {  }

    @Override
    public void handleMessage(byte[] data) {
        byte msgType = data[0];
        Log.d(TAG, "msg type: " + data[0]);
        switch (msgType) {
            case UPDATE_NOTES:
                int noteCount = data[1];
                Log.d(TAG, "note count " + noteCount);
                keys.setNoteCount(noteCount);
        }
    }

    @Override
    public void initialiseState(byte[] sceneState) { }

    @Override
    public int sceneID() {
        return mSceneID;
    }

}
