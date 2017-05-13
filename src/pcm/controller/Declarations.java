package pcm.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Declarations {
    public static final String ENUM = "enum";
    public static final String STRING = "string";

    final Map<String, String> declarations = new HashMap<String, String>();

    public Declarations() {
    }

    public void add(String namespace, String type) {
        declarations.put(namespace, type);
    }

    public boolean available() {
        return !declarations.isEmpty();
    }

    public Set<Map.Entry<String, String>> entries() {
        return declarations.entrySet();
    }
}
