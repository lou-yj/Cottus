package com.louyj.rhttptunnel.worker.script;

import java.io.StringWriter;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.python.jsr223.PyScriptEngineFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.worker.script.shell.ShellScriptEngineFactory;

import bsh.engine.BshScriptEngineFactory;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
@Component
public class ScriptEngineExecutor implements InitializingBean {

	@Value("${work.directory}")
	private String workDirectory;

	private ScriptEngineManager engineManager;

	@Override
	public void afterPropertiesSet() throws Exception {
		engineManager = new ScriptEngineManager();

//		Field field = engineManager.getClass().getDeclaredField("DEBUG");
//
//		Field modifiersField = Field.class.getDeclaredField("modifiers");
//		modifiersField.setAccessible(true);
//		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//
//		field.setAccessible(true);
//		field.set(null, true);

		PyScriptEngineFactory python = new PyScriptEngineFactory();
		engineManager.registerEngineName("python", python);

		BshScriptEngineFactory bsh = new BshScriptEngineFactory();
		engineManager.registerEngineName("java", bsh);

		ShellScriptEngineFactory shell = new ShellScriptEngineFactory(workDirectory);
		engineManager.registerEngineName("shell", shell);
	}

	public EvalResult eval(String language, String script, Map<String, Object> env, boolean collectStdLog)
			throws ScriptException {
		ScriptEngine scriptEngine = engineManager.getEngineByName(language);
		if (scriptEngine == null) {
			throw new RuntimeException("No such language " + language);
		}
		ScriptContext context = new SimpleScriptContext();
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		StringWriter writer = null;
		StringWriter errwriter = null;
		if (collectStdLog) {
			writer = new StringWriter();
			errwriter = new StringWriter();
			context.setWriter(writer);
			context.setErrorWriter(errwriter);
		}
		if (env != null) {
			bindings.putAll(env);
		}
		Object eval = scriptEngine.eval(script, context);
		EvalResult result = new EvalResult();
		result.setEval(eval);
		result.setStdout(writer);
		result.setStderr(errwriter);
		return result;
	}

}
