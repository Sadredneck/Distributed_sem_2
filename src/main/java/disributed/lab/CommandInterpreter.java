package main.java.disributed.lab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandInterpreter {

    private Map<String, Long> storage = new HashMap<>();

    public String doSelect(String key) {
        if (storage.getOrDefault(key, null) == null) {
            System.out.printf("Value for '%s' is absent%n", key);
            return "NONE";
        } else {
            System.out.printf("Value for '%s' is %d%n", key, storage.get(key));
            return String.valueOf(storage.get(key));
        }
    }

    public List<String> doSelect() {
        storage.entrySet().forEach(System.out::println);
        return storage.entrySet().stream().map(x -> x.getKey() + " " + x.getValue()).collect(Collectors.toList());
    }

    public void doInsert(String key, long value) {
        storage.put(key, value);
        System.out.printf("Value for '%s' set to %d%n", key, storage.get(key));
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
