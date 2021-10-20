package pcm.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teaselib.State;
import teaselib.core.util.QualifiedString;
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
        String valueLowerCase;
        if (value.equalsIgnoreCase("item")) {
            valueLowerCase = ITEM;
        } else if (value.equalsIgnoreCase("state")) {
            valueLowerCase = STATE;
        } else {
            valueLowerCase = value.toLowerCase();
        }

        if (names.contains(valueLowerCase)) {
            nameDeclarations.put(namespace.toLowerCase(), valueLowerCase);
        } else if (types.contains(valueLowerCase)) {
            typeDeclarations.put(namespace.toLowerCase(), valueLowerCase);
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
                    var qualifiedValue = QualifiedString.of(value);
                    boolean isDeclared = qualifiedValue.namespace().equalsIgnoreCase(entry.getKey());
                    if (isDeclared) {
                        if (isKeyword(entry, Declarations.ENUM)) {
                            ReflectionUtils.getEnum(qualifiedValue);
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
                String key = QualifiedString.of(item).namespace().toLowerCase();
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
