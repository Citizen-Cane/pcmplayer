package pcm.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ValidationError;
import teaselib.Actor;
import teaselib.TeaseLib;
import teaselib.core.ResourceLoader;

public class ScriptCache {

    Map<String, SoftReference<Script>> cache = new HashMap<String, SoftReference<Script>>();
    ResourceLoader resourceLoader;
    final String path;
    public final Stack<ActionRange> stack;

    public ScriptCache(ResourceLoader resourceLoader, String path) {
        this.resourceLoader = resourceLoader;
        this.path = path;
        stack = new Stack<ActionRange>();
    }

    public Script get(Actor actor, String name) throws ParseError,
            ValidationError, IOException {
        Script script = null;
        String key = name.toLowerCase();
        if (cache.containsKey(key)) {
            script = cache.get(key).get();
        }
        if (script != null) {
            TeaseLib.log("Using cached script " + name);
        } else {
            final String location = path + name;
            final BufferedReader scriptReader = resourceLoader.script(location);
            try {
                script = new Script(actor, name, this, scriptReader);
            } finally {
                scriptReader.close();
            }
            cache.put(key, new SoftReference<Script>(script));
        }
        return script;
    }

    public Set<String> names() {
        // Avoid concurrent modification exception when adding scripts while
        // iterating names
        return new HashSet<String>(cache.keySet());
    }
}
