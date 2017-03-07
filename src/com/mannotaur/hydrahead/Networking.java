package com.mannotaur.hydrahead;

import android.os.*;
import android.util.Log;

import java.io.IOException;
import java.net.*;


/**
 * A networking object will provide the communication interface to the HydraHead server. <br>
 * It uses two separate threads for both sending and receiving packets. <br>
 * Management of connection and state and disconnection from the server is handled by a Networking object. <br>
 * The callback passed into it will react to all incoming packets from the server.
 */
public class Networking {

    private final String TAG = "Networking";

    private static final int PACKET_SIZE = 16;

    // Receiving Msg types
    public static final byte CONFIRM_CONNECT = 1;
    public static final byte DISCONNECTING = 2;
    public static final byte SCENE_UPDATE = 3;
    public static final byte CHANGE_SCENE = 4;
    public static final byte ACTIVATE_INSTRUMENT = 5;
    public static final byte DEACTIVATE_INSTUMENT = 6;
    public static final byte UPDATE_INSTRUMENT = 7;

    public static final byte DETECTING_WIFI_TIMED_OUT = 99;


    // Sending Msg types
    public static final byte CONNECTION_REQUEST = 1;
    public static final byte DISCONNECTION_REQUEST = 2;
    public static final byte CONNECTION_MAINTENANCE = 3;
    public static final byte INSTRUMENT_OUTPUT = 4;

    private NetworkSendThread mSendThread;
    private NetworkRecvThread mRecvThread;
    private Handler recvHandler;

    private InetAddress server_address;
    private InetSocketAddress local_address;


    /**
     * @param callback enables the receiving thread to pass it's messages back
     *                 to the main thread.
     */
    public Networking(Handler.Callback callback) {

        this.recvHandler = new Handler(callback);

        try { server_address = InetAddress.getByName(HydraConfig.SERVER_IP);
        } catch (UnknownHostException e) { e.printStackTrace(); }

    }

    /**
     * Starts a new thread in order to perform a connection
     * with the HydraHead server.<br>
     * This is called while the network may be attempting to connect,
     * and so potentially throws SocketExceptions as it attempts to bind
     * to it's assigned.
     * java.net.UnknownHostException Usually if the newly attached wifi
     * has not assigned an address.
     */

    /**
     * Once we have established a connection with the server,
     * we can begin the sending and receiving threads
     * asynchronously.
     */
    public void initialise(InetSocketAddress local_address) {
        this.local_address = local_address;
        mSendThread = new NetworkSendThread();
        mRecvThread = new NetworkRecvThread();
        mRecvThread.start();
        mSendThread.start();
    }

    /**
     * Disconnects the opened sockets.
     */
    public void close() {
        if (mSendThread != null && mRecvThread != null) {
            mSendThread.closeConnection();
            mRecvThread.closeConnection();
        }
    }

    /**
     * @param message - to be sent to the HydraServer
     */
    public void send(byte[] message) {

        Message msg = Message.obtain(mSendThread.messageHandler);
        Bundle data = new Bundle();

        data.putByteArray("body", message);
        msg.setData(data);
        if (mSendThread != null)
            mSendThread.sendMessage(msg);

    }

    class NetworkRecvThread extends Thread {

        private DatagramSocket recv_socket;
        private boolean close_thread = false;

        public void closeConnection() {
            synchronized (this) {
                close_thread = true;
            }
        }

        @Override
        public void run() {
            try {
                recv_socket = new DatagramSocket(local_address);
                // A timeout is neccesary so that it can move through the loop and check if we need to stop the thread
                recv_socket.setSoTimeout(1000);
            } catch (SocketException e) {
                Log.d(TAG, e.toString());
            }

            while(true) {
                final byte[] buffer = new byte[PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    if (recv_socket != null)
                        recv_socket.receive(packet);

                    Bundle data = new Bundle();
                    data.putByteArray("data", buffer);
                    Message msg = Message.obtain(recvHandler);
                    msg.what = (int) buffer[0];
                    msg.setData(data);
                    recvHandler.sendMessage(msg);
                } catch (IOException e) {
                    //Log.d("NoOutput", "We are ignoring the timeout exception because we are bad at writing " +
                    //        "concurrent code Kappa");
                }


                synchronized(this) {
                    if (recv_socket != null && close_thread) {
                        Log.d(TAG, "close");
                        recv_socket.close();
                        break;
                    }
                }
            }

        }

    }

    class NetworkSendThread extends Thread {

        private final int MSG_CLOSE_CONNECTION = 1;

        public Handler messageHandler;
        private DatagramSocket send_socket;

        public NetworkSendThread() {
            try {
                send_socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(Message msg) {
            if (messageHandler != null) messageHandler.sendMessage(msg);
        }

        public void closeConnection() {
            Message closeMessage = Message.obtain(messageHandler, MSG_CLOSE_CONNECTION);
            messageHandler.sendMessage(closeMessage);
        }

        @Override
        public void run() {
            Log.d(TAG, "Send thread running!");
            Looper.prepare();

            messageHandler = new Handler() {
                public void handleMessage(Message msg) {

                    if (msg.what == MSG_CLOSE_CONNECTION) {

                        byte[] data;
                        data = new byte[]{DISCONNECTING, HydraConfig.LOCAL_IP[3]};
                        DatagramPacket packet = new DatagramPacket(data, data.length, server_address, HydraConfig.SERVER_PORT);
                        try {
                            send_socket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        send_socket.close();

                    } else {

                        byte[] data;
                        if (msg.getData().containsKey("body")) {
                            data = msg.getData().getByteArray("body");
                        } else {
                            data = new byte[0];
                        }
                        DatagramPacket packet = new DatagramPacket(data, data.length, server_address, HydraConfig.SERVER_PORT);
                        try {
                            send_socket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };

            Looper.loop();
        }

    }

    /**
     *
     * @param address 64 bit int representation of an ip address
     * @return byte[4] IP address
     */
    public static byte[] getLocalAddress(int address) {
        return new byte[]{(byte) (0xff & address),
                        (byte) (0xff & (address >> 8)),
                        (byte) (0xff & (address >> 16)),
                        (byte) (0xff & (address >> 24))
        };
    }
}
