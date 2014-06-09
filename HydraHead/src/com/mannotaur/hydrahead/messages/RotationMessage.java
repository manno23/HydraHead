package com.mannotaur.hydrahead.messages;

import com.mannotaur.hydrahead.HydraConfig;
import com.mannotaur.hydrahead.Networking;

/**
 * @author Jason
 */
public class RotationMessage extends HydraMessage {

    private float[] r;
    private byte mSceneID;

    public RotationMessage(float[] matrix, int sceneID) {
        r = matrix;
        mSceneID = (byte) sceneID;
    }

    @Override
    public byte[] byteArray() {

        byte[] output = new byte[67];
        output[0] = 4;
        output[1] = HydraConfig.LOCAL_IP[3]; // Client ID
        output[2] = mSceneID;
        byte[] r2 = floatToByte(r);
        System.arraycopy(r2, 0, output, 3, 64);
        return output;
    }

}
