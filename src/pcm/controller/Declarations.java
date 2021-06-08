package pcm.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teaselib.State;
import teaselib.core.util.QualifiedItem;
import teaselib.core.util.ReflectionUtils;

public class Declarations {
    public static final String ENUM = "enum";
    public static final String STRING = "string";
    private static final Set<String> names = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ENUM, STRING)));

    public static final String STATE = teaselib.State.class.getName().toLowerCase();
    public static final String ITEM = teaselib.util.Item.class.getName().toLowerCase();
    private static final Set<String> types = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(STATE, ITEM)));

    private final Map<String, String> nameDeclarations;
    private final Map<String, String> typeDeclarations;

    public Declarations() {
        this.nameDeclarations = new HashMap<>();
        this.typeDeclarations = new HashMap<>();
    }

    public void add(String namespace, String value) {
        if (value.equalsIgnoreCase("item")) {
            value = ITEM;
        } else if (value.equalsIgnoreCase("state")) {
            value = STATE;
        }

        String valueIgnoreCase = value.toLowerCase();
        if (names.contains(valueIgnoreCase)) {
            nameDeclarations.put(namespace.toLowerCase(), valueIgnoreCase);
        } else if (types.contains(valueIgnoreCase)) {
            typeDeclarations.put(namespace.toLowerCase(), valueIgnoreCase);
        } else {
            throw new IllegalArgumentException(value);
        }
    }

    public boolean available() {
        return !nameDeclarations.isEmpty();
    }

    public Set<Map.Entry<String, String>> entries() {
        return nameDeclarations.entrySet();
    }

    public void validate(List<String> qualifiedNames) {
        for (String value : qualifiedNames) {
            if (!RuntimeVariable.isVariable(value)) {
                var valueChecked = false;
                for (Map.Entry<String, String> entry : entries()) {
                    var qualifiedItem = QualifiedItem.of(value);
                    boolean isDeclared = qualifiedItem.namespace().equalsIgnoreCase(entry.getKey());
                    if (isDeclared) {
                        if (isKeyword(entry, Declarations.ENUM)) {
                            ReflectionUtils.getEnum(qualifiedItem);
                            valueChecked = true;
                        } else if (isKeyword(entry, Declarations.STRING)) {
                            valueChecked = true;
                        }
                    }
                    if (valueChecked) {
                        break;
                    }
                }
                if (!valueChecked) {
                    throw new IllegalArgumentException("Undeclared qualified name " + value);
                }
            }
        }
    }

    private static boolean isKeyword(Map.Entry<String, String> entry, String keyWord) {
        return entry.getValue().equalsIgnoreCase(keyWord);
    }

    public boolean validate(String[] items, Class<? extends State> clazz) {
        for (String item : items) {
            if (!RuntimeVariable.isVariable(item)) {
                var qualifiedItem = QualifiedItem.of(item);
                String key = qualifiedItem.namespace().toLowerCase();
                if (!typeDeclarations.containsKey(key)) {
                    throw new IllegalArgumentException("Undefiend type " + item);
                } else if (!typeDeclarations.get(key).equalsIgnoreCase(clazz.getName())) {
                    throw new IllegalArgumentException("Type " + item + " must be " + typeDeclarations.get(key));
                }
            }
        }
        return true;
    }
}
