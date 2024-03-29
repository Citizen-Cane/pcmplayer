package pcm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import pcm.controller.Declarations;
import pcm.model.AbstractAction.Statement;

public class ScriptLineTokenizer {
    final Script script;
    final int lineNumber;
    final String line;
    final String code;
    private final StringTokenizer tokenizer;

    public final Statement statement;
    final Declarations declarations;

    private String argv[];

    // TODO Split into statement parsing and line arguments parsing -> speed up message creation
    public ScriptLineTokenizer(int lineNumber, String line, Script script, Declarations declarations) {
        this.script = script;
        this.lineNumber = lineNumber;
        this.line = line;
        this.declarations = declarations;
        // Cut off comment at the end of the line
        int commentStart = line.indexOf('\'');
        this.code = (commentStart > 0 ? line.substring(0, commentStart) : line).trim();
        this.tokenizer = new StringTokenizer(this.code, " \t");
        String token = tokenizer.nextToken().toLowerCase();
        String statementString = token.substring(1);
        this.statement = parseStatement(statementString);
    }

    public ScriptLineTokenizer(Statement statement, int lineNumber, String line, Script script, Declarations declarations) {
        this.script = script;
        this.lineNumber = lineNumber;
        this.line = line;
        this.declarations = declarations;
        this.code = line;
        this.tokenizer = null;
        this.statement = statement;
    }

    public String[] args() {
        if (argv == null) {
            List<String> parsed = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                parsed.add(tokenizer.nextToken());
            }
            argv = toStringArray(parsed);
        }
        return argv;
    }

    public String[] args(int from) {
        return Arrays.copyOfRange(args(), from, args().length);
    }

    public void addArgsTo(Collection<Integer> collection) {
        String[] args = args();
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments");
        }
        for (String arg : args) {
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
        return s.toString().trim();
    }

    /**
     * Return all, including the comment
     * 
     * @return The whole line
     */
    public String allAsText() {
        int s = statement.toString().length();
        if (code.length() > s + 1) {
            return line.substring(s + 2).trim();
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
        return line.substring(index).trim();
    }

    /**
     * Returns all arguments of the statement. A comment is cut off, so this won't work for text messages. It's great
     * however for retrieving file names.
     * 
     * @return All command line arguments
     */
    public String asFilename() {
        final int indexOfCommentStart = line.indexOf('\'');
        if (indexOfCommentStart < 0) {
            return allAsText();
        } else {
            return line.substring(statement.toString().length() + 1, indexOfCommentStart - 1).trim();
        }
    }

    private static Statement parseStatement(String statement) {
        if (Statement.Lookup.containsKey(statement)) {
            return Statement.Lookup.get(statement);
        } else {
            throw new IllegalArgumentException("Unknown statement " + statement);
        }
    }

    private static String[] toStringArray(List<String> list) {
        return Arrays.copyOf(list.toArray(), list.size(), String[].class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + code;
    }
}
