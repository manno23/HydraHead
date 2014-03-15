package com.mannotaur.hydrahead;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.Float.floatToIntBits;

public class UDPTask extends AsyncTask<Float, Void, Void> {

    private DatagramSocket sock;
    private String MIDICLIENT = "MidiClient";

    @Override
    protected Void doInBackground(Float... params) {

        sock = null;
        try {
            InetAddress addr = InetAddress.getByName(HydraConfig.SERVER_IP);
            sock = new DatagramSocket();
            byte[] message = float2Byte(params);
            DatagramPacket packet = new DatagramPacket(message, message.length, addr, HydraConfig.SERVER_PORT);
            sock.send(packet);
        } catch (UnknownHostException e) {
            Log.d(MIDICLIENT, "unknown host: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] float2Byte(Float... inData) {
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
