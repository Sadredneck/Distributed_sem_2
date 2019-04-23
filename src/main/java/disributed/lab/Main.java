package main.java.disributed.lab;

import main.java.disributed.lab.router.Router;
import main.java.disributed.lab.worker.Worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Worker worker1 = new Worker("localhost", 999);
        Worker worker2 = new Worker("localhost", 1111);
        List<Worker> workers = new ArrayList<>(Arrays.asList(worker1, worker2));
        Router router = new Router("localhost", 888, workers);

        new Thread(worker1::launch).start();
        new Thread(worker2::launch).start();
        new Thread(router::launch).start();
    }
}
