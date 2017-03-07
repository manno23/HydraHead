package com.mannotaur.hydrahead;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import static com.mannotaur.hydrahead.Networking.CONFIRM_CONNECT;
import static com.mannotaur.hydrahead.Networking.DETECTING_WIFI_TIMED_OUT;


/**
 * Created by jm on 24/11/16.
 */
class ConnectionThread extends Thread {

    private static final String TAG = "ConnectionThread";
    private static final int NUM_CONNECTION_ATTEMPTS_ALLOWED = 10;
    private static final int PACKET_SIZE = 64;

    private InetSocketAddress localAddress;
    private InetSocketAddress serverAddress;
    private Handler recvHandler;

    private final Object lock = new Object();
    private boolean exitConnectionThread;
    private boolean connectionThreadRunning;

    protected ConnectionThread(Handler.Callback handler) {
        recvHandler = new Handler(handler);
        serverAddress = new InetSocketAddress(HydraConfig.SERVER_IP, HydraConfig.SERVER_PORT);
    }

    public void connect(InetSocketAddress local_address) {
        this.localAddress = local_address;
        synchronized (lock) {
            exitConnectionThread = false;
            start();
        }
    }

    public void close() {
        synchronized (lock) {
            if (connectionThreadRunning) {
                exitConnectionThread = true;
                connectionThreadRunning = false;
            }
        }
    }

    @Override
    public void run() {

        Log.d(TAG, "In new thread, just started");
        int connection_attempt_count = 0;

                    /* Construct connection packet with information for
                       - Identity
                       - Message type */
        byte[] init_packet = new byte[PACKET_SIZE];
        init_packet[0] = Networking.CONNECTION_REQUEST;      // The message type
        init_packet[1] = HydraConfig.LOCAL_IP[3]; // The ID we will assign to the phone

        DatagramSocket recv_socket = null;
        boolean no_received_reply;

        Log.d(TAG, "Entering loop");
        do {
            no_received_reply = false;
            try {

                DatagramSocket send_socket = new DatagramSocket();
                DatagramPacket send_packet = new DatagramPacket(init_packet, init_packet.length, serverAddress);
                send_socket.send(send_packet);
                send_socket.close();

                recv_socket = new DatagramSocket(localAddress);
                recv_socket.setReuseAddress(true);
                byte[] buffer = new byte[PACKET_SIZE];
                recv_socket.setSoTimeout(1000);
                DatagramPacket recv_packet = new DatagramPacket(buffer, buffer.length);
                Log.d(TAG, "Receiving packet");
                recv_socket.receive(recv_packet);
                Log.d(TAG, ByteBuffer.wrap(recv_packet.getData()).toString() );

                // Either socket timesout, or continues here
                if(buffer[0] == CONFIRM_CONNECT) {
                    Bundle initialisationData = new Bundle();
                    initialisationData.putByteArray("data", buffer);
                    // Process received packet
                    Message msg = Message.obtain(recvHandler, CONFIRM_CONNECT);
                    msg.setData(initialisationData);
                    Log.d(TAG, "Sending msg");
                    recvHandler.sendMessage(msg);
                } else {
                    Log.d(TAG, "Received a non connection related message");
                    no_received_reply = true;
                }
                synchronized(lock) {
                    if (exitConnectionThread) {
                        Log.d(TAG, "We've been asked to exit the loop");
                        break;
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "IO Exception: " + e.toString());
                connection_attempt_count++;
                if (recv_socket != null)
                    recv_socket.close();
                no_received_reply = true;
                if (connection_attempt_count > NUM_CONNECTION_ATTEMPTS_ALLOWED) {
                    // Process received packet
                    Message msg = Message.obtain(recvHandler, DETECTING_WIFI_TIMED_OUT);
                    recvHandler.sendMessage(msg);
                    break;
                }
                SystemClock.sleep(500);
            }
        } while(no_received_reply);

        Log.d(TAG, "Exiting thread");
        if (recv_socket != null) {
            recv_socket.close();
        }
    }
}
