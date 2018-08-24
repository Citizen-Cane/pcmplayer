package pcm.controller;

import pcm.model.Action;
import pcm.model.ScriptLineTokenizer;

interface StatementCollector {
    abstract void init();

    abstract void parse(ScriptLineTokenizer cmd);

    abstract void applyTo(Action action);
}