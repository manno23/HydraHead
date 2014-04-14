package com.mannotaur.hydrahead;

import com.mannotaur.hydrahead.messages.Message;
import android.util.Log;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class Networking {

    private DatagramSocket send_socket;
    private DatagramSocket recv_socket;
    private InetAddress server_address;
    private boolean running;
    private String MIDICLIENT = "MidiClient";

    public Networking() {
        running = false;
    }


    public void init() {

        running = true;

        try {
            server_address = InetAddress.getByName(HydraConfig.SERVER_IP);
            send_socket = new DatagramSocket();
        } catch (UnknownHostException e) {
            Log.d(MIDICLIENT, "unknown host: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean establishConnection() {

        final boolean[] result = new boolean[1];


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                int num_attempts = 0;

                byte[] init_packet = new byte[4];
                init_packet[0] = (byte)HydraConfig.IDENTIFIER;
                init_packet[1] = (byte)4; // Message type (4: connection handshake type)
                init_packet[2] = (byte)1; // Handshake phase

                boolean no_received_reply;
                do {
                    no_received_reply = false;
                    try {

                        init_packet[3] = (byte)++num_attempts;

                        DatagramSocket conn_socket = new DatagramSocket();
                        DatagramPacket packet = new DatagramPacket(init_packet, init_packet.length, server_address, HydraConfig.SERVER_PORT);
                        Log.d(MIDICLIENT, "sending connection request");
                        conn_socket.send(packet);
                        conn_socket.close();

                        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("192.168.1.3"), 5555);
                        DatagramSocket recv_socket = new DatagramSocket(addr);
                        byte[] buffer = new byte[4];
                        recv_socket.setSoTimeout(1000);
                        DatagramPacket recv_packet = new DatagramPacket(buffer, buffer.length);
                        recv_socket.receive(recv_packet);

                        // Process received packet
                        if(buffer[0] == 'a') Log.d(MIDICLIENT, "Accepted");
                        if(buffer[0] == 'b') Log.d(MIDICLIENT, "Already registered");
                        result[0] = true;

                    } catch (SocketTimeoutException e) {
                        Log.d(MIDICLIENT, "receive timed out, time to try again");
                        no_received_reply = true;
                        e.printStackTrace();
                    } catch (SocketException e) {
                        result[0] = false;
                        e.printStackTrace();
                    } catch (IOException e) {
                        result[0] = false;
                        e.printStackTrace();
                    }
                } while(no_received_reply);
            }
        });
        t.start();

        return result[0];
    }

    public void beginReceiving() {
        running = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("192.168.1.3"), 5555);
                    DatagramSocket recv_socket = new DatagramSocket(addr);
                    while(running) {
                        Log.d(MIDICLIENT, "Waiting on a message");
                        byte[] buffer = new byte[18];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        // If no receival within timelimit resend
                        recv_socket.receive(packet);
                        Log.d(MIDICLIENT, "Received a packet!" + buffer.toString());
                    }
                    recv_socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public boolean send(final Message message) {

        final boolean[] result = new boolean[1];
        result[0] = false;
        if(running) {
            Thread send_packet = new Thread( new Runnable() {
                                                 @Override
                                                 public void run() {
                    byte[] packet_body = message.byteArray();
                    DatagramPacket packet = new DatagramPacket(packet_body, packet_body.length, server_address, HydraConfig.SERVER_PORT);
                    try {
                        send_socket.send(packet);
                        result[0] = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
            });
            send_packet.start();
        }
        return result[0];
    }

    public void close() {
        if(recv_socket != null) recv_socket.close();
    }

    public String getLocalAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface interf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = interf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress addr = enumIpAddr.nextElement();
                    if (!addr.isLoopbackAddress()) {
                        Log.d(MIDICLIENT, addr.toString());
                        return addr.toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

}
