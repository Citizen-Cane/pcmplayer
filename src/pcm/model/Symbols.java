package pcm.model;

import java.util.HashMap;

public class Symbols extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;

    public static final String Mm = "Mm";
    public static final String Fm = "Fm";
    public static final String Mf = "Mf";
    public static final String Ff = "Ff";
    public static final String Mtv = "Mtv";
    public static final String Ftv = "Ftv";

    public Symbols() {
        super();
    }

    public static Symbols getDominantSubmissiveRelations() {
        Symbols symbols = new Symbols();
        symbols.put(Mm, Mm);
        symbols.put(Fm, Fm);
        symbols.put(Mf, Mf);
        symbols.put(Ff, Ff);
        symbols.put(Mtv, Mtv);
        symbols.put(Ftv, Ftv);
        return symbols;
    }
}
