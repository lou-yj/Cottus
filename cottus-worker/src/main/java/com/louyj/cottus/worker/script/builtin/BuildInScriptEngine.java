package com.louyj.cottus.worker.script.builtin;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
public class BuildInScriptEngine extends AbstractScriptEngine {

	private Map<String, IBuildInExecutor> buildInExecutors;
	private BuildInScriptEngineFactory factory;

	public BuildInScriptEngine(Map<String, IBuildInExecutor> buildInExecutors, BuildInScriptEngineFactory factory) {
		super();
		this.buildInExecutors = buildInExecutors;
		this.factory = factory;
	}

	@Override
	public Object eval(String type, ScriptContext context) throws ScriptException {
		try {

			IBuildInExecutor buildInExecutor = buildInExecutors.get(type);
			if (buildInExecutor == null) {
				throw new RuntimeException("No such buildin executor " + type);
			}
			Bindings bindings = getBindings(ScriptContext.ENGINE_SCOPE);
			return buildInExecutor.execute(bindings);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		try {
			List<String> lines = IOUtils.readLines(reader);
			return eval(StringUtils.join(lines, "\n"), context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

}
