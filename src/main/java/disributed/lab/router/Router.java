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
            System.out.printf("Router %s: %d started.%n", host, port);

            performTasks(readTasks());

            serverSocket.close();
            System.out.printf("Router %s: %d stopped.%n", host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performTasks(LinkedList<String> tasks) {
        String[] lastTask = tasks.get(0).trim().split("\\W+");
        while (!tasks.isEmpty()) {
            String[] task = tasks.removeFirst().trim().split("\\W+");

            if (!task[0].equals(lastTask[0]))
                emptyThreads();

            Thread thread = new Thread(() -> sendTask(task));
            thread.start();
            threads.add(thread);
            lastTask = task;
        }
        emptyThreads();
    }

    private void emptyThreads() {
        while (threads.stream().anyMatch(Thread::isAlive)) {
        }
    }

    private void checkActive(Worker worker) {
        try {
            Socket socket = new Socket(worker.getHost(), worker.getPort());
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            output.append("CHECK").append("\n");
            output.flush();
            String answer = input.readLine();
            while (!answer.equals("YES")) {
            }
            System.out.println(answer);

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTask(String[] tasks) {
        switch (tasks[1].toUpperCase()) {
            case "SELECT":
                if (tasks.length == 2)
                    sendSelect();
                else
                    sendSelect(tasks[2]);
                break;
            case "INSERT":
                sendInsert(tasks[2], tasks[3]);
                break;
        }
    }

    //ToDo: make less code in send*()
    private Map<String, Long> sendSelect() {
        Map<String, Long> response = new HashMap<>();
        try {
            for (Worker worker : workers) {
                Socket socket = new Socket(worker.getHost(), worker.getPort());
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream());

                output.append("SELECT").append("\n");
                output.flush();
                String answer = input.readLine();
                while (!answer.toUpperCase().equals("DONE")) {
                    String[] array = answer.split("\\W+");
                    response.put(array[0], Long.parseLong(array[1]));
                }
                System.out.println(answer);

                input.close();
                output.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private Long sendSelect(String key) {
        Worker worker = getWorkerByKey(key);
        Long result = null;
        try {
            Socket socket = new Socket(worker.getHost(), worker.getPort());
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            output.append("SELECT ").append(key).append("\n");
            output.flush();
            String answer = input.readLine();
            if (!answer.toUpperCase().equals("NONE"))
                result = Long.parseLong(answer.split("\\W+")[1]);

            input.close();
            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void sendInsert(String key, String value) {
        Worker worker = getWorkerByKey(key);
        try {
            Socket socket = new Socket(worker.getHost(), worker.getPort());
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            output.append("INSERT ").append(key).append(" ").append(value).append("\n");
            output.flush();
            System.out.println(input.readLine());

            input.close();
            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Worker getWorkerByKey(String key){
        return workers.get(key.hashCode() / workers.size());
    }

    private LinkedList readTasks() {
        return new LinkedList(Arrays.asList("100 SELECT", "200 INSERT abc 100", "300 SELECT"));
    }

    public static void main(String[] args) {
        Worker worker1 = new Worker("localhost", Integer.parseInt(args[0]));
        Worker worker2 = new Worker("localhost", Integer.parseInt(args[1]));

        Router router = new Router("localhost", 888, new ArrayList<>(Arrays.asList(worker1, worker2)));
        router.launch();
    }
}