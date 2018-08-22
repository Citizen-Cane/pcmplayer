package pcm.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RuntimeVariables {
    private final Map<String, RuntimeVariable> entries = new HashMap<>();

    public void add(String name, Supplier<String> value) {
        entries.put(name, new RuntimeVariable(name, value));
    }

    public RuntimeVariable get(String name) {
        return entries.get(RuntimeVariable.stripped(name));
    }
}
