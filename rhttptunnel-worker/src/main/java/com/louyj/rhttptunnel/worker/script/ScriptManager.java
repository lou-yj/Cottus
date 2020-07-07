package com.louyj.rhttptunnel.worker.script;

import javax.script.ScriptEngineManager;

import org.python.jsr223.PyScriptEngineFactory;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
public class ScriptManager {

	static {
		ScriptEngineManager engineManager = new ScriptEngineManager();

		PyScriptEngineFactory python = new PyScriptEngineFactory();
		engineManager.registerEngineName("python", python);

		bsh.engine.BshScriptEngineFactory bsh = new bsh.engine.BshScriptEngineFactory();
		engineManager.registerEngineName("java", bsh);
	}

}
