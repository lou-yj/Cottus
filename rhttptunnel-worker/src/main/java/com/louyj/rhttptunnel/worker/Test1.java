package com.louyj.rhttptunnel.worker;

import java.io.IOException;

import javax.script.ScriptException;

import com.louyj.rhttptunnel.worker.script.EvalResult;
import com.louyj.rhttptunnel.worker.script.ScriptEngineExecutor;

public class Test1 {

	public static void main(String[] args) throws IOException, ScriptException {

		ScriptEngineExecutor executor = new ScriptEngineExecutor();
		executor.setup();
		EvalResult evalResult = executor.eval("shell", "echo 123", null);
		System.out.println(evalResult.getEval());
		System.out.println(evalResult.getStdout().toString());
		System.out.println(evalResult.getStderr().toString());
		System.out.println(System.getProperty("sun.jnu.encoding"));
	}

}
