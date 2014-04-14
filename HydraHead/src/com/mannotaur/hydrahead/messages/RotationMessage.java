package com.mannotaur.hydrahead.messages;

import com.mannotaur.hydrahead.HydraConfig;

/**
 * User: jason
 * Date: 1/04/14
 * Time: 6:57 AM
 */
public class RotationMessage extends Message {

    private float[] r;

    public RotationMessage(float[] matrix) {
        r = matrix;
    }

    @Override
    public byte[] byteArray() {

        byte[] output = new byte[66];
        output[0] = (byte) HydraConfig.IDENTIFIER;
        output[1] = (byte)3;
        byte[] r2 = floatToByte(r);
        System.arraycopy(r2, 0, output, 2, 64);
        return output;
    }

}
