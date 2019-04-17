package main.java.disributed.lab;

import main.java.disributed.lab.router.Router;
import main.java.disributed.lab.worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Worker worker = new Worker("localhost", 999);
        List<Worker> workers = new ArrayList<>();
        workers.add(worker);
        Router router = new Router("localhost", 888, workers);

        new Thread(worker::launch).start();
        new Thread(router::launch).start();
    }
}
