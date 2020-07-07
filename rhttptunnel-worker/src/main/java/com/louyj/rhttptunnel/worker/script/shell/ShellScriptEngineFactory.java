package com.louyj.rhttptunnel.worker.script.shell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import com.louyj.rhttptunnel.worker.shell.ShellWrapper;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
public class ShellScriptEngineFactory implements ScriptEngineFactory {

	private static final Map<String, Object> parameters = new HashMap<String, Object>();

	private String workDirectory;

	public ShellScriptEngineFactory(String workDirectory) {
		super();
		this.workDirectory = workDirectory;
	}

	static {
		parameters.put(ScriptEngine.NAME, "shell");
		parameters.put(ScriptEngine.ENGINE, "shell script engine");
		parameters.put(ScriptEngine.ENGINE_VERSION, "1.0");
		parameters.put(ScriptEngine.LANGUAGE, "shell");
		parameters.put(ScriptEngine.LANGUAGE_VERSION, "1.0");
	}

	@Override
	public String getEngineName() {
		return "shell";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList(".sh");
	}

	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList(new String[] { "application/x-shell" });
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList("shell");
	}

	@Override
	public String getLanguageName() {
		return "shell";
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
		return "echo -n " + toDisplay;
	}

	@Override
	public String getProgram(String... statements) {
		String program = "#!/bin/bash\n";
		for (String statement : statements) {
			program += statement + "\n";
		}
		return program;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		ShellWrapper shell = new ShellWrapper(workDirectory);
		return new ShellScriptEngine(shell, this);
	}

}
