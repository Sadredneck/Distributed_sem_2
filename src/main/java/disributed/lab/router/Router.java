package main.java.disributed.lab.router;

import main.java.disributed.lab.worker.Worker;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Router {

    private ServerSocket serverSocket;
    private String host;
    private int port;
    private List<Worker> workers;
    private List<Socket> sockets;

    public Router(String host, int port, List<Worker> workers) {
        this.host = host;
        this.port = port;
        this.workers = workers;
        this.sockets = new ArrayList<>();
    }

    public void launch() {
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));

            for (Worker worker : workers) {
                sockets.add(new Socket(worker.getHost(), worker.getPort()));
            }

            /*
            BufferedReader input = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(workerSocket.getOutputStream()));
            output.write("Hi.");
            output.flush();
            output.close();
            workerSocket.close();
            */
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkActive(Socket socket){
        //ToDo: make interval check
    }

    public void sendTask(Socket socket){
        //ToDo: Send task to curtain worker
    }

    public void readTasks(){
        //ToDo: read file from file
    }

    public static void main(String[] args) {
        Router router = new Router("localhost", 888, new ArrayList<>());
        router.launch();
    }
}
