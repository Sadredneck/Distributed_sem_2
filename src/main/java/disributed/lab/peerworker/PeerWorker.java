package main.java.disributed.lab.peerworker;

import main.java.disributed.lab.CommandInterpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PeerWorker {
    private ServerSocket serverSocket;
    private String host;
    private int port;
    private int number;
    private boolean working = true;
    private int state = 0;

    private Thread inputThread;
    private CommandInterpreter interpreter = new CommandInterpreter();
    private Map<Integer, PeerWorker> peerWorkers = new HashMap<>();
    private LinkedList<Thread> inputThreads = new LinkedList<>();
    private LinkedList<Thread> outputThreads = new LinkedList<>();

    public PeerWorker(String host, int port, int number) {
        this.host = host;
        this.port = port;
        this.number = number;
    }

    public static void main(String[] args) {
        PeerWorker worker = new PeerWorker("localhost", Integer.valueOf(args[1]), Integer.valueOf(args[0]));

        worker.peerWorkers.put(0, new PeerWorker("localhost", 555, 0));
        worker.peerWorkers.put(1, new PeerWorker("localhost", 777, 1));
        worker.peerWorkers.put(2, new PeerWorker("localhost", 888, 2));

        worker.launch();
    }

    public void launch() {
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));
            System.out.printf("Router number %d %s: %d started.%n", number, host, port);
            inputThread = new Thread(this::acceptRequests);
            inputThread.start();

            waitForAll();
            performTasks(readTasks());

            working = false;
            emptyThreads(outputThreads);
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Waiting all workers
    private void waitForAll() {
        for (Map.Entry<Integer, PeerWorker> entry : peerWorkers.entrySet()) {
            if (entry.getKey() != number) {
                boolean trying = true;
                while (trying) {
                    try (Socket socket = new Socket(entry.getValue().getHost(), entry.getValue().getPort())) {
                        PrintWriter output = new PrintWriter(socket.getOutputStream());

                        output.append("HI").append("\n");
                        output.flush();

                        output.close();
                        socket.close();
                        trying = false;
                    } catch (IOException ex) {
                        try {
                            Thread.sleep(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //Work with commands
    private void performTasks(LinkedList<String> tasks) {
        while (!tasks.isEmpty()) {
            String[] task = tasks.removeFirst().trim().split("\\W+");

            if (Integer.parseInt(task[0]) != state) {
                emptyThreads(inputThreads);
                sendState(Integer.valueOf(task[0]));
                updateState(number, Integer.parseInt(task[0]));
            }
            while (Integer.parseInt(task[0]) != state) {
            }

            Thread thread = new Thread(() -> sendTask(task));
            thread.start();
            inputThreads.add(thread);
        }
        emptyThreads(inputThreads);
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

    private void sendState(int state) {
        try {
            for (Map.Entry<Integer, PeerWorker> entry : peerWorkers.entrySet()) {
                if (entry.getKey() != number) {
                    Socket socket = new Socket(entry.getValue().host, entry.getValue().port);
                    PrintWriter output = new PrintWriter(socket.getOutputStream());

                    output.append("STATE ").append(String.valueOf(number)).append(" ").append(String.valueOf(state)).append("\n");
                    output.flush();

                    output.close();
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Long> sendSelect() {
        Map<String, Long> response = new HashMap<>();
        try {
            for (Map.Entry<Integer, PeerWorker> entry : peerWorkers.entrySet()) {
                if (entry.getKey() == number)
                    interpreter.doSelect();
                else {
                    Socket socket = new Socket(entry.getValue().host, entry.getValue().port);
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private Long sendSelect(String key) {
        PeerWorker worker = getWorkerByKey(key);
        Long result = null;
        if (worker.getNumber() == number)
            result = Long.parseLong(interpreter.doSelect(key));
        else
            try {
                Socket socket = new Socket(worker.host, worker.port);
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
        PeerWorker worker = getWorkerByKey(key);
        if (worker.getNumber() == number)
            interpreter.doInsert(key, Long.parseLong(value));
        else
            try {
                Socket socket = new Socket(worker.host, worker.port);
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

    private PeerWorker getWorkerByKey(String key) {
        return peerWorkers.get(key.hashCode() / peerWorkers.size());
    }


    //Work with coming commands
    private void acceptRequests() {
        while (working) {
            try {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(() -> doConnection(socket));
                thread.start();
                outputThreads.add(thread);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doConnection(Socket socket) {
        try {
            System.out.println("Received request");
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            perform(output, input.readLine());

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
            case "STATE":
                updateState(Integer.valueOf(words[1]), Integer.valueOf(words[2]));
                break;
        }
    }

    private void updateState(int workerNumber, int workerState) {
        peerWorkers.get(workerNumber).state = workerState;
        int minState = Integer.MAX_VALUE;
        for (Map.Entry<Integer, PeerWorker> entry : peerWorkers.entrySet())
            minState = Math.min(entry.getValue().state, minState);
        state = minState;
    }

    private void emptyThreads(LinkedList<Thread> threads) {
        while (threads.stream().anyMatch(Thread::isAlive)) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private LinkedList readTasks() {
        return new LinkedList(Arrays.asList("100 SELECT", "200 INSERT abc 100", "300 SELECT"));
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getNumber() {
        return number;
    }
}
