package com.louyj.rhttptunnel.worker.script.shell;

import java.io.Reader;
import java.util.List;
import java.util.Map.Entry;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.louyj.rhttptunnel.worker.shell.ShellWrapper;
import com.louyj.rhttptunnel.worker.shell.ShellWrapper.ShellOutput;
import com.louyj.rhttptunnel.worker.shell.ShellWrapper.SubmitStatus;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
public class ShellScriptEngine extends AbstractScriptEngine {

	private ShellWrapper shell;
	private ScriptEngineFactory factory;

	public ShellScriptEngine(ShellWrapper shell, ScriptEngineFactory factory) {
		super();
		this.shell = shell;
		this.factory = factory;
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		try {
			shell.setup();
			Bindings bindings = getBindings(ScriptContext.ENGINE_SCOPE);
			for (Entry<String, Object> entry : bindings.entrySet()) {
				Pair<SubmitStatus, String> submit = shell
						.submit(String.format("export %s=%s", entry.getKey(), String.valueOf(entry.getValue())));
				shell.fetchAllResult(submit.getRight());
			}
			Pair<SubmitStatus, String> submit = shell.submit(script);
			ShellOutput shellResult = shell.fetchAllResult(submit.getRight());
			ShellScriptResult result = new ShellScriptResult();
			result.setStdout(StringUtils.join(shellResult.out, "\n"));
			result.setStderr(StringUtils.join(shellResult.err, "\n"));
			submit = shell.submit("echo $?");
			shellResult = shell.fetchAllResult(submit.getRight());
			int exitValue = NumberUtils.toInt(StringUtils.trim(StringUtils.join(shellResult.out, "\n")));
			result.setExitValue(exitValue);
			IOUtils.closeQuietly(shell);
			return result;
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
