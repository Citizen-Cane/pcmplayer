package pcm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teaselib.core.util.QualifiedItem;
import teaselib.core.util.ReflectionUtils;

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

    public List<String> checked(List<String> qualifiedNames) throws ClassNotFoundException {
        for (String value : qualifiedNames) {
            boolean valueChecked = false;
            for (java.util.Map.Entry<String, String> entry : entries()) {
                if (QualifiedItem.fromType(value).namespace().equalsIgnoreCase(entry.getKey())) {
                    if (entry.getValue().equalsIgnoreCase(Declarations.ENUM)) {
                        ReflectionUtils.getEnum(value);
                        valueChecked = true;
                        break;
                    } else if (entry.getValue().equalsIgnoreCase(Declarations.STRING)) {
                        valueChecked = true;
                        break;
                    }
                }
            }
            if (!valueChecked) {
                throw new IllegalArgumentException("Undeclared qualified name " + value);
            }
        }
        return qualifiedNames;
    }

}
