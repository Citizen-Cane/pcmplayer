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

    final Map<String, String> declarations = new HashMap<>();

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
                QualifiedItem qualifiedItem = QualifiedItem.of(value);
                boolean isDeclared = qualifiedItem.namespace().equalsIgnoreCase(entry.getKey());
                if (isDeclared) {
                    if (isKeyword(entry, Declarations.ENUM)) {
                        ReflectionUtils.getEnum(qualifiedItem);
                        valueChecked = true;
                        break;
                    } else if (isKeyword(entry, Declarations.STRING)) {
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

    private static boolean isKeyword(java.util.Map.Entry<String, String> entry, String keyWord) {
        return entry.getValue().equalsIgnoreCase(keyWord);
    }

}
