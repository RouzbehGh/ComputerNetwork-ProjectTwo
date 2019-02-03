package Roozbeh.Ghasemi;

/**
 * Created by Roozbeh Ghasemi on 12/19/2018.
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.io.IOException;
import java.net.DatagramPacket;


public class Client {

    public static void main(String[] args) {
        byte[] buf = new byte[256];
        String[] request_answer = null;
        String received = "";
        int server_with_req_file = -1;
        int servers_connected = 0;
        Scanner stdin = new Scanner(System.in);
        String filename = null;
        System.out.println("please enter number of connected servers");
        servers_connected = stdin.nextInt();
        DatagramSocket[] server_sockets = new DatagramSocket [servers_connected];
        int[] server_port = new int[servers_connected];
        String[] serverIP = new String[servers_connected];
        for(int i = 0; i < servers_connected; i++)
        {
            System.out.println("please enter the port of server " + i);
            int port = stdin.nextInt();
            server_port[i] = port;
            System.out.println("please enter the IP of server" + i);
            String serIP = stdin.next();
            serverIP[i] = serIP;
        }
        while (true) {
            new Server().start();
            System.out.println("Please your requested filename :");
            while(filename == null)
                filename = stdin.next();
            String request = "p2p - i want a file - file name -" + filename;
            System.out.println(request);
            try {
                System.out.println("client sending request started :)");
                for(int i = 0; i < servers_connected; i++)
                {
                    DatagramSocket client = new DatagramSocket();
                    server_sockets[i] = client;
                }
                for (int i = 0; i < servers_connected ; i++)
                    server_sockets[i].send(new DatagramPacket(request.getBytes(),
                            request.length(), InetAddress.getByName(serverIP[i]), server_port[i]));

                System.out.println("client waiting for the answer...!");
                {
                    loop: for(int i = 0; i < servers_connected ; i++)
                    {
                        DatagramPacket waiting = new DatagramPacket(buf, buf.length);
                        server_sockets[i].receive(waiting);
                        received
                                = new String(waiting.getData(), 0, waiting.getLength());
                        System.out.println("client received...!" + received);
                        request_answer = received.split("-");
                        if(request_answer[0].equals("yes"))
                        {
                            server_with_req_file = i;
                            System.out.println("server has the file IP: "+serverIP[i] + " port: " + server_port[i]);
                            break loop;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(server_with_req_file != -1)
            {
                String acknowledgement_file = "send me file + " + filename;
                try {
                    server_sockets[server_with_req_file].send(new DatagramPacket(acknowledgement_file.getBytes(),
                            acknowledgement_file.length(), InetAddress.getByName(serverIP[server_with_req_file]), server_port[server_with_req_file]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for(int i = 0; i < servers_connected; i++)
            {
                if(i != server_with_req_file)
                {
                    String acknowledgement_file = "Thanks!";
                    try {
                        server_sockets[i].send(new DatagramPacket(acknowledgement_file.getBytes(),
                                acknowledgement_file.length(), InetAddress.getByName(serverIP[i]), server_port[i]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            DatagramPacket number_packets = new DatagramPacket(buf, buf.length);
            try {
                server_sockets[server_with_req_file].receive(number_packets);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inforec
                    = new String(number_packets.getData(), 0, number_packets.getLength());
            String[] infosplit = inforec.split("-");
            double number =  Double.parseDouble(infosplit[0]);
            double file_length = Double.parseDouble(infosplit[1]);
            System.out.println("file length is " + file_length);
            byte[]file = new byte[(int) file_length];
            System.out.println("download started:)");
            for(int i  = 0; i < Math.min(number, 256) ; i++)
            {
                DatagramPacket filepack = new DatagramPacket(buf, buf.length);
                try {
                    server_sockets[server_with_req_file].receive(filepack);
                } catch (IOException e) {
                    System.out.println("uh...! error in downloading file" );
                    e.printStackTrace();
                }
                int number_of_packs = buf[0];
                if(number_of_packs > 0)
                {
                    for(int j = 0; j < 127 ; j++)
                    {
                        file[number_of_packs*127 + j ] = buf[1 + j];
                    }
                }
                else
                    for(int j = 0; j < 127 ; j++)
                    {
                        file[-number_of_packs*127 + j ] = buf[1 + j];
                    }
            }
            if(number >= 256)
            {
                DatagramPacket file_packets = new DatagramPacket(buf, buf.length);
                try {
                    server_sockets[server_with_req_file].receive(file_packets);
                } catch (IOException e) {
                    System.out.println("uh...! error in downloading file" );
                    e.printStackTrace();
                }
                int numberpack = buf[0];
                if(numberpack > 0)
                {
                    for(int j = 0; j < 127 ; j++)
                    {
                        file[(numberpack + 255)*127 + j ] = buf[1 + j];
                    }
                }
                else
                    for(int j = 0; j < 127 ; j++)
                    {
                        file[(-numberpack*127 + 255) + j ] = buf[1 + j];
                    }
            }
            System.out.println("download finished successfully!)");
            System.out.println("Now,building file started!");
            try (FileOutputStream fos =
                         new FileOutputStream("C:\\Desktop\\file_holder\\received_file.txt")) {
                try {
                    fos.write(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Do you want to continue? (Y/N)");
            String answer = stdin.next();
            if (!answer.equals("Y")) {
                break;
            }
        }
        System.exit(0);
    }
}