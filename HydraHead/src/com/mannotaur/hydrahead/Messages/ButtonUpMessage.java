package com.mannotaur.hydrahead.Messages;

import com.mannotaur.hydrahead.HydraConfig;

/*
Constructs a message giving information:
    Type: Button Down Message (first 8 bytes [1-16]) 1
    Data:
        Horizontal Position: position horizontally [0 - 100]
        Vertical Position: position vertically [0 - 100]
 */
public class ButtonUpMessage extends Message {

    private int x;
    private int y;

    public ButtonUpMessage(float x, float y, int screenWidth, int screenHeight) {
        this.x = (int)(x / (float)screenWidth * 100f);
        this.y = (int)(y / (float)screenHeight * 100f);
    }

    @Override
    public byte[] byteArray() {
        byte[] output = new byte[4];
        output[0] = (byte)HydraConfig.IDENTIFIER;
        output[1] = (byte)2;
        output[2] = (byte)x;
        output[3] = (byte)y;

        return output;
    }
}
