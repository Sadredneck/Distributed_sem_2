package main.java.disributed.lab.worker;

import main.java.disributed.lab.CommandInterpreter;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

            System.out.printf("Worker %s: %d started.%n", host, port);

            while (working) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(() -> doConnection(socket));
                thread.start();
                threads.add(thread);
            }

            emptyThreads();

            serverSocket.close();
            System.out.printf("Worker %s: %d stopped.%n", host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void emptyThreads() {
        while (threads.stream().anyMatch(Thread::isAlive)){
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doConnection(Socket socket) {
        try {
            System.out.println("Received request");
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            String task = input.readLine();
            if (task.toUpperCase().equals("EXIT"))
                working = false;
            else
                interpreter.perform(task);
            output.println("done\n");
            output.flush();

            input.close();
            output.close();
            socket.close();
            System.out.println("Request completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Worker worker = new Worker("localhost", Integer.parseInt(args[0]));
        worker.launch();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
