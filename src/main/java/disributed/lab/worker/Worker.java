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
        while (threads.stream().anyMatch(Thread::isAlive)) {
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
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            String task = input.readLine();
            if (task.toUpperCase().equals("EXIT"))
                working = false;
            else
                perform(output, task);
            output.append("done").append("\n");
            output.flush();

            input.close();
            output.close();
            socket.close();
            System.out.println("Request completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void perform(PrintWriter output, String task) {
        String[] words = task.split("\\W+");

        switch (words[0].toUpperCase()) {
            case "SELECT":
                if (words.length == 1) {
                    List<String> list = interpreter.doSelect();
                    for (String line : list) {
                        output.append(line).append("\n");
                        output.flush();
                    }
                } else {
                    output.append(interpreter.doSelect(words[1])).append("\n");
                    output.flush();
                }
                break;
            case "INSERT":
                interpreter.doInsert(words[1], Long.parseLong(words[2]));
                break;
            case "CHECK":
                output.append("YES");
                output.flush();
                break;
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
