package main.java.disributed.lab.worker;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Worker {

    private ServerSocket serverSocket;
    private String host;
    private int port;
    private boolean working = true;
    private Map<String, Long> storage;

    public Worker(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void launch() {
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));

            Socket socket = serverSocket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (working) {
                String task = input.readLine();
                new Thread(() -> performTask(input, output, task));
            }
            input.close();
            output.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performTask(BufferedReader input, BufferedWriter output, String task) {
        System.out.println(task);
    }

    public static void main(String[] args) {
        Worker worker = new Worker("localhost", 999);
        worker.launch();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
