package com.louyj.rhttptunnel.worker;

import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.python.jsr223.PyScriptEngineFactory;

public class Test1 {

	public static void main(String[] args) throws IOException, ScriptException {

		ScriptEngineManager engineManager = new ScriptEngineManager();
		PyScriptEngineFactory python = new PyScriptEngineFactory();
		engineManager.registerEngineName("python", python);

		List<ScriptEngineFactory> engineFactories = engineManager.getEngineFactories();
		System.out.println(engineFactories);

		bsh.engine.BshScriptEngineFactory bsh = new bsh.engine.BshScriptEngineFactory();
		System.out.println(bsh.getEngineName());

		ScriptEngine scriptEngine = engineManager.getEngineByName("BeanShell");
		scriptEngine = bsh.getScriptEngine();
		System.out.println(scriptEngine);
		scriptEngine.eval("print 123");

	}

}
