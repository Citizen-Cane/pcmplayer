package pcm.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptParsingException;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;
import teaselib.Actor;
import teaselib.core.ResourceLoader;

public class ScriptCache {
    private static final Logger logger = LoggerFactory.getLogger(ScriptCache.class);

    private final Symbols staticSymbols;
    private final Map<String, SoftReference<Script>> cache = new HashMap<>();
    private final ResourceLoader resourceLoader;
    private final String path;

    public final Deque<ActionRange> stack;

    public ScriptCache(ResourceLoader resourceLoader, String path, Symbols staticSymbols) {
        this.resourceLoader = resourceLoader;
        this.staticSymbols = staticSymbols;
        this.path = path;
        stack = new ArrayDeque<>();
    }

    public Script get(Actor actor, String name) throws ScriptParsingException, ValidationIssue, IOException {
        Script script = null;
        if (cache.containsKey(name)) {
            script = cache.get(name).get();
        }
        if (script != null) {
            logger.debug("Using cached script {}", name);
        } else {
            try (BufferedReader scriptReader = script(name);) {
                script = new Script(actor, name, this, new ScriptParser(scriptReader, staticSymbols, this));
            }
            cache.put(name, new SoftReference<>(script));
        }
        return script;
    }

    public BufferedReader script(String name) throws IOException {
        return loadScript(path + name + ".sbd");
    }

    public BufferedReader subScript(String name) throws IOException {
        return loadScript(path + name + ".sbe");
    }

    private BufferedReader loadScript(String path) throws IOException {
        InputStream inputStream = resourceLoader.get(path);
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public Set<String> names() {
        return avoidConcurrentModificationException();
    }

    private HashSet<String> avoidConcurrentModificationException() {
        return new HashSet<>(cache.keySet());
    }
}
