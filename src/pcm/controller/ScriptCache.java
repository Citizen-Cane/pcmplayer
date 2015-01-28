package pcm.controller;

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
import teaselib.ResourceLoader;
import teaselib.TeaseLib;

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

    public Script get(String name) throws ParseError, ValidationError,
            IOException {
        Script script = null;
        String key = name.toLowerCase();
        if (cache.containsKey(key)) {
            script = cache.get(key).get();
        }
        if (script != null) {
            TeaseLib.log("Using cached script " + name);
        } else {
            script = new Script(name, this, resourceLoader.script(path + name));
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
