package pcm.controller;

import java.util.function.Supplier;

public class RuntimeVariable {
    private static final String PREFIX = "$(";
    private static final String POSTFIX = ")";

    private final String name;
    private Supplier<String> value;

    public RuntimeVariable(String name, Supplier<String> value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String decoratedName() {
        return PREFIX + name + POSTFIX;
    }

    public String value() {
        return value.get();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RuntimeVariable other = (RuntimeVariable) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    static boolean isVariable(String name) {
        return name.startsWith(PREFIX) && name.endsWith(POSTFIX);
    }

    /**
     * @param variable
     *            String with the optional format $(name)
     * @return stripped name
     */
    static String stripped(String name) {
        if (name.startsWith(PREFIX)) {
            int prefix = RuntimeVariable.PREFIX.length();
            int postfix = POSTFIX.length();
            return name.substring(prefix, name.length() - postfix);
        } else {
            return name;
        }
    }
}
