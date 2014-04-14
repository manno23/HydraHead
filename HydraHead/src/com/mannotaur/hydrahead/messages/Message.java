package com.mannotaur.hydrahead.messages;

import static java.lang.Float.floatToIntBits;


/* types of messages
    Button down -discrete
    Button up - discrete
    Rotation matrix - continuous stream
 */
public abstract class Message {

    private static final String MIDICLIENT = "MidiClient";

    public abstract byte[] byteArray();

    protected byte[] floatToByte(float[] inData) {

        int j = 0;
        int num_floats = inData.length;
        byte[] out_data = new byte[num_floats*4];
        for(float value : inData) {
            int data = floatToIntBits(value);
            out_data[j++] = (byte) (data>>>24);
            out_data[j++] = (byte) (data>>>16);
            out_data[j++] = (byte) (data>>>8);
            out_data[j++] = (byte) (data);
        }
        return out_data;
    }
}

