package main.java.disributed.lab.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static final int PORT = 4444;
    private static List<Socket> clientSockets = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main(String[] args) {
        try {
            try {
                ServerSocket serverSocket = new ServerSocket(800, 0, InetAddress.getByName("localhost"));
                clientSockets.add(serverSocket.accept());

                try (Socket clientSocket = clientSockets.get(0)) {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    System.out.println(in.readLine());

                    out.write("Wrong server.");
                    out.flush();

                } finally {
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
