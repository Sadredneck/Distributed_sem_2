package main.java.disributed.lab;

import java.util.HashMap;
import java.util.Map;

public class CommandInterpreter {

    private Map<String, Long> storage = new HashMap<>();

    private void doSelect(String key) {
        if (storage.getOrDefault(key, null) == null) {
            System.out.printf("Value for '%s' is absent%n", key);
        } else System.out.printf("Value for '%s' is %d%n", key, storage.get(key));
    }

    private void doSelect() {
        storage.entrySet().forEach(System.out::println);
    }

    private void doInsert(String key, long value) {
        storage.put(key, value);
        System.out.printf("Value for '%s' set to %d%n", key, storage.get(key));
    }

    //ToDo: create checks
    public void perform(String task) {
        String[] words = task.split("\\W+");

        switch (words[0].toUpperCase()) {
            case "SELECT":
                if (words.length == 1)
                    doSelect();
                else
                    doSelect(words[1]);
                break;
            case "INSERT":
                doInsert(words[1], Long.parseLong(words[2]));
                break;
        }
    }

    public static boolean isCommand(String line) {
        if (line == null)
            return false;
        String[] items = line.split(" ");
        if (items.length < 1 || items.length > 3)
            return false;
        switch (items[0].toUpperCase()) {
            case "SELECT":
                return true;
            case "INSERT":
                return true;
        }
        return false;
    }
}
