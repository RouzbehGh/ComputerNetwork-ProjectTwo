package Roozbeh.Ghasemi;

/**
 * Created by Roozbeh Ghasemi on 12/19/2018.
 */

import java.io.IOException;
import java.nio.file.Path;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server extends Thread {
    int port ;
    private boolean running;
    private byte[] buffer = new byte[256]; //Size of buffer
    private byte[] result = new byte[1024]; //Size of result
    private byte[] request_answer = new byte[256];
    private DatagramSocket socket;
    String[] acknowledgement;
    InetAddress address;
    String received_acknoledgment = "";
    String answer = ""; //answer that it has file or not
    private String[] fiesta = {"hello.txt", "Roozbeh.txt", "Roozbeh1.txt"};
    /**
     * This method is used for Make Server in port of 1397 and in loopback position.
     */
    public Server() {
        try {
            socket = new DatagramSocket(1397, InetAddress.getLoopbackAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used for Send and Receive files.
     */
    public void run() {
        running = true;
        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                System.out.println("server recieved");
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            address = packet.getAddress();
            port = packet.getPort();
            String received
                    = new String(packet.getData(), 0, packet.getLength());
            System.out.printf("Packet is just arrived from: %s:%d with content of %s\n", address.toString(), port, received);
            String[] request_file = received.split("-");
            String reqfile = request_file[3];
            System.out.println("request_file name" + request_file[3]);
            if(request_file[3] == "")
            {
                System.out.println("not a valid req");
            }
            for(int i = 0; i < fiesta.length ; i++)
            {
                if(reqfile.equals(fiesta[i]))
                {
                    System.out.println("we have req file -" + reqfile);
                    answer  = ("yes-" + reqfile);
                }
            }
            request_answer = answer.getBytes();
            packet = new DatagramPacket(request_answer, request_answer.length, address, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("error in sending anser to client");
            }
            System.out.println("answer send to the client");
            DatagramPacket packet1
                    = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet1);
                received_acknoledgment
                        = new String(packet1.getData(), 0, packet1.getLength());
                System.out.println("server received : " + received_acknoledgment);
            } catch (IOException e) {
                e.printStackTrace();
            }
            acknowledgement = received_acknoledgment.split("-");
            if(!(acknowledgement[0].equals("Thanks!")))
            {
                System.out.println("I will send a file");
                sendfile();
            }
        }
    }
    /**
     * this method is use for send file from a peer to another peer
     */
    private void sendfile()
    {
        System.out.println("sending a file started :)");
        Path fileLocation = Paths.get("C:\\Desktop\\file_holder\\file.txt");
        byte[] data = null;
        try {
            data = Files.readAllBytes(fileLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double number_packets = Math.ceil((data.length)/(127.0));
        String num_pack = Double.toString(number_packets) + "-" + data.length;
        byte[]num =  num_pack.getBytes();
        DatagramPacket packet = new DatagramPacket(num, num.length , address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("error in sending number of packets");
            e.printStackTrace();
        }
        System.out.println("data size" + data.length);
        for(int i = 0; i < number_packets - 1; i++)
        {
            byte[] buffer = new byte[128];
            buffer[0] = (byte) (i % 256);
            for(int j = 0; j < 127; j++)
            {
                buffer[1 + j] = data[j + i*127];
            }
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length , address, port);
            try {
                socket.send(packet1);
            } catch (IOException e) {
                System.out.println("error in sending packets");
                e.printStackTrace();
            }
        }
        byte[] buffer = new byte[((data.length) % 127) + 1];
        buffer[0] = (byte) ((number_packets - 1)%256);
        for(int i = 0; i < ((data.length) % 127); i++)
        {
            buffer[1 + i] = data[(int) (127*(number_packets -1) + i)];
        }
        DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length , address, port);
        try {
            socket.send(packet1);
        } catch (IOException e) {
            System.out.println("error in sending last packet");
            e.printStackTrace();
        }
        System.out.println("sending file finished...:)");

    }
}