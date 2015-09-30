package pcm.model;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

import pcm.model.AbstractAction.Statement;

public class ScriptLineTokenizer {
    public final int lineNumber;
    public final String line;
    private final StringTokenizer tokenizer;

    public final Statement statement;
    private String args[];

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
        if (args == null) {
            Vector<String> parsed = new Vector<String>();
            while (tokenizer.hasMoreTokens()) {
                parsed.add(tokenizer.nextToken());
            }
            args = toStringArray(parsed);
        }
        return args;
    }

    /**
     * Returns all arguments. Won't cut off anything.
     * 
     * @return The whole line
     */
    public String asText() {
        int s = statement.toString().length();
        if (line.length() > s + 1) {
            return line.substring(s + 2);
        } else {
            return "";
        }
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
            return asText();
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
            throw new IllegalArgumentException("Unknown statement " + statement);
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
