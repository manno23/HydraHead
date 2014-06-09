package com.mannotaur.hydrahead;

import android.os.*;
import android.util.Log;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * A networking object will provide the communication interface to the Hydra server. <br>
 * It uses two separate threads for both sending and receiving packets. <br>
 * Management of connection and state and disconnection from the server is handled by a Networking object. <br>
 * The callback passed into it will react to all incoming packets from the server.
 */
public class Networking {

    private static final int PACKET_SIZE = 128;

    // Receiving Msg types
    public static final byte CONNECTION_VERIFICATION = 1;
    public static final byte DISCONNECTING = 2;
    public static final byte SCENE_UPDATE = 3;
    public static final byte CHANGE_SCENE = 4;
    public static final byte ACTIVATE_INSTRUMENT = 5;
    public static final byte DEACTIVATE_INSTUMENT = 6;

    public static final byte DETECTING_WIFI_TIMED_OUT = 99;


    // Sending Msg types
    public static final byte CONNECTION_REQUEST = 1;
    public static final byte DISCONNECTION_REQUEST = 2;
    public static final byte CONNECTION_MAINTENANCE = 3;
    public static final byte SCENE_OUTPUT = 4;

    private NetworkSendThread mSendThread;
    private NetworkRecvThread mRecvThread;
    private ConnectionThread mConnectionThread;
    private boolean connectionThreadRunning = false;
    private Handler recvHandler;

    private InetAddress server_address;
    private InetSocketAddress local_address;
    private String TAG = "ControlSurface";


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
     * with the Hydra server.<br>
     * This is called while the network may be attempting to connect,
     * and so potentially throws SocketExceptions as it attempts to bind
     * to it's assigned.
     * @exception java.net.UnknownHostException Usually if the newly attached wifi
     * has not assigned an address.
     */
    public void connect() {
        local_address = new InetSocketAddress(HydraConfig.LOCAL_PORT);
        synchronized (this) {
            if (!connectionThreadRunning) {
                mConnectionThread = new ConnectionThread(local_address, server_address, recvHandler);
                mConnectionThread.connect();
                connectionThreadRunning = true;
            }
        }
    }

    /**
     * Once we have established a connection with the server,
     * we can begin the sending and receiving threads
     * asynchronously.
     */
    public void initialise() {
        mSendThread = new NetworkSendThread();
        mRecvThread = new NetworkRecvThread();
        mRecvThread.start();
        mSendThread.start();
    }

    /**
     * Disconnects the opened sockets.
     */
    public void close() {
        synchronized (this) {
            if (mConnectionThread != null) {
                mConnectionThread.close();
                connectionThreadRunning = false;
            }
        }
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
                    Log.d("TAG", e.toString());
                }


                synchronized(this) {
                    if (recv_socket != null && close_thread) {
                        Log.d("ControlSurface", "close");
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

    class ConnectionThread extends Thread {

        private static final int NUM_CONNECTION_ATTEMPTS_ALLOWED = 10;

        private InetSocketAddress localAddress;
        private InetAddress serverAddress;
        private Handler recvHandler;

        private final Object lock = new Object();
        private boolean exitConnectionThread;

        private ConnectionThread(InetSocketAddress localAddress,
                                 InetAddress serverAddress,
                                 Handler recvHandler) {
            this.localAddress = localAddress;
            this.serverAddress = serverAddress;
            this.recvHandler = recvHandler;
        }

        public void connect() {
            synchronized (lock) {
                exitConnectionThread = false;
                start();
            }
        }

        public void close() {
            synchronized (lock) {
                exitConnectionThread = true;
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
            init_packet[0] = CONNECTION_REQUEST;      // The message type
            init_packet[1] = HydraConfig.LOCAL_IP[3]; // The ID we will assign to the phone

            DatagramSocket send_socket = null;
            DatagramSocket recv_socket = null;
            boolean no_received_reply;

            Log.d(TAG, "Entering loop");
            do {
                no_received_reply = false;
                try {

                    send_socket = new DatagramSocket();
                    DatagramPacket send_packet = new DatagramPacket(init_packet, init_packet.length, serverAddress, HydraConfig.SERVER_PORT);
                    send_socket.send(send_packet);
                    send_socket.close();

                    recv_socket = new DatagramSocket(localAddress);
                    recv_socket.setReuseAddress(true);
                    byte[] buffer = new byte[PACKET_SIZE];
                    recv_socket.setSoTimeout(1000);
                    DatagramPacket recv_packet = new DatagramPacket(buffer, buffer.length);
                    Log.d(TAG, "Receiving packet");
                    recv_socket.receive(recv_packet);

                    // Either socket timesout, or continues here
                    if(buffer[0] == CONNECTION_VERIFICATION) {
                        Bundle initialisationData = new Bundle();
                        initialisationData.putByteArray("data", buffer);
                        // Process received packet
                        Message msg = Message.obtain(recvHandler, CONNECTION_VERIFICATION);
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



    /**
     *
     * @param address 64 bit int representation of an ip address
     * @return byte[4] IP address
     */
    public static byte[] getLocalAddress(int address) {
        byte[] addressBytes = {
                (byte)(0xff & address),
                (byte)(0xff & (address >> 8)),
                (byte)(0xff & (address >> 16)),
                (byte)(0xff & (address >> 24)) };

        return addressBytes;
    }
}
