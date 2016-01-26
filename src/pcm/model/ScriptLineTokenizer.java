package pcm.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import pcm.model.AbstractAction.Statement;

public class ScriptLineTokenizer {
    public final int lineNumber;
    public final String line;
    private final StringTokenizer tokenizer;

    public final Statement statement;

    private String argv[];

    public ScriptLineTokenizer(int lineNumber, String line) {
        this.lineNumber = lineNumber;
        this.line = line;
        // Cut off comment at the end of the line
        int commentStart = line.indexOf("'");
        line = (commentStart > 0 ? line.substring(0, commentStart) : line)
                .trim();
        tokenizer = new StringTokenizer(line, " \t");
        String token = tokenizer.nextToken().toLowerCase();
        String statementString = token.substring(1);
        statement = parseStatement(statementString);
    }

    public String[] args() {
        if (argv == null) {
            Vector<String> parsed = new Vector<String>();
            while (tokenizer.hasMoreTokens()) {
                parsed.add(tokenizer.nextToken());
            }
            argv = toStringArray(parsed);
        }
        return argv;
    }

    public void addArgsTo(Collection<Integer> collection) {
        for (String arg : args()) {
            collection.add(Integer.parseInt(arg));
        }
    }

    public String allArgs() {
        return argsFrom(0);
    }

    public String argsFrom(int n) {
        StringBuilder s = new StringBuilder();
        String[] args = args();
        s.append(args[n]);
        for (int i = n + 1; i < args.length; i++) {
            s.append(" ");
            s.append(args[i]);
        }
        return s.toString();
    }

    /**
     * Return all, including the comment
     * 
     * @return The whole line
     */
    public String allAsText() {
        int s = statement.toString().length();
        if (line.length() > s + 1) {
            return line.substring(s + 2);
        } else {
            return "";
        }
    }

    /**
     * Return all, including the comment, starting from argument n
     * 
     * @param n
     * @return
     */
    public String allAsTextFrom(int n) {
        int index = statement.toString().length() + 2;
        String[] args = args();
        for (int i = 0; i < n; i++) {
            String arg = args[i];
            index = line.indexOf(arg, index) + arg.length() + 1;
        }
        return line.substring(index);
    }

    /**
     * Returns all arguments of the statement. A comment is cut off, so this
     * won't work for text messages. It's great however for retrieving file
     * names.
     * 
     * @return All command line arguments
     */
    public String asFilename() {
        final int indexOfCommentStart = line.indexOf("'");
        if (indexOfCommentStart < 0) {
            return allAsText();
        } else {
            return line.substring(statement.toString().length() + 1,
                    indexOfCommentStart - 1).trim();
        }
    }

    private static Statement parseStatement(String statement) {
        String key = statement.toLowerCase();
        if (Statement.lookup.containsKey(key)) {
            return Statement.lookup.get(key);
        } else {
            throw new IllegalArgumentException(
                    "Unknown statement " + statement);
        }
    }

    private static String[] toStringArray(Vector<String> collection) {
        return Arrays.copyOf(collection.toArray(), collection.size(),
                String[].class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + line;
    }
}
