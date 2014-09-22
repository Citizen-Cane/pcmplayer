package pcm.controller;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ValidationError;
import teaselib.ResourceLoader;
import teaselib.TeaseLib;

public class ScriptCache {

	Map<String, WeakReference<Script>> cache = new HashMap<>();
	ResourceLoader resourceLoader;
	final String path;
	
	public ScriptCache(ResourceLoader resourceLoader, String path)
	{
		this.resourceLoader = resourceLoader;
		this.path = path;
	}
	
	public Script get(String name) throws ParseError, ValidationError, IOException
	{
		Script script = null;
		String key = name.toLowerCase();
		if (cache.containsKey(key))
		{
			TeaseLib.log("Using cached script " + name);
			script = cache.get(key).get();
		}
		if (script == null)
		{
			script = new Script(name, this, resourceLoader.script(path + name));
			cache.put(key, new WeakReference<>(script));
		}
		return script;
	}

	public Set<String> scripts()
	{
		return cache.keySet();
	}
}
