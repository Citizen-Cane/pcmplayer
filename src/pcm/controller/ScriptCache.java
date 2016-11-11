package pcm.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
    private static final Logger logger = LoggerFactory
            .getLogger(ScriptCache.class);

    private final Symbols staticSymbols;
    private final Map<String, SoftReference<Script>> cache = new HashMap<String, SoftReference<Script>>();
    private final ResourceLoader resourceLoader;
    private final String resourcePath;
    private final String scriptPath;

    public final Stack<ActionRange> stack;

    public ScriptCache(ResourceLoader resourceLoader, String resourcePath,
            Symbols staticSymbols) {
        this.resourceLoader = resourceLoader;
        this.resourcePath = resourcePath;
        this.staticSymbols = staticSymbols;
        this.scriptPath = resourcePath + Player.Scripts;
        stack = new Stack<ActionRange>();
    }

    public Script get(Actor actor, String name)
            throws ScriptParsingException, ValidationIssue, IOException {
        Script script = null;
        if (cache.containsKey(name)) {
            script = cache.get(name).get();
        }
        if (script != null) {
            logger.debug("Using cached script " + name);
        } else {
            final String location = scriptPath + name;
            final BufferedReader scriptReader = script(location);
            try {
                script = new Script(actor, name, this, new ScriptParser(
                        scriptReader, resourcePath, staticSymbols));
            } finally {
                scriptReader.close();
            }
            cache.put(name, new SoftReference<Script>(script));
        }
        return script;
    }

    public BufferedReader script(String name) throws IOException {
        String path = name + ".sbd";
        InputStream inputStream = resourceLoader.getResource(path);
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public Set<String> names() {
        // Avoid concurrent modification exception when adding scripts while
        // iterating names
        return new HashSet<String>(cache.keySet());
    }
}
