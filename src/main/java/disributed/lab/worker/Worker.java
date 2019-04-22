package main.java.disributed.lab.worker;

import main.java.disributed.lab.CommandInterpreter;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Worker {

    private ServerSocket serverSocket;
    private String host;
    private int port;
    private boolean working = true;
    private CommandInterpreter interpreter = new CommandInterpreter();
    private List<Thread> threads = new ArrayList<>();

    public Worker(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void launch() {
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));

            while (working) {
                Socket socket = serverSocket.accept();
                threads.add(new Thread(() -> doConnection(socket)));
            }

            //ToDo: add thread wait

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doConnection(Socket socket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (working) {
                String task = input.readLine();
                new Thread(() -> performTask(input, output, task));
            }

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performTask(BufferedReader input, BufferedWriter output, String task) {
        interpreter.perform(task);
        try {
            output.write("done");
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
