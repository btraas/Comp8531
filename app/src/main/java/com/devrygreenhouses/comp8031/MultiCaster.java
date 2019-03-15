package com.devrygreenhouses.comp8031;

import android.content.Context;
import android.net.Uri;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class MultiCaster {
    private static final int SEND_BUFFER = 1301;
    private DatagramSocket socket;
    private InetAddress group;
//    private byte[] buf;

    private String address;
    private int port;
    public MultiCaster(String address, int port) {
        this.address = address;
        this.port = port;

        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(address); // "230.0.0.0"
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send2(File file) throws IOException {
        InetAddress multicastAddress = InetAddress.getByName(address);
        int destPort = port;
        // Destination port of multicast packets
        int TTL = Integer.parseInt("10");
        // Create a UDP multicast socket with any available local port
        MulticastSocket socket = new MulticastSocket();
        socket.setTimeToLive(TTL);
        // Set the TTL




        socket.close();
    }

    public void send(File file) throws IOException, URISyntaxException {

        byte[] message = new byte[SEND_BUFFER];

        socket = new DatagramSocket();
        group = InetAddress.getByName(address); // "230.0.0.0"

        long length = file.length();

        System.out.println("length: " + length);

        byte[] start = new String("bytes=" + length + "\n").getBytes();
        System.arraycopy(start, 0, message, 0, start.length);

//        System.out.println("Sending start);
//        DatagramPacket packet0 = new DatagramPacket(message, SEND_BUFFER, group, this.port); // 4446
//        socket.send(packet0);

        int offset = 0;
        BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(file), SEND_BUFFER);
        byte[] buffer = new byte[SEND_BUFFER];
        int datapacket = 0;
        while(fileStream.read(buffer, offset, SEND_BUFFER) != -1) {
            datapacket++;
            DatagramPacket packet = new DatagramPacket(buffer, SEND_BUFFER, group, this.port); // 4446
            System.out.println("Sending data packet #"+datapacket);
            socket.send(packet);
        }
        fileStream.close();

        byte[] end = new String("end").getBytes();
        System.arraycopy(end, 0, message, 0, end.length);

        DatagramPacket packet = new DatagramPacket(message, SEND_BUFFER, group, this.port); // 4446
//        socket.send(packet);
        socket.close();
        //is.close();
    }


    public void sendBytes(byte[] bytes) throws IOException, URISyntaxException {


        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, this.port); // 4446
//        System.out.println("Sending data packet #"+datapacket);
        socket.send(packet);

//        DatagramPacket packet = new DatagramPacket(message, SEND_BUFFER, group, this.port); // 4446
//        socket.send(packet);

        //is.close();
    }

    public void close() {
        socket.close();
    }
}
