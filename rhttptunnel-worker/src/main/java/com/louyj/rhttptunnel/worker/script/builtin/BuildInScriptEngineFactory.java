package com.louyj.rhttptunnel.worker.script.builtin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
public class BuildInScriptEngineFactory implements ScriptEngineFactory {

	private static final Map<String, Object> parameters = new HashMap<String, Object>();

	private Map<String, IBuildInExecutor> buildInExecutors;

	public BuildInScriptEngineFactory(Map<String, IBuildInExecutor> buildInExecutors) {
		super();
		this.buildInExecutors = buildInExecutors;
	}

	static {
		parameters.put(ScriptEngine.NAME, "buildin");
		parameters.put(ScriptEngine.ENGINE, "buildin engine");
		parameters.put(ScriptEngine.ENGINE_VERSION, "1.0");
		parameters.put(ScriptEngine.LANGUAGE, "buildin");
		parameters.put(ScriptEngine.LANGUAGE_VERSION, "1.0");
	}

	@Override
	public String getEngineName() {
		return "buildin";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList(".java");
	}

	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList(new String[] { "application/x-buildin" });
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList("buildin");
	}

	@Override
	public String getLanguageName() {
		return "buildin";
	}

	@Override
	public String getLanguageVersion() {
		return "1.0";
	}

	@Override
	public Object getParameter(String key) {
		return parameters.get(key);
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		String methodCall = m + " ";
		for (String arg : args) {
			methodCall += arg + " ";
		}
		return methodCall;
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "System.out.println(\"" + toDisplay + "\")";
	}

	@Override
	public String getProgram(String... statements) {
		return "buildin";
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new BuildInScriptEngine(buildInExecutors, this);
	}

}
