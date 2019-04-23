package main.java.disributed.lab.router;

import main.java.disributed.lab.worker.Worker;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Router {

    private ServerSocket serverSocket;
    private String host;
    private int port;
    private List<Worker> workers;
    private LinkedList<Thread> threads = new LinkedList<>();

    public Router(String host, int port, List<Worker> workers) {
        this.host = host;
        this.port = port;
        this.workers = workers;
    }

    public void launch() {
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));
            LinkedList<String> commands = readTasks();
            System.out.printf("Router %s: %d started.%n", host, port);

            String lastTask = commands.get(0);
            while (!commands.isEmpty()) {
                String task = commands.removeFirst();

                if (!task.split("\\W+", 1)[0].equals(lastTask.split("\\W+", 1)[0]))
                    emptyThreads();
                Thread thread = new Thread(() -> sendTask(task));
                thread.start();
                threads.add(thread);
                lastTask = task;
            }

            serverSocket.close();
            System.out.printf("Router %s: %d stopped.%n", host, port);
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

    private void checkActive(Socket socket) {
        //ToDo: make interval check
    }

    private void sendTask(String task) {
        String[] words = task.trim().split("\\W+");
        switch (words[1].toUpperCase()) {
            case "SELECT":
                if (words.length == 2)
                    sendToAll("SELECT");
                else
                    sendToSpecific("SELECT " + words[2], words[2]);
                break;
            case "INSERT":
                sendToSpecific("INSERT " + words[2] + " " + words[3], words[2]);
                break;
        }

    }

    private void sendToAll(String task) {
        try {
            for (Worker worker : workers) {
                Socket socket = new Socket(worker.getHost(), worker.getPort());
                send(socket, task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToSpecific(String task, String key) {
        try {
            Worker worker = key.length() <= 64 ? workers.get(0) : workers.get(1);
            Socket socket = new Socket(worker.getHost(), worker.getPort());
            new Thread(() -> send(socket, task));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(Socket socket, String task) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            output.println(task+"\n");
            output.flush();
            input.readLine();

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failure at send(Socket, String)");
        }

    }

    private LinkedList readTasks() {
        //return new LinkedList(Arrays.asList("100 SELECT", "200 INSERT abc 100", "300 SELECT"));
        return new LinkedList(Collections.singletonList("200 INSERT abc 100"));
    }

    public static void main(String[] args) {
        Worker worker1 = new Worker("localhost", Integer.parseInt(args[0]));
        Worker worker2 = new Worker("localhost", Integer.parseInt(args[1]));

        Router router = new Router("localhost", 888, new ArrayList<>(Arrays.asList(worker1, worker2)));
        router.launch();
    }
}
