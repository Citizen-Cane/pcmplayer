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

        if (names.contains(value.toLowerCase())) {
            nameDeclarations.put(namespace, value);
        } else if (types.contains(value.toLowerCase())) {
            typeDeclarations.put(namespace, value);
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

    public void validate(List<String> qualifiedNames) throws ClassNotFoundException {
        for (String value : qualifiedNames) {
            if (!RuntimeVariable.isVariable(value)) {
                boolean valueChecked = false;
                for (Map.Entry<String, String> entry : entries()) {
                    QualifiedItem qualifiedItem = QualifiedItem.of(value);
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
                QualifiedItem qualifiedItem = QualifiedItem.of(item);
                if (!typeDeclarations.containsKey(qualifiedItem.namespace())) {
                    throw new IllegalArgumentException("Undefiend type " + item);
                } else if (!typeDeclarations.get(qualifiedItem.namespace()).equalsIgnoreCase(clazz.getName())) {
                    throw new IllegalArgumentException(
                            "Type " + item + " must be " + typeDeclarations.get(qualifiedItem.namespace()));
                }
            }
        }
        return true;
    }
}
