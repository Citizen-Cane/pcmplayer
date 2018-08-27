package pcm.model;

public interface StatementCollector {
    void init();

    void parse(ScriptLineTokenizer cmd);

    void nextSection(Action action);

    void applyTo(Action action);
}