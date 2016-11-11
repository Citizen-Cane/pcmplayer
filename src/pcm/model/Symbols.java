package pcm.model;

import java.util.HashMap;

public class Symbols extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;

    public static final String Fm = "Fm";
    public static final String Ff = "Ff";
    public static final String Ftv = "Ftv";

    public Symbols() {
        super();
    }

    public static Symbols getDominantSubmissiveRelations() {
        Symbols symbols = new Symbols();
        symbols.put(Fm, Fm);
        symbols.put(Ff, Ff);
        symbols.put(Ftv, Ftv);
        return symbols;
    }
}
